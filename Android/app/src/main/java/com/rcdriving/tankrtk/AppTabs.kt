package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

enum class AppTab { MAIN, RECORD, SETTINGS }

@Composable
fun AppTabs(
    activeTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    mainContent: @Composable () -> Unit,
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
                selected = activeTab == AppTab.MAIN,
                onClick = { onTabSelected(AppTab.MAIN) },
                text = { Text("MAIN") }
            )
            Tab(
                selected = activeTab == AppTab.RECORD,
                onClick = { onTabSelected(AppTab.RECORD) },
                text = { Text("RECORD") }
            )
            Tab(
                selected = activeTab == AppTab.SETTINGS,
                onClick = { onTabSelected(AppTab.SETTINGS) },
                text = { Text("SETTINGS") }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                AppTab.MAIN -> mainContent()
                AppTab.RECORD -> recordContent()
                AppTab.SETTINGS -> settingsContent()
            }
        }
    }
}
