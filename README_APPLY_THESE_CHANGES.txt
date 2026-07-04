HOW TO APPLY THIS UPDATE
=========================

This zip contains only the files that changed in this round (octagon STOP button,
Home screen with Record/Settings tabs, path recording + playback, landscape lock).

1. Extract this zip.
2. Copy the "Android" folder from the extracted zip directly over your existing
   E:\Tank\Android folder, allowing it to overwrite files with the same path.

   Example (from PowerShell, adjust the extracted path as needed):
     robocopy "C:\path\to\extracted\Android" "E:\Tank\Android" /E

   Or just drag-and-drop merge in File Explorer, choosing "Replace the files in
   the destination" when prompted.

3. Files included (all under Android\app\src\main\...):
   - AndroidManifest.xml                                   (replace)
   - java\com\rcdriving\tankrtk\OdometryTracker.kt          (new)
   - java\PathReplayer.kt                                   (replace)
   - java\TankViewModel.kt                                  (replace)
   - java\com\rcdriving\tankrtk\TankDriveScreen.kt           (replace)
   - java\com\rcdriving\tankrtk\HomeScreen.kt                (new)
   - java\com\rcdriving\tankrtk\SettingsScreen.kt            (replace)
   - java\com\rcdriving\tankrtk\RecordScreen.kt              (new)
   - java\com\rcdriving\tankrtk\MainActivity.kt              (replace)

   Note: TankViewModel.kt and PathReplayer.kt land in the loose "java\" folder
   (not the "java\com\rcdriving\tankrtk\" subfolder) to match where your existing
   copies of those two files currently live in the repo.

4. Files NOT touched this round (still correct from earlier fixes, left alone):
   - Joystick.kt
   - TankControlUtils.kt
   - TankWifiClient.kt
   - ConnectionStatus.kt

5. After copying, in Android Studio: File -> Sync Project with Gradle Files,
   then rebuild.

6. Once it builds and looks right on your S20, commit and push:
     cd E:\Tank
     git add -A
     git commit -m "Octagon stop button, home tabs, path record/playback, landscape lock"
     git push

DESIGN NOTES (worth rereading before testing)
----------------------------------------------
- The Record screen's path is built from dead-reckoning odometry (your own
  joystick output integrated over time), NOT real GPS. It will drift over
  longer runs. Real RTK/GPS wiring (RtkFix/PathRecorder) is still unused
  scaffolding for a future upgrade.
- TURBO/TURTLE and the 4-level SPEED setting both apply to joystick scaling
  and stack multiplicatively (e.g. TURTLE + Speed 1 = very slow). Say if you
  want them to be mutually exclusive instead.
- The STOP octagon is positioned via Alignment.TopEnd + a manual offset
  (x=-8dp, y=-56dp) over the Turbo/Turtle row. Nudge those two numbers if it's
  not exactly where you want it once you see it on-device.
