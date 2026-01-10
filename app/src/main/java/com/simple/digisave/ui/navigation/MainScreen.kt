package com.simple.digisave.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.blur
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable

import com.simple.digisave.ui.components.DigiSaveTopBar
import com.simple.digisave.ui.components.SpeedDialFAB
import com.simple.digisave.ui.dashboard.DashboardScreen
import com.simple.digisave.ui.dashboard.DashboardViewModel
import com.simple.digisave.ui.transactions.TransactionsScreen
import com.simple.digisave.ui.transactions.SortGroupBottomSheet
import com.simple.digisave.domain.sorting.SortOption
import com.simple.digisave.domain.grouping.GroupOption

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    rootNavController: NavHostController,
    userId: String
) {
    val bottomNavController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentRoute = bottomNavController.currentBackStackEntryAsState().value?.destination?.route

    val showFab = currentRoute == BottomNavItem.Dashboard.route

    var fabExpanded by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val dashboardVM: DashboardViewModel =
        hiltViewModel(remember(rootNavController) { rootNavController.currentBackStackEntry!! })

    Box(modifier = Modifier.fillMaxSize()) {

        // ---------- CONTENT WITH OPTIONAL BLUR ----------
        Box(
            modifier = Modifier
                .fillMaxSize()
                .let { if (fabExpanded) it.blur(4.dp) else it }
                .zIndex(0f)
        ) {
            Scaffold(
                topBar = {
                    DigiSaveTopBar(
                        title = when (currentRoute) {
                            BottomNavItem.Dashboard.route -> "DigiSave"
                            BottomNavItem.Budgets.route -> "Budgets"
                            BottomNavItem.Analytics.route -> "Analytics"
                            BottomNavItem.Transactions.route -> "All Transactions"
                            BottomNavItem.Profile.route -> "Profile"
                            else -> ""
                        },
                        actions = {
                            if (currentRoute == BottomNavItem.Transactions.route) {
                                IconButton(onClick = { showFilterSheet = true }) {
                                    Icon(Icons.Default.FilterList, null)
                                }
                            }
                        }
                    )
                },

                bottomBar = {
                    BubbleBottomNavBar(
                        navController = bottomNavController,
                        items = listOf(
                            BottomNavItem.Dashboard,
                            BottomNavItem.Budgets,
                            BottomNavItem.Analytics,
                            BottomNavItem.Transactions,
                            BottomNavItem.Profile
                        ),
                        onTabSelected = { newRoute ->
                            fabExpanded = false
                            bottomNavController.navigate(newRoute) {
                                popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            ) { padding ->

                AnimatedNavHost(
                    navController = bottomNavController,
                    startDestination = BottomNavItem.Dashboard.route,
                    modifier = Modifier.padding(padding)
                ) {
                    composable(BottomNavItem.Dashboard.route) {
                        DashboardScreen(
                            mainNavController = rootNavController,
                            bottomNavController = bottomNavController,
                            userId = userId,
                            snackbarHostState = snackbarHostState
                        )
                    }

                    composable(BottomNavItem.Transactions.route) {
                        TransactionsScreen(viewModel = dashboardVM)
                    }

                    composable(BottomNavItem.Budgets.route) { Text("Budgets") }
                    composable(BottomNavItem.Analytics.route) { Text("Analytics") }
                    composable(BottomNavItem.Profile.route) { Text("Profile") }
                }
            }
        }

        // ---------- DIM OVERLAY ----------
        AnimatedVisibility(
            visible = fabExpanded,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize().zIndex(1f)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.12f))
                    .clickable { fabExpanded = false }
            )
        }

        // ---------- SNACKBAR (always above blur, below FAB) ----------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 160.dp) // perfect position
                .zIndex(2f)
                .align(Alignment.BottomCenter)
        ) {
            SnackbarHost(hostState = snackbarHostState)
        }

        // ---------- FAB (top-most) ----------
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 110.dp) // safely above snackbar + nav bar
                .zIndex(3f)
        ) {
            SpeedDialFAB(
                visible = showFab,
                expanded = fabExpanded,
                onExpandedChange = { fabExpanded = it },
                onAddIncome = {
                    fabExpanded = false
                    rootNavController.navigate("add_transaction?type=income")
                },
                onAddExpense = {
                    fabExpanded = false
                    rootNavController.navigate("add_transaction?type=expense")
                }
            )
        }
    }

    if (showFilterSheet) {
        SortGroupBottomSheet(
            currentSort = dashboardVM.sortOption.collectAsState().value,
            currentGroup = dashboardVM.groupOption.collectAsState().value,
            onApply = { s, g -> dashboardVM.updateSort(s); dashboardVM.updateGroup(g) },
            onReset = {
                dashboardVM.updateSort(SortOption.DATE_ADDED)
                dashboardVM.updateGroup(GroupOption.NONE)
            },
            onDismiss = { showFilterSheet = false }
        )
    }
}
