// ============================================================
//  TankMotorTest.ino
//  Standalone motor test for the dual IBT-2 tank drive.
//  No WiFi / networking — just drives the motors directly and
//  prints each command to Serial. Useful for verifying motor
//  wiring/direction independent of the WiFi control chain.
//
//  Cycle (repeats forever):
//    3s forward       (L+40% R+40%)
//    1s pause (stopped)
//    3s backward      (L-40% R-40%)
//    1s pause (stopped)
//    3s spin in place (L+40% R-40%)
//    1s pause (stopped)
// ============================================================

// ── IBT-2 pin assignments (same as TankArduino.ino) ──────────
const uint8_t LEFT_RPWM  = 5;   // left  forward PWM
const uint8_t LEFT_LPWM  = 6;   // left  reverse PWM
const uint8_t RIGHT_RPWM = 9;   // right forward PWM
const uint8_t RIGHT_LPWM = 10;  // right reverse PWM

const uint8_t  PWM_MAX      = 255;
const uint8_t  PWM_DEADBAND = 10;   // below this → coast

// ── Test parameters ───────────────────────────────────────────
const int16_t  TEST_SPEED = (int16_t)(0.40f * PWM_MAX);   // 40% constant speed = 102
const uint32_t PHASE_MS   = 3000;                         // each move lasts 3s
const uint32_t PAUSE_MS   = 1000;                         // 1s stopped between moves

// ============================================================
//  Setup
// ============================================================
void setup() {
  Serial.begin(115200);
  delay(1500);   // give the Serial Monitor a moment to connect

  pinMode(LEFT_RPWM,  OUTPUT);
  pinMode(LEFT_LPWM,  OUTPUT);
  pinMode(RIGHT_RPWM, OUTPUT);
  pinMode(RIGHT_LPWM, OUTPUT);
  motorsStop();

  Serial.println("TankMotorTest ready — forward / backward / spin, repeating.");
}

// ============================================================
//  Loop
// ============================================================
void loop() {
  runPhase("FORWARD",        TEST_SPEED,  TEST_SPEED);
  pauseStopped();

  runPhase("BACKWARD",      -TEST_SPEED, -TEST_SPEED);
  pauseStopped();

  runPhase("SPIN IN PLACE",  TEST_SPEED, -TEST_SPEED);
  pauseStopped();
}

// ── Run one phase: apply L/R, print the command, hold for PHASE_MS ──
void runPhase(const char* label, int16_t left, int16_t right) {
  setLeftMotor(left);
  setRightMotor(right);

  Serial.print(label);
  Serial.print(" -> L");
  Serial.print(left);
  Serial.print(" R");
  Serial.println(right);

  delay(PHASE_MS);
  motorsStop();
}

// ── Stop and pause between phases ─────────────────────────────
void pauseStopped() {
  Serial.println("-- pause --");
  delay(PAUSE_MS);
}

// ============================================================
//  Motor helpers (same behavior as TankArduino.ino)
// ============================================================
void setLeftMotor(int16_t speed) {
  applyIBT2(LEFT_RPWM, LEFT_LPWM, (int16_t)constrain(speed, -PWM_MAX, PWM_MAX));
}

void setRightMotor(int16_t speed) {
  applyIBT2(RIGHT_RPWM, RIGHT_LPWM, (int16_t)constrain(speed, -PWM_MAX, PWM_MAX));
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
}
