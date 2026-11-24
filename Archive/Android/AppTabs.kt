package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(Color(0xFF0F3D0F))
        ) {
            ThinTab("MAIN", activeTab == AppTab.MAIN, Modifier.weight(1f)) { onTabSelected(AppTab.MAIN) }
            ThinTab("RECORD", activeTab == AppTab.RECORD, Modifier.weight(1f)) { onTabSelected(AppTab.RECORD) }
            ThinTab("SETTINGS", activeTab == AppTab.SETTINGS, Modifier.weight(1f)) { onTabSelected(AppTab.SETTINGS) }
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

@Composable
private fun ThinTab(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(if (selected) Color(0xFF2E7D32) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 11.sp)
    }
}
