// ============================================================
//  TankCoPilot.ino
//  Dual-motor RC tank · Arduino UNO R4 WiFi
//  IBT-2 x2 (differential drive) + ZED-F9P RTK GPS (UBX)
//  Commands via JSON-over-UDP (port 5006)
//  Telemetry via JSON-over-UDP (port 5005)
//  OTA firmware updates via ArduinoOTA over WiFi
//
//  Library required:
//    SparkFun u-blox GNSS Arduino Library v3
//    Arduino IDE → Library Manager → "SparkFun u-blox GNSS v3"
// ============================================================

#include <WiFiS3.h>
#include <WiFiUdp.h>
#include <ArduinoOTA.h>
#include <SparkFun_u-blox_GNSS_v3.h>

// ── WiFi credentials ────────────────────────────────────────
const char* SSID     = "Dexter5G 2";
const char* PASSWORD = "madmax";

// ── OTA config ───────────────────────────────────────────────
const char* OTA_HOSTNAME = "Tank";   // shows in Arduino IDE port list
const char* OTA_PASSWORD = "Dexter";      // set to something private

// ── UDP ports ───────────────────────────────────────────────
const uint16_t PORT_TELEM = 5005;   // outbound telemetry
const uint16_t PORT_CMD   = 5006;   // inbound commands

// ── IBT-2 pin assignments ────────────────────────────────────
//   Each IBT-2 needs RPWM + LPWM.
//   R_EN / L_EN are wired HIGH (always enabled).
//
//   Left track
const uint8_t LEFT_RPWM  = 5;   // forward PWM
const uint8_t LEFT_LPWM  = 6;   // reverse PWM
//   Right track
const uint8_t RIGHT_RPWM = 9;   // forward PWM
const uint8_t RIGHT_LPWM = 10;  // reverse PWM

// ── Motor limits ─────────────────────────────────────────────
const uint8_t PWM_MIN   = 0;
const uint8_t PWM_MAX   = 255;
const uint8_t PWM_DEADBAND = 10;  // below this → coast

// ── Safety watchdog ──────────────────────────────────────────
const uint32_t CMD_TIMEOUT_MS = 500;   // stop if no command for 500 ms
uint32_t lastCmdMs = 0;

// ── GPS / UBX ────────────────────────────────────────────────
//   ZED-F9P TX→RX1, RX→TX1 (Serial1)
//   Library talks UBX binary — leave ZED-F9P at default 38400
//   or call gnss.setSerialRate() after begin() to change it.
const uint32_t GPS_BAUD = 38400;

SFE_UBLOX_GNSS gnss;

struct GpsState {
  double   lat        = 0.0;
  double   lon        = 0.0;
  double   altM       = 0.0;
  float    hdop       = 99.9f;
  float    speedKnots = 0.0f;
  float    courseDeg  = 0.0f;
  uint8_t  rtkType    = 0;   // 0=none, 1=RTK float, 2=RTK fixed
  uint8_t  satellites = 0;
  bool     valid      = false;
} gps;

// ── Network objects ───────────────────────────────────────────
WiFiUDP udp;
IPAddress remoteIP;
bool remoteKnown = false;

// ── Buffers ───────────────────────────────────────────────────
char rxBuf[256];

// ── Telemetry interval ────────────────────────────────────────
const uint32_t TELEM_INTERVAL_MS = 100;
uint32_t lastTelemMs = 0;

// ── Motor state ───────────────────────────────────────────────
int16_t leftSpeed  = 0;   // -255 … +255
int16_t rightSpeed = 0;

