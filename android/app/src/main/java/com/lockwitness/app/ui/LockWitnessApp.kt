package com.lockwitness.app.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lockwitness.app.ui.screens.AboutScreen
import com.lockwitness.app.ui.screens.DashboardScreen
import com.lockwitness.app.ui.screens.DiagnosticsScreen
import com.lockwitness.app.ui.screens.ExportEvidenceScreen
import com.lockwitness.app.ui.screens.HistoryScreen
import com.lockwitness.app.ui.screens.IncidentDetailScreen
import com.lockwitness.app.ui.screens.SettingsScreen
import com.lockwitness.app.ui.screens.SetupScreen
import com.lockwitness.app.ui.screens.ShareEvidenceScreen
import com.lockwitness.app.ui.screens.UpgradeScreen
import com.lockwitness.app.ui.theme.GraphiteBg
import com.lockwitness.app.ui.theme.LWNavActive
import com.lockwitness.app.ui.theme.LWNavIndicator
import com.lockwitness.app.ui.theme.LWNavInactive
import com.lockwitness.app.ui.theme.SurfaceRaised

const val UPGRADE_ROUTE = "upgrade"
const val SETUP_ROUTE = "setup"

@Composable
fun LockWitnessApp() {
    val navController = rememberNavController()
    val screens = LockWitnessDestination.entries
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route ?: LockWitnessDestination.Dashboard.route

    val bottomNavRoutes = screens.map { it.route }.toSet()
    val showBottomNav = currentRoute in bottomNavRoutes

    Scaffold(
        containerColor = GraphiteBg,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = SurfaceRaised,
                    contentColor = LWNavInactive
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
                                selectedIconColor = LWNavActive,
                                selectedTextColor = LWNavActive,
                                indicatorColor = LWNavIndicator,
                                unselectedIconColor = LWNavInactive,
                                unselectedTextColor = LWNavInactive
                            )
                        )
                    }
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
                    onNavigateToSettings = { navController.navigate(LockWitnessDestination.Settings.route) },
                    onNavigateToSetup = { navController.navigate(SETUP_ROUTE) }
                )
            }
            composable(UPGRADE_ROUTE) {
                UpgradeScreen(
                    contentPadding = innerPadding,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(SETUP_ROUTE) {
                SetupScreen(
                    contentPadding = innerPadding,
                    onNavigateToDashboard = { navController.navigate(LockWitnessDestination.Dashboard.route) },
                    onNavigateToDiagnostics = { navController.navigate(LockWitnessDestination.Diagnostics.route) }
                )
            }
            composable(LockWitnessDestination.Settings.route) {
                SettingsScreen(
                    contentPadding = innerPadding,
                    onNavigateToSetup = { navController.navigate(SETUP_ROUTE) }
                )
            }
            composable(LockWitnessDestination.History.route) {
                HistoryScreen(
                    contentPadding = innerPadding,
                    onNavigateToDetail = { incidentId ->
                        navController.navigate("incident_detail/$incidentId")
                    }
                )
            }
            composable(
                route = "incident_detail/{incidentId}",
                arguments = listOf(navArgument("incidentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val incidentId = backStackEntry.arguments?.getLong("incidentId") ?: return@composable
                IncidentDetailScreen(
                    incidentId = incidentId,
                    contentPadding = innerPadding,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToExport = { id ->
                        navController.navigate("export_evidence/$id")
                    }
                )
            }
            composable(
                route = "export_evidence/{incidentId}",
                arguments = listOf(navArgument("incidentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val incidentId = backStackEntry.arguments?.getLong("incidentId") ?: -1L
                ExportEvidenceScreen(
                    incidentId = incidentId,
                    contentPadding = innerPadding,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToShare = { filePath ->
                        navController.navigate("share_evidence/${Uri.encode(filePath)}")
                    }
                )
            }
            composable(
                route = "share_evidence/{filePath}",
                arguments = listOf(navArgument("filePath") { type = NavType.StringType })
            ) { backStackEntry ->
                val filePath = Uri.decode(backStackEntry.arguments?.getString("filePath") ?: "")
                ShareEvidenceScreen(
                    filePath = filePath,
                    contentPadding = innerPadding,
                    onNavigateBack = { navController.popBackStack() }
                )
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
