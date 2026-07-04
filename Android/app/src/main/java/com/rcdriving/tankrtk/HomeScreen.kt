package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class HomeTab { RECORD, SETTINGS }

@Composable
fun HomeScreen(
    activeTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    onReturnToMain: () -> Unit,
    recordContent: @Composable () -> Unit,
    settingsContent: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A2F0A))
    ) {
        TabRow(selectedTabIndex = activeTab.ordinal) {
            Tab(
                selected = activeTab == HomeTab.RECORD,
                onClick = { onTabSelected(HomeTab.RECORD) },
                text = { Text("RECORD") }
            )
            Tab(
                selected = activeTab == HomeTab.SETTINGS,
                onClick = { onTabSelected(HomeTab.SETTINGS) },
                text = { Text("SETTINGS") }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                HomeTab.RECORD -> recordContent()
                HomeTab.SETTINGS -> settingsContent()
            }
        }

        Button(
            onClick = onReturnToMain,
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text("RETURN TO MAIN")
        }
    }
}
