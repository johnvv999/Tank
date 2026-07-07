package com.rcdriving.tankrtk

// Values here MUST match Arduino/TankDrive/TankDrive.ino on the tank.
object Config {
    // Firmware: const char* AP_SSID = "TankAP";
    const val TANK_AP_SSID = "TankAP"

    // Firmware: const char* AP_PASSWORD = "tankdrive";
    const val TANK_AP_PASSWORD = "tankdrive"

    // Firmware: WiFi.config(IPAddress(192, 168, 4, 1));
    const val TANK_HOST = "192.168.4.1"

    // Firmware: const int CMD_PORT = 9000;
    const val TANK_PORT = 9000
}
