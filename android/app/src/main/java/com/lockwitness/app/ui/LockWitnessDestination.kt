package com.lockwitness.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class LockWitnessDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    Dashboard("dashboard", "Dashboard", Icons.Outlined.Home),
    Settings("settings", "Settings", Icons.Outlined.Settings),
    History("history", "History", Icons.Outlined.History),
    About("about", "About", Icons.Outlined.Info)
}
