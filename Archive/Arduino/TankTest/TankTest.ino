// TankTest.ino
// Sequential drive test: 5s forward, 5s backward, 5s spin (left)
// IBT-2 motor drivers — Left motor on pins 3/5, Right motor on pins 6/9
#include <WiFiS3.h>
#include <ArduinoOTA.h>
#include "Arduino_LED_Matrix.h"

// Your home network credentials
const char* HOME_SSID     = "YourHomeSSID";
const char* HOME_PASSWORD = "YourHomePassword";

// OTA window in milliseconds
const unsigned long OTA_WINDOW_MS = 15000;

void setupOTA() {
  Serial.print("Connecting to home WiFi for OTA window");
  WiFi.begin(HOME_SSID, HOME_PASSWORD);

  unsigned long start = millis();
  while (WiFi.status() != WL_CONNECTED && millis() - start < 10000) {
    delay(500);
    Serial.print(".");
  }

  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("\nHome WiFi not reachable — skipping OTA window.");
    return;
  }

  Serial.println("\nConnected! IP: " + WiFi.localIP().toString());

  ArduinoOTA.setHostname("TankCoPilot"); // shows up in Arduino IDE port list
  ArduinoOTA.setPassword("ota_secret");  // optional but recommended

  ArduinoOTA.onStart([]() { Serial.println("OTA Start"); });
  ArduinoOTA.onEnd([]()   { Serial.println("OTA End");   });
  ArduinoOTA.onError([](ota_error_t e) {
    Serial.printf("OTA Error[%u]\n", e);
  });

  ArduinoOTA.begin();
  Serial.println("OTA ready. Waiting " + String(OTA_WINDOW_MS / 1000) + "s...");

  // Poll for OTA_WINDOW_MS, then fall through to AP mode
  start = millis();
  while (millis() - start < OTA_WINDOW_MS) {
    ArduinoOTA.handle();
    delay(10);
  }

  Serial.println("OTA window closed. Switching to AP mode.");
  WiFi.disconnect();
  delay(500);
}


// ── Adjust this to set speed (0–255) ──────────────────────────────────────
const int SPEED = 30;
// ──────────────────────────────────────────────────────────────────────────

// Left IBT-2
const int L_RPWM = 5;
const int L_LPWM = 6;

// Right IBT-2
const int R_RPWM = 9;
const int R_LPWM = 10;

ArduinoLEDMatrix matrix;
void otaStatus(const char* msg) {
  matrix.beginDraw();
  matrix.stroke(0xFFFFFFFF);
  matrix.textScrollSpeed(50);
  matrix.textFont(Font_5x7);
  matrix.beginText(0, 1, 0xFFFFFF);
  matrix.println(msg);
  matrix.endText(SCROLL_LEFT);
  matrix.endDraw();
}

void setup() {
  pinMode(L_RPWM, OUTPUT);
  pinMode(L_LPWM, OUTPUT);
  pinMode(R_RPWM, OUTPUT);
  pinMode(R_LPWM, OUTPUT);

  Serial.begin(115200);
  Serial.println("Tank test starting...");
  delay(1000);
  setupOTA();       // <-- OTA window first
  setupAPMode();    // <-- then your normal AP + application init


}

void motorLeft(int speed) {
  // speed: positive = forward, negative = backward, 0 = stop
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

void loop() {
  // Forward
  Serial.println("FORWARD");
  motorLeft(SPEED);
  motorRight(SPEED);
  delay(5000);

  stopAll();
  delay(500);

  // Backward
  Serial.println("BACKWARD");
  motorLeft(-SPEED);
  motorRight(-SPEED);
  delay(5000);

  stopAll();
  delay(500);

  // Spin left (left track back, right track forward)
  Serial.println("SPIN");
  motorLeft(-SPEED);
  motorRight(SPEED);
  delay(5000);

  stopAll();
  Serial.println("--- cycle complete ---");
  delay(2000);
}
