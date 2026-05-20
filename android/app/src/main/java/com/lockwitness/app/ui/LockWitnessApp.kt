package com.lockwitness.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockWitnessApp() {
    val navController = rememberNavController()
    val screens = LockWitnessDestination.entries
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route ?: LockWitnessDestination.Dashboard.route
    val currentScreen = screens.firstOrNull { it.route == currentRoute } ?: LockWitnessDestination.Dashboard

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "LockWitness") }
            )
        },
        bottomBar = {
            NavigationBar {
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
                        label = { Text(screen.label) }
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
                    onNavigateToUpgrade = { navController.navigate(UPGRADE_ROUTE) }
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
