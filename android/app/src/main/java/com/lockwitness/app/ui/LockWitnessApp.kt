package com.lockwitness.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lockwitness.app.ui.screens.AboutScreen
import com.lockwitness.app.ui.screens.DashboardScreen
import com.lockwitness.app.ui.screens.DiagnosticsScreen
import com.lockwitness.app.ui.screens.HistoryScreen
import com.lockwitness.app.ui.screens.SettingsScreen
import com.lockwitness.app.ui.screens.UpgradeScreen
import com.lockwitness.app.ui.theme.LWBackground
import com.lockwitness.app.ui.theme.LWPanel
import com.lockwitness.app.ui.theme.LockWitnessPrimary
import com.lockwitness.app.ui.theme.LockWitnessTextSecondary

@Composable
fun LockWitnessApp() {
    val navController = rememberNavController()
    val screens = LockWitnessDestination.entries
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route ?: LockWitnessDestination.Dashboard.route

    Scaffold(
        containerColor = LWBackground,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF080C14), Color(0xFF071830))
                    )
                ),
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = NavigationBarDefaults.Elevation
            ) {
                screens.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LockWitnessPrimary,
                            selectedTextColor = LockWitnessPrimary,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                            unselectedIconColor = LockWitnessTextSecondary,
                            unselectedTextColor = LockWitnessTextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = LockWitnessDestination.Dashboard.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(LockWitnessDestination.Dashboard.route) {
                DashboardScreen(
                    contentPadding = innerPadding,
                    onNavigateToUpgrade = { navController.navigate(UPGRADE_ROUTE) },
                    onNavigateToSettings = { navController.navigate(LockWitnessDestination.Settings.route) }
                )
            }
            composable(UPGRADE_ROUTE) {
                UpgradeScreen(
                    contentPadding = innerPadding,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(LockWitnessDestination.Settings.route) {
                SettingsScreen(contentPadding = innerPadding)
            }
            composable(LockWitnessDestination.History.route) {
                HistoryScreen(contentPadding = innerPadding)
            }
            composable(LockWitnessDestination.Diagnostics.route) {
                DiagnosticsScreen(contentPadding = innerPadding)
            }
            composable(LockWitnessDestination.About.route) {
                AboutScreen(contentPadding = innerPadding)
            }
        }
    }
}
