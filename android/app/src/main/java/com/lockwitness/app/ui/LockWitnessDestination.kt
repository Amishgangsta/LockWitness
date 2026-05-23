package com.lockwitness.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class LockWitnessDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    Dashboard("dashboard", "Home", Icons.Outlined.Home),
    History("history", "History", Icons.Outlined.History),
    Settings("settings", "Settings", Icons.Outlined.Settings),
    Diagnostics("diagnostics", "Diagnostic", Icons.Outlined.MonitorHeart),
    About("about", "About", Icons.Outlined.Info)
}

const val UPGRADE_ROUTE = "upgrade"
