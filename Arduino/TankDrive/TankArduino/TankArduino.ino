// ============================================================
//  TankCoPilot.ino
//  Dual-motor RC tank · Arduino UNO R4 WiFi
//  IBT-2 x2 (differential drive) + ZED-F9P RTK GPS (NMEA)
//
//  Commands: plain-text TCP, port 9000 — "L<val> R<val>\n"
//    (matches Android/app/.../TankWifiClient.kt + TankViewModel.kt)
//  Telemetry: JSON-over-UDP, port 5005 — sent to whichever
//    device is currently connected via TCP (not read by the
//    app today, but available for future use)
//
//  WiFi: board hosts its own access point.
//    Values below MUST match Android/app/.../Config.kt
// ============================================================

#include <WiFiS3.h>
#include <WiFiUdp.h>

// ── WiFi credentials (access point) ──────────────────────────
const char* AP_SSID     = "TankAP";
const char* AP_PASSWORD = "tankdrive";        // WPA2 requires 8+ characters
IPAddress   AP_IP(192, 168, 4, 1);            // matches Config.TANK_HOST

// ── TCP command server ───────────────────────────────────────
const uint16_t CMD_PORT = 9000;               // matches Config.TANK_PORT
WiFiServer cmdServer(CMD_PORT);
WiFiClient cmdClient;

char    lineBuf[64];
uint8_t lineIdx = 0;

// ── UDP telemetry (optional; sent to the connected TCP client) ─
const uint16_t PORT_TELEM = 5005;
WiFiUDP   telemUdp;
IPAddress remoteIP;
bool      remoteKnown = false;

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

// ── GPS / NMEA ───────────────────────────────────────────────
//   ZED-F9P TX → Arduino RX1 (Serial1)
//   Default baud 38400 for ZED-F9P; change to match u-center config
const uint32_t GPS_BAUD = 38400;

struct GpsState {
  double   lat       = 0.0;
  double   lon       = 0.0;
  double   altM      = 0.0;
  float    hdop      = 99.9f;
  float    speedKnots = 0.0f;
  float    courseDeg  = 0.0f;
  uint8_t  fixQuality = 0;   // 0=none,1=GPS,2=DGPS,4=RTK-fixed,5=RTK-float
  uint8_t  satellites = 0;
  bool     valid     = false;
  char     utcTime[12] = "";   // hhmmss.ss
} gps;

// ── Buffers ───────────────────────────────────────────────────
char nmeaBuf[128];
uint8_t nmeaIdx = 0;

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
  Serial1.begin(GPS_BAUD);   // ZED-F9P

  // IBT-2 pins
  pinMode(LEFT_RPWM,  OUTPUT);
  pinMode(LEFT_LPWM,  OUTPUT);
  pinMode(RIGHT_RPWM, OUTPUT);
  pinMode(RIGHT_LPWM, OUTPUT);
  motorsStop();

  // WiFi — host our own access point, matching Config.kt on the app side
  WiFi.config(AP_IP);
  Serial.print("Starting access point: ");
  Serial.println(AP_SSID);
  int apStatus = WiFi.beginAP(AP_SSID, AP_PASSWORD);
  if (apStatus != WL_AP_LISTENING) {
    Serial.println("Failed to start access point! Halting.");
    while (true) delay(1000);
  }
  delay(1000);   // give the AP a moment to come fully online

  Serial.print("AP IP address: ");
  Serial.println(WiFi.localIP());

  cmdServer.begin();
  telemUdp.begin(PORT_TELEM);

  Serial.print("TankCoPilot ready — connect to WiFi \"");
  Serial.print(AP_SSID);
  Serial.print("\", then TCP to ");
  Serial.print(WiFi.localIP());
  Serial.print(":");
  Serial.println(CMD_PORT);
}