// ============================================================
//  Setup
// ============================================================
void setup() {
  Serial.begin(115200);

  // ── ZED-F9P via SparkFun GNSS library ───────────────────
  Serial1.begin(GPS_BAUD);
  if (!gnss.begin(Serial1)) {
    Serial.println("ZED-F9P not detected — check wiring");
    while (true) delay(1000);
  }
  gnss.setUART1Output(COM_TYPE_UBX);          // UBX only, no NMEA noise
  gnss.setNavigationFrequency(5);             // 5 Hz position updates
  gnss.setAutoPVT(true);                      // push PVT packets automatically
  gnss.saveConfigSelective(VAL_CFG_SUBSEC_IOPORT); // persist to F9P flash
  Serial.println("ZED-F9P ready");

  // IBT-2 pins
  pinMode(LEFT_RPWM,  OUTPUT);
  pinMode(LEFT_LPWM,  OUTPUT);
  pinMode(RIGHT_RPWM, OUTPUT);
  pinMode(RIGHT_LPWM, OUTPUT);
  motorsStop();

  // WiFi
  Serial.print("Connecting to ");
  Serial.println(SSID);
  WiFi.begin(SSID, PASSWORD);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print('.');
  }
  Serial.print("\nIP: ");
  Serial.println(WiFi.localIP());

  udp.begin(PORT_CMD);
  Serial.println("TankCoPilot ready");

  // ── OTA ──────────────────────────────────────────────────
  ArduinoOTA.setHostname(OTA_HOSTNAME);
  ArduinoOTA.setPassword(OTA_PASSWORD);

  ArduinoOTA.onStart([]() {
    motorsStop();   // safety: halt motors before flashing
    Serial.println("OTA start");
  });
  ArduinoOTA.onEnd([]() {
    Serial.println("OTA complete – rebooting");
  });
  ArduinoOTA.onProgress([](unsigned int done, unsigned int total) {
    Serial.printf("OTA %u%%\n", done * 100 / total);
  });
  ArduinoOTA.onError([](ota_error_t err) {
    Serial.printf("OTA error [%u]\n", err);
  });

  ArduinoOTA.begin();
  Serial.print("OTA ready at ");
  Serial.println(WiFi.localIP());
}

// ============================================================
//  Loop
// ============================================================
void loop() {
  ArduinoOTA.handle();   // must be first — handles incoming flash requests
  updateGps();
  readUdpCommands();
  checkWatchdog();

  if (millis() - lastTelemMs >= TELEM_INTERVAL_MS) {
    sendTelemetry();
    lastTelemMs = millis();
  }
}

// ============================================================
//  Motor control
// ============================================================

// speed: -255 (full reverse) … 0 (stop) … +255 (full forward)
void setLeftMotor(int16_t speed) {
  leftSpeed = constrain(speed, -PWM_MAX, PWM_MAX);
  applyIBT2(LEFT_RPWM, LEFT_LPWM, leftSpeed);
}

void setRightMotor(int16_t speed) {
  rightSpeed = constrain(speed, -PWM_MAX, PWM_MAX);
  applyIBT2(RIGHT_RPWM, RIGHT_LPWM, rightSpeed);
}

void applyIBT2(uint8_t rpwm, uint8_t lpwm, int16_t speed) {
  if (abs(speed) < PWM_DEADBAND) {
    analogWrite(rpwm, 0);
    analogWrite(lpwm, 0);
    return;
  }
  if (speed > 0) {
    analogWrite(rpwm, (uint8_t)speed);
    analogWrite(lpwm, 0);
  } else {
    analogWrite(rpwm, 0);
    analogWrite(lpwm, (uint8_t)(-speed));
  }
}

void motorsStop() {
  applyIBT2(LEFT_RPWM,  LEFT_LPWM,  0);
  applyIBT2(RIGHT_RPWM, RIGHT_LPWM, 0);
  leftSpeed = 0;
  rightSpeed = 0;
}

// ── Differential drive helpers ────────────────────────────────
//   throttle: -255…+255 (forward/back)
//   steering: -255…+255 (left/right)
void setDrive(int16_t throttle, int16_t steering) {
  int16_t L = constrain(throttle + steering, -PWM_MAX, PWM_MAX);
  int16_t R = constrain(throttle - steering, -PWM_MAX, PWM_MAX);
  setLeftMotor(L);
  setRightMotor(R);
}

// ============================================================
//  Safety watchdog
// ============================================================
void checkWatchdog() {
  if (millis() - lastCmdMs > CMD_TIMEOUT_MS) {
    motorsStop();
  }
}

