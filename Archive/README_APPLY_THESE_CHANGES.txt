HOW TO APPLY THIS UPDATE
=========================

1. Extract this zip.
2. Copy the "Android" folder from the extracted zip directly over your existing
   E:\Tank\Android folder, overwriting files with the same path.
3. IMPORTANT — one manual step before this will build: you need a Google Maps
   API key (see below). The app will not compile/run correctly without it.
4. DELETE the old HomeScreen.kt file — it's fully replaced by AppTabs.kt:
     E:\Tank\Android\app\src\main\java\com\rcdriving\tankrtk\HomeScreen.kt
5. Sync Gradle, rebuild.
6. Test on your S20, then commit and push.

GETTING A GOOGLE MAPS API KEY (required, one-time)
----------------------------------------------------
1. Go to https://console.cloud.google.com/
2. Create a project (or use an existing one).
3. Under "APIs & Services" -> "Library", search for and enable:
     "Maps SDK for Android"
4. You'll be prompted to enable billing on the project. Google requires a
   card on file, but there's a substantial free monthly usage tier — normal
   personal/hobby use like this won't incur charges.
5. Go to "APIs & Services" -> "Credentials" -> "Create Credentials" -> "API key".
6. Copy the generated key.
7. Open Android\app\src\main\AndroidManifest.xml in this update and replace:
     YOUR_GOOGLE_MAPS_API_KEY_HERE
   with your actual key.
8. (Recommended) Click "Restrict Key" on the credentials page and restrict it
   to "Android apps", adding your app's package name (com.rcdriving.tankrtk)
   and SHA-1 fingerprint, so the key can't be used by anyone else if leaked.

SETTING THE MAP LOCATION (Coggins Street)
-------------------------------------------
Open Android\app\src\main\java\com\rcdriving\tankrtk\RecordScreen.kt and find:

    private const val MAP_ORIGIN_LAT = 32.2163
    private const val MAP_ORIGIN_LON = -80.7526

These are currently just general Hilton Head Island coordinates. Replace them
with the exact latitude/longitude of the spot on Coggins Street you want the
map centered on and zoomed into. Easiest way to get exact coordinates: open
Google Maps on your phone/computer, long-press the exact spot, and it will
show you the lat/lon to copy.

The zoom level is set in the same file:
    position = CameraPosition.fromLatLngZoom(origin, 19f)
Higher numbers = closer zoom. 19-20 is roughly street/yard level.

FILES INCLUDED IN THIS ZIP
----------------------------
- AndroidManifest.xml                                        (replace)
- gradle/libs.versions.toml                                  (replace)
- app/build.gradle.kts                                       (replace)
- java/com/rcdriving/tankrtk/AppTabs.kt                       (new — replaces HomeScreen.kt, DELETE HomeScreen.kt)
- java/com/rcdriving/tankrtk/TankDriveScreen.kt               (replace)
- java/com/rcdriving/tankrtk/RecordScreen.kt                  (replace — now a real Google Map)
- java/com/rcdriving/tankrtk/SettingsScreen.kt                (replace — trim/speed buttons confirmed present)
- java/com/rcdriving/tankrtk/MainActivity.kt                  (replace)

NOT touched this round (still correct from earlier, left alone):
- Joystick.kt, TankControlUtils.kt, TankWifiClient.kt, ConnectionStatus.kt,
  TankViewModel.kt, PathReplayer.kt, OdometryTracker.kt

WHAT CHANGED, AND WHY
------------------------
- Navigation is now a single top TabRow: MAIN | RECORD | SETTINGS, always
  visible. The old gear-icon -> Home -> tabs -> bottom "Return to Main"
  button flow is gone entirely, replaced by this simpler structure.
- TURBO/TURTLE is now one button that toggles between the two states,
  instead of two separate buttons.
- The STOP octagon's upward offset was halved (from -56dp to -28dp), moving
  it closer to the Turbo/Turtle button. Adjust the offset numbers in
  TankDriveScreen.kt if it's still not quite where you want it.
- Trim and Speed buttons remain on the Settings tab (unchanged) — reachable
  directly via the top tab bar now. If you actually wanted them duplicated
  onto the Main/drive screen as well, let me know.
- Record screen now shows a real Google Map (satellite/street view of
  Hilton Head Island, zoomable/pannable) instead of a plain custom-drawn
  canvas. The red recorded path and green playback path are drawn as
  polylines on top of the map.
- Recorded path positions are still derived from dead-reckoning odometry
  (your own joystick output integrated over time), not real GPS — converted
  to approximate lat/lon around the map's center point using a flat-earth
  approximation. This is fine for a short yard-sized path but will drift
  over longer/faster runs. Real RTK/GPS integration (RtkFix/PathRecorder)
  is still unused scaffolding for a future upgrade.
