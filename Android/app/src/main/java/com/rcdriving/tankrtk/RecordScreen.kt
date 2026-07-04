package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.math.cos

// TODO: replace with the exact coordinates of Coggins Street you want the map centered on.
// This is currently just Hilton Head Island generally.
private const val MAP_ORIGIN_LAT = 32.222424
private const val MAP_ORIGIN_LON = -80.676946

@Composable
fun RecordScreen(
    recordedPath: List<EnuPoint>,
    playbackPath: List<EnuPoint>,
    isRecording: Boolean,
    isPlaying: Boolean,
    onToggleRecord: () -> Unit,
    onTogglePlayback: () -> Unit,
    onClearPath: () -> Unit
) {
    val origin = LatLng(MAP_ORIGIN_LAT, MAP_ORIGIN_LON)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(origin, 19f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A2F0A))
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                if (recordedPath.size >= 2) {
                    Polyline(
                        points = recordedPath.map { enuToLatLng(it) },
                        color = Color.Red,
                        width = 8f
                    )
                }
                if (playbackPath.size >= 2) {
                    Polyline(
                        points = playbackPath.map { enuToLatLng(it) },
                        color = Color.Green,
                        width = 8f
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onToggleRecord,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) Color.Red else Color(0xFF145214)
                )
            ) {
                Text(if (isRecording) "STOP RECORDING" else "● RECORD")
            }

            Button(
                onClick = onTogglePlayback,
                enabled = recordedPath.size >= 2,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlaying) Color.Red else Color(0xFF145214)
                )
            ) {
                Text(if (isPlaying) "STOP PLAYBACK" else "▶ PLAY")
            }

            Button(
                onClick = onClearPath,
                enabled = !isRecording && !isPlaying,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("CLEAR")
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Recorded points: ${recordedPath.size}" +
                if (isPlaying) "   |   Playback: ${playbackPath.size}/${recordedPath.size}" else "",
            color = Color.White,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

// Flat-earth approximation: fine for a yard-sized dead-reckoning path, not for real distances.
private fun enuToLatLng(p: EnuPoint): LatLng {
    val metersPerDegLat = 111320.0
    val metersPerDegLon = 111320.0 * cos(Math.toRadians(MAP_ORIGIN_LAT))
    val lat = MAP_ORIGIN_LAT + (p.y / metersPerDegLat)
    val lon = MAP_ORIGIN_LON + (p.x / metersPerDegLon)
    return LatLng(lat, lon)
}
