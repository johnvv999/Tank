// ============================================================
//  TankTest.ino
//  Sequential drive test: 5s forward, 5s backward, 5s spin
//  Arduino UNO R4 WiFi · Dual IBT-2 motor drivers
//
//  OTA via JAndrassy ArduinoOTA library (InternalStorage)
//  LED status via ArduinoGraphics + Arduino_LED_Matrix
// ============================================================

#include <WiFiS3.h>
#include <ArduinoOTA.h>           // JAndrassy library
#include <ArduinoGraphics.h>      // required for text/scroll methods
#include <Arduino_LED_Matrix.h>

// ── Home WiFi credentials ────────────────────────────────────
const char* HOME_SSID     = "Dexter5G";
const char* HOME_PASSWORD = "maxmaddie";

// ── OTA settings ─────────────────────────────────────────────
const char* OTA_NAME     = "TankTest";
const char* OTA_PASSWORD = "";

// ── IBT-2 pin assignments ────────────────────────────────────
const int L_RPWM = 5;   // Left motor
const int L_LPWM = 6;
const int R_RPWM = 9;   // Right motor
const int R_LPWM = 10;

// ── Test speed (0–255) ───────────────────────────────────────
const int SPEED = 100;  // ~70% — safe starting point

// ── Globals ──────────────────────────────────────────────────
ArduinoLEDMatrix matrix;

// ────────────────────────────────────────────────────────────
//  LED matrix — scrolls a short status string
// ────────────────────────────────────────────────────────────
void showStatus(const char* msg) {
  matrix.beginDraw();
  matrix.stroke(0xFFFFFFFF);
  matrix.textScrollSpeed(150);
  matrix.textFont(Font_5x7);
  matrix.beginText(0, 1, 0xFFFFFF);
  matrix.println(msg);
  matrix.endText(SCROLL_LEFT);
  matrix.endDraw();
}

// ────────────────────────────────────────────────────────────
//  Motor helpers
// ────────────────────────────────────────────────────────────
void motorLeft(int speed) {
  if (speed > 0) {
    analogWrite(L_RPWM, speed);
    analogWrite(L_LPWM, 0);
  } else if (speed < 0) {
    analogWrite(L_RPWM, 0);
    analogWrite(L_LPWM, -speed);
  } else {
    analogWrite(L_RPWM, 0);
    analogWrite(L_LPWM, 0);
  }
}

void motorRight(int speed) {
  if (speed > 0) {
    analogWrite(R_RPWM, speed);
    analogWrite(R_LPWM, 0);
  } else if (speed < 0) {
    analogWrite(R_RPWM, 0);
    analogWrite(R_LPWM, -speed);
  } else {
    analogWrite(R_RPWM, 0);
    analogWrite(R_LPWM, 0);
  }
}

void stopAll() {
  motorLeft(0);
  motorRight(0);
}

// ────────────────────────────────────────────────────────────
//  Blocking delay that keeps OTA poll alive
// ────────────────────────────────────────────────────────────
void waitWithOTA(unsigned long ms) {
  unsigned long start = millis();
  while (millis() - start < ms) {
    ArduinoOTA.poll();
    delay(10);
  }
}

// ────────────────────────────────────────────────────────────
//  Setup
// ────────────────────────────────────────────────────────────
void setup() {
  Serial.begin(115200);

  // Motor pins
  pinMode(L_RPWM, OUTPUT);
  pinMode(L_LPWM, OUTPUT);
  pinMode(R_RPWM, OUTPUT);
  pinMode(R_LPWM, OUTPUT);
  stopAll();

  // LED matrix
  matrix.begin();
}
/*
//  Connect to home WiFi
  Serial.print("Connecting to home WiFi");
  showStatus("WiFi");

  WiFi.begin(HOME_SSID, HOME_PASSWORD);
  unsigned long start = millis();
  while (WiFi.status() != WL_CONNECTED && millis() - start < 15000) {
    delay(500);
    Serial.print(".");
 }

   Wait for valid IP address
    start = millis();
    while (WiFi.localIP() == IPAddress(0, 0, 0, 0) && millis() - start < 3000) {
    delay(500);
    Serial.print(".");
  }

// Serial.println("\nConnected! IP: " + WiFi.localIP().toString());

  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("\nHome WiFi unreachable — OTA unavailable.");
    showStatus("NWIFI");
    delay(3000);
  } else {
    Serial.println("\nConnected! IP: " + WiFi.localIP().toString());
    delay(3000);
    // JAndrassy ArduinoOTA — 4-argument begin() for R4

    ArduinoOTA.begin(WiFi.localIP(), OTA_NAME, "", InternalStorage);

    Serial.println("OTA ready.");
    showStatus("RDY");
    delay(3000);
  }

  showStatus("TEST");
  delay(1000);


ArduinoOTA.begin(WiFi.localIP(), "TankTest", "password", InternalStorage);


}
*/

// ────────────────────────────────────────────────────────────
//  Main test loop
// ────────────────────────────────────────────────────────────
void loop() {
  ArduinoOTA.poll();

  // Forward
  Serial.println("FORWARD");
  showStatus("FWD");
  motorLeft(SPEED);
  motorRight(SPEED);
  waitWithOTA(5000);

  stopAll();
  waitWithOTA(500);

  // Backward
  Serial.println("BACKWARD");
  showStatus("REV");
  motorLeft(-SPEED);
  motorRight(-SPEED);
  waitWithOTA(5000);

  stopAll();
  waitWithOTA(500);

  // Spin left (left track back, right track forward)
  Serial.println("SPIN");
  showStatus("SPIN");
  motorLeft(-SPEED);
  motorRight(SPEED);
  waitWithOTA(5000);

  stopAll();
  Serial.println("--- cycle complete ---");
  showStatus("WAIT");
  waitWithOTA(2000);
}
