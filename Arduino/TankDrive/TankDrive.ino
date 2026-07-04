// ============================================================
//  TankDrive.ino
//  Broadcasts its own WiFi access point and listens for
//  "L<val> R<val>\n" drive commands from the TankRTK Android app.
//  Arduino UNO R4 WiFi · Dual IBT-2 motor drivers
//
//  OTA via JAndrassy ArduinoOTA library (InternalStorage)
// ============================================================

#include <WiFiS3.h>
#include <ArduinoOTA.h>

// ── Access point settings ────────────────────────────────────
// Must match TANK_AP_SSID in the Android app's Config.kt
const char* AP_SSID     = "TankAP";
const char* AP_PASSWORD = "tankdrive";   // WPA2 needs 8+ characters

// ── TCP command server ───────────────────────────────────────
const int CMD_PORT = 9000;
WiFiServer server(CMD_PORT);
WiFiClient client;

// ── Safety watchdog ──────────────────────────────────────────
const unsigned long CMD_TIMEOUT_MS = 500;
unsigned long lastCommandMillis = 0;
bool wasClientConnected = false;

// ── OTA settings ─────────────────────────────────────────────
const char* OTA_NAME     = "TankDrive";
const char* OTA_PASSWORD = "password";

// ── IBT-2 pin assignments (same as TankTest.ino) ─────────────
const int L_RPWM = 5;
const int L_LPWM = 6;
const int R_RPWM = 9;
const int R_LPWM = 10;

// ────────────────────────────────────────────────────────────
//  Motor helpers
// ────────────────────────────────────────────────────────────
void motorLeft(int speed) {
  if (speed > 0) { analogWrite(L_RPWM, speed); analogWrite(L_LPWM, 0); }
  else if (speed < 0) { analogWrite(L_RPWM, 0); analogWrite(L_LPWM, -speed); }
  else { analogWrite(L_RPWM, 0); analogWrite(L_LPWM, 0); }
}

void motorRight(int speed) {
  if (speed > 0) { analogWrite(R_RPWM, speed); analogWrite(R_LPWM, 0); }
  else if (speed < 0) { analogWrite(R_RPWM, 0); analogWrite(R_LPWM, -speed); }
  else { analogWrite(R_RPWM, 0); analogWrite(R_LPWM, 0); }
}

void stopAll() {
  motorLeft(0);
  motorRight(0);
}

// ────────────────────────────────────────────────────────────
//  Parses "L<val> R<val>" text commands, e.g. "L75 R-40"
//  Values are -100..100 PWM percentages from the Android app.
// ────────────────────────────────────────────────────────────
void parseAndDrive(String line) {
  line.trim();
  int lIdx = line.indexOf('L');
  int rIdx = line.indexOf('R');
  if (lIdx == -1 || rIdx == -1 || rIdx <= lIdx) return;

  int leftPct  = line.substring(lIdx + 1, rIdx).toInt();
  int rightPct = line.substring(rIdx + 1).toInt();

  int leftPwm  = map(abs(leftPct), 0, 100, 0, 255);
  int rightPwm = map(abs(rightPct), 0, 100, 0, 255);

  motorLeft(leftPct >= 0 ? leftPwm : -leftPwm);
  motorRight(rightPct >= 0 ? rightPwm : -rightPwm);

  lastCommandMillis = millis();
}

// ────────────────────────────────────────────────────────────
//  Setup
// ────────────────────────────────────────────────────────────
void setup() {
  Serial.begin(115200);

  pinMode(L_RPWM, OUTPUT);
  pinMode(L_LPWM, OUTPUT);
  pinMode(R_RPWM, OUTPUT);
  pinMode(R_LPWM, OUTPUT);
  stopAll();

  Serial.println("Starting access point...");
  WiFi.config(IPAddress(192, 168, 4, 1));
  int status = WiFi.beginAP(AP_SSID, AP_PASSWORD);

  if (status != WL_AP_LISTENING) {
    Serial.println("Failed to start access point.");
    while (true) { delay(1000); }
  }

  Serial.print("Access point started: ");
  Serial.println(AP_SSID);
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());

  server.begin();

  ArduinoOTA.begin(WiFi.localIP(), OTA_NAME, OTA_PASSWORD, InternalStorage);
  Serial.println("OTA ready.");

  lastCommandMillis = millis();
}

// ────────────────────────────────────────────────────────────
//  Main loop
// ────────────────────────────────────────────────────────────
void loop() {
  ArduinoOTA.poll();

  // Safety: if the access point itself has dropped (WiFi hardware fault,
  // AP crash, etc.), OTA and command control both go down silently with
  // it — stop immediately rather than spinning blind.
  if (WiFi.status() != WL_AP_LISTENING) {
    stopAll();
    return;
  }

  if (!client || !client.connected()) {