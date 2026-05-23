package com.lockwitness.app.ui

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.launch

const val UPGRADE_ROUTE = "upgrade"
const val SETUP_ROUTE = "setup"
private const val MAIN_ROUTE = "main"

@Composable
fun LockWitnessApp() {
    val navController = rememberNavController()
    val screens = LockWitnessDestination.entries
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { screens.size })

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route ?: MAIN_ROUTE
    val showBottomNav = currentRoute == MAIN_ROUTE

    Scaffold(
        containerColor = GraphiteBg,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = SurfaceRaised,
                    contentColor = LWNavInactive
                ) {
                    screens.forEachIndexed { index, screen ->
                        NavigationBarItem(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(index) }
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
            startDestination = MAIN_ROUTE,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(MAIN_ROUTE) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (screens[page]) {
                        LockWitnessDestination.Dashboard -> DashboardScreen(
                            contentPadding = innerPadding,
                            onNavigateToUpgrade = { navController.navigate(UPGRADE_ROUTE) },
                            onNavigateToSettings = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(LockWitnessDestination.Settings.ordinal)
                                }
                            },
                            onNavigateToSetup = { navController.navigate(SETUP_ROUTE) }
                        )
                        LockWitnessDestination.History -> HistoryScreen(
                            contentPadding = innerPadding,
                            onNavigateToDetail = { incidentId ->
                                navController.navigate("incident_detail/$incidentId")
                            }
                        )
                        LockWitnessDestination.Settings -> SettingsScreen(contentPadding = innerPadding)
                        LockWitnessDestination.Diagnostics -> DiagnosticsScreen(contentPadding = innerPadding)
                        LockWitnessDestination.About -> AboutScreen(contentPadding = innerPadding)
                    }
                }
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
                    onNavigateToDashboard = { navController.popBackStack() },
                    onNavigateToDiagnostics = {
                        coroutineScope.launch {
                            pagerState.scrollToPage(LockWitnessDestination.Diagnostics.ordinal)
                        }
                        navController.popBackStack()
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
                    onNavigateToExport = { id -> navController.navigate("export_evidence/$id") }
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
        }
    }
}
