package com.rcdriving.tankrtk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
            val status by viewModel.wifi.status.collectAsState()
            val connected = status == ConnectionStatus.CONNECTED

            var currentTab by remember { mutableStateOf(TopTab.MAIN) }

            // Try to connect to the tank AP on launch
            LaunchedEffect(Unit) {
                viewModel.connect()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {

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
                        onSettings = { currentTab = TopTab.SETTINGS }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