// ============================================================
//  UDP command parsing  (JSON)
//
//  Expected packet format (matches CoPilot Android app):
//  { "type":"CMD", "throttle":<-255..255>, "steering":<-255..255> }
//  or
//  { "type":"HELLO" }
//  or
//  { "type":"STOP" }
// ============================================================
void readUdpCommands() {
  int pktSize = udp.parsePacket();
  if (pktSize <= 0) return;

  remoteIP    = udp.remoteIP();
  remoteKnown = true;

  int len = udp.read(rxBuf, sizeof(rxBuf) - 1);
  if (len <= 0) return;
  rxBuf[len] = '\0';

  // -- very lightweight JSON field extraction --
  // type
  char typeVal[16] = "";
  extractJsonStr(rxBuf, "type", typeVal, sizeof(typeVal));

  if (strcmp(typeVal, "HELLO") == 0) {
    lastCmdMs = millis();
    sendAck("HELLO_ACK");
    return;
  }

  if (strcmp(typeVal, "STOP") == 0) {
    motorsStop();
    lastCmdMs = millis();
    return;
  }

  if (strcmp(typeVal, "CMD") == 0) {
    int16_t thr = (int16_t)extractJsonInt(rxBuf, "throttle");
    int16_t str = (int16_t)extractJsonInt(rxBuf, "steering");
    setDrive(thr, str);
    lastCmdMs = millis();
    return;
  }
}

// ============================================================
//  Telemetry sender  (JSON → UDP port 5005)
// ============================================================
void sendTelemetry() {
  if (!remoteKnown) return;

  char buf[320];
  snprintf(buf, sizeof(buf),
    "{\"type\":\"TELEM\","
    "\"lat\":%.8f,"
    "\"lon\":%.8f,"
    "\"alt\":%.2f,"
    "\"rtk\":%u,"
    "\"sats\":%u,"
    "\"hdop\":%.1f,"
    "\"spd\":%.2f,"
    "\"cog\":%.1f,"
    "\"leftPWM\":%d,"
    "\"rightPWM\":%d}",
    gps.lat, gps.lon, gps.altM,
    gps.rtkType, gps.satellites, gps.hdop,
    gps.speedKnots, gps.courseDeg,
    leftSpeed, rightSpeed
  );

  udp.beginPacket(remoteIP, PORT_TELEM);
  udp.write((uint8_t*)buf, strlen(buf));
  udp.endPacket();
}

void sendAck(const char* msg) {
  if (!remoteKnown) return;
  char buf[64];
  snprintf(buf, sizeof(buf), "{\"type\":\"%s\"}", msg);
  udp.beginPacket(remoteIP, PORT_TELEM);
  udp.write((uint8_t*)buf, strlen(buf));
  udp.endPacket();
}

// ============================================================
//  GPS update — SparkFun u-blox GNSS library (UBX PVT)
//
//  gnss.getPVT() returns true when a fresh navigation solution
//  is available (fires at the rate set by setNavigationFrequency).
//  All fields come pre-parsed — no NMEA string handling needed.
//
//  rtkType values from getCarrierSolutionType():
//    0 = no RTK
//    1 = RTK float  (sub-metre)
//    2 = RTK fixed  (centimetre)
// ============================================================
void updateGps() {
  if (!gnss.getPVT()) return;   // no fresh fix yet this cycle

  gps.lat        = gnss.getLatitude()  / 1e7;   // degrees×10⁷ → degrees
  gps.lon        = gnss.getLongitude() / 1e7;
  gps.altM       = gnss.getAltitudeMSL() / 1000.0;  // mm → m
  gps.hdop       = gnss.getHorizontalDOP() / 100.0;  // hdop×100 → hdop
  gps.speedKnots = gnss.getGroundSpeed() / 1000.0 * 1.94384;  // mm/s → knots
  gps.courseDeg  = gnss.getHeading() / 1e5;     // degrees×10⁵ → degrees
  gps.rtkType    = gnss.getCarrierSolutionType();  // 0/1/2
  gps.satellites = gnss.getSIV();
  gps.valid      = gnss.getGnssFixOk();
}

// ============================================================
//  Minimal JSON helpers (no library dependency)
// ============================================================

// Extract a string value: "key":"value"
void extractJsonStr(const char* json, const char* key, char* out, uint8_t outLen) {
  char needle[32];
  snprintf(needle, sizeof(needle), "\"%s\":\"", key);
  const char* p = strstr(json, needle);
  if (!p) { out[0] = '\0'; return; }
  p += strlen(needle);
  uint8_t i = 0;
  while (*p && *p != '"' && i < outLen - 1) out[i++] = *p++;
  out[i] = '\0';
}

// Extract an integer value: "key":number
long extractJsonInt(const char* json, const char* key) {
  char needle[32];
  snprintf(needle, sizeof(needle), "\"%s\":", key);
  const char* p = strstr(json, needle);
  if (!p) return 0;
  p += strlen(needle);
  return strtol(p, nullptr, 10);
}