// ============================================================
//  Loop
// ============================================================
void loop() {
  readGps();
  acceptClient();
  readTcpCommands();
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

// ============================================================
//  Client connection management
// ============================================================
void acceptClient() {
  // Drop a client that has disconnected
  if (cmdClient && !cmdClient.connected()) {
    Serial.println("Client disconnected");
    motorsStop();
    cmdClient.stop();
  }

  if (!cmdClient || !cmdClient.connected()) {
    WiFiClient newClient = cmdServer.available();
    if (newClient) {
      cmdClient   = newClient;
      remoteIP    = cmdClient.remoteIP();
      remoteKnown = true;
      lastCmdMs   = millis();
      lineIdx     = 0;
      Serial.print("Client connected: ");
      Serial.println(remoteIP);
    }
  }
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
//  TCP command parsing
//
//  Expected line format (matches TankViewModel.kt / formatTankCommand):
//    "L<val> R<val>\n"   e.g.  "L0 R0\n"  or  "L-60 R80\n"
//  <val> is a signed PERCENT value in range -100..100 (the app's
//  arcade-mix output), scaled up to PWM range in percentToPwm() below.
// ============================================================
void readTcpCommands() {
  if (!cmdClient || !cmdClient.connected()) return;

  while (cmdClient.available()) {
    char c = (char)cmdClient.read();

    if (c == '\n' || c == '\r') {
      if (lineIdx > 0) {
        lineBuf[lineIdx] = '\0';
        parseCommandLine(lineBuf);
        lineIdx = 0;
      }
      continue;
    }

    if (lineIdx < sizeof(lineBuf) - 1) {
      lineBuf[lineIdx++] = c;
    } else {
      lineIdx = 0;   // overflow — discard malformed line
    }
  }
}

void parseCommandLine(const char* line) {
  Serial.print("RX: [");
  Serial.print(line);
  Serial.println("]");

  const char* lp = strchr(line, 'L');
  const char* rp = strchr(line, 'R');
  if (!lp || !rp) {
    Serial.println("  -> malformed, ignored");
    return;
  }

  // The app sends percent values (-100..+100 — see TankViewModel.kt's
  // computeMotorOutputs, which clamps to that range), not raw PWM. Scale
  // up to the full -255..+255 PWM range here so "100%" actually reaches
  // full motor power. Without this scaling, every command topped out at
  // ~39% duty cycle (100/255) — fine for driving straight, but weak
  // enough that a diagonal turn's already-reduced inner-track value
  // (e.g. ~37% of that already-capped range) could drop below the
  // motors' real-world stall torque and barely move at all.
  int16_t lPercent = (int16_t)atoi(lp + 1);
  int16_t rPercent = (int16_t)atoi(rp + 1);

  setLeftMotor(percentToPwm(lPercent));
  setRightMotor(percentToPwm(rPercent));
  lastCmdMs = millis();
}

// Converts a -100..+100 percent command from the app into a -255..+255
// PWM value for the motor drivers.
int16_t percentToPwm(int16_t percent) {
  int32_t pwm = ((int32_t)percent * PWM_MAX) / 100;
  return (int16_t)constrain(pwm, -PWM_MAX, PWM_MAX);
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
    "\"fix\":%u,"
    "\"sats\":%u,"
    "\"hdop\":%.1f,"
    "\"spd\":%.2f,"
    "\"cog\":%.1f,"
    "\"utc\":\"%s\","
    "\"leftPWM\":%d,"
    "\"rightPWM\":%d}",
    gps.lat, gps.lon, gps.altM,
    gps.fixQuality, gps.satellites, gps.hdop,
    gps.speedKnots, gps.courseDeg, gps.utcTime,
    leftSpeed, rightSpeed
  );

  telemUdp.beginPacket(remoteIP, PORT_TELEM);
  telemUdp.write((uint8_t*)buf, strlen(buf));
  telemUdp.endPacket();
}

// ============================================================
//  GPS / NMEA parser (ZED-F9P via Serial1)
//
//  Sentences parsed:
//    $GNGGA  – position, fix quality, HDOP, altitude
//    $GNRMC  – position, speed, course, date
//  ZED-F9P outputs GN-prefix talker for combined constellations.
// ============================================================
void readGps() {
  while (Serial1.available()) {
    char c = (char)Serial1.read();

    if (c == '$') {
      nmeaIdx = 0;
      nmeaBuf[nmeaIdx++] = c;
      return;
    }

    if (nmeaIdx == 0) return;   // waiting for start

    if (c == '\r') return;      // skip CR

    if (c == '\n') {
      nmeaBuf[nmeaIdx] = '\0';
      parseNmeaSentence(nmeaBuf);
      nmeaIdx = 0;
      return;
    }

    if (nmeaIdx < (uint8_t)(sizeof(nmeaBuf) - 1)) {
      nmeaBuf[nmeaIdx++] = c;
    } else {
      nmeaIdx = 0;   // overflow – discard
    }
  }
}

void parseNmeaSentence(const char* s) {
  if (!nmeaChecksum(s)) return;

  if (strncmp(s + 1, "GNGGA", 5) == 0 ||
      strncmp(s + 1, "GPGGA", 5) == 0) {
    parseGGA(s);
  } else if (strncmp(s + 1, "GNRMC", 5) == 0 ||
             strncmp(s + 1, "GPRMC", 5) == 0) {
    parseRMC(s);
  }
}

// ── GGA: $GNGGA,hhmmss.ss,lat,N,lon,E,fix,sats,hdop,alt,M,...*cs
void parseGGA(const char* s) {
  char fields[15][20];
  splitNmea(s, fields, 15);

  strncpy(gps.utcTime, fields[1], sizeof(gps.utcTime) - 1);
  gps.lat        = nmeaLatLon(fields[2], fields[3]);
  gps.lon        = nmeaLatLon(fields[4], fields[5]);
  gps.fixQuality = (uint8_t)atoi(fields[6]);
  gps.satellites = (uint8_t)atoi(fields[7]);
  gps.hdop       = atof(fields[8]);
  gps.altM       = atof(fields[9]);
  gps.valid      = (gps.fixQuality > 0);
}

// ── RMC: $GNRMC,hhmmss.ss,A,lat,N,lon,E,spd,cog,date,...*cs
void parseRMC(const char* s) {
  char fields[13][20];
  splitNmea(s, fields, 13);

  if (fields[2][0] != 'A') return;   // not Active
  gps.speedKnots = atof(fields[7]);
  gps.courseDeg  = atof(fields[8]);
}

// ── Convert NMEA ddmm.mmmm to decimal degrees
double nmeaLatLon(const char* val, const char* dir) {
  if (val[0] == '\0') return 0.0;
  double raw = atof(val);
  int deg  = (int)(raw / 100);
  double min = raw - deg * 100.0;
  double dd = deg + min / 60.0;
  if (dir[0] == 'S' || dir[0] == 'W') dd = -dd;
  return dd;
}

// ── Verify NMEA checksum  ($....*XY format)
bool nmeaChecksum(const char* s) {
  if (s[0] != '$') return false;
  const char* star = strchr(s, '*');
  if (!star || strlen(star) < 3) return false;

  uint8_t calc = 0;
  for (const char* p = s + 1; p < star; p++) calc ^= (uint8_t)*p;

  char hexCs[3] = { star[1], star[2], '\0' };
  uint8_t recv  = (uint8_t)strtol(hexCs, nullptr, 16);
  return calc == recv;
}

// ── Split comma-separated NMEA sentence into fields[]
void splitNmea(const char* s, char fields[][20], uint8_t maxFields) {
  uint8_t fi = 0;
  uint8_t ci = 0;
  const char* p = s;
  while (*p && fi < maxFields) {
    if (*p == ',' || *p == '*') {
      fields[fi][ci] = '\0';
      fi++;
      ci = 0;
    } else if (ci < 19) {
      fields[fi][ci++] = *p;
    }
    p++;
  }
  if (fi < maxFields) fields[fi][ci] = '\0';
  for (uint8_t i = fi + 1; i < maxFields; i++) fields[i][0] = '\0';
}
