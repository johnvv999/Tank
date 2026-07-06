package com.rcdriving.tankrtk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = TankViewModel()

        setContent {

            // ------------------------------------------------------------
            // GLOBAL APP STATE
            // ------------------------------------------------------------
            var connected by remember { mutableStateOf(false) }
            var currentTab by remember { mutableStateOf(TopTab.MAIN) }

            // ------------------------------------------------------------
            // ROOT CONTAINER
            // ------------------------------------------------------------
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {

                // ------------------------------------------------------------
                // SCREEN SWITCHER
                // ------------------------------------------------------------
                when (currentTab) {

                    TopTab.MAIN -> TankDriveScreen(
                        viewModel = viewModel,
                        connected = connected,
                        onMain = { currentTab = TopTab.MAIN },
                        onRecord = { currentTab = TopTab.RECORD },
                        onSettings = { currentTab = TopTab.SETTINGS }
                    )

                    TopTab.RECORD -> RecordScreen(
                        viewModel = viewModel,
                        connected = connected,
                        onMain = { currentTab = TopTab.MAIN },
                        onRecord = { currentTab = TopTab.RECORD },
                        onSettings = { currentTab = TopTab.SETTINGS }
                    )

                    TopTab.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        connected = connected,
                        onMain = { currentTab = TopTab.MAIN },
                        onRecord = { currentTab = TopTab.RECORD },
                        onSettings = { currentTab = TopTab.SETTINGS }   // ⭐ FIXED
                    )
                }
            }
        }
    }
}
