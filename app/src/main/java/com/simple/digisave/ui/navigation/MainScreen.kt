package com.simple.digisave.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import androidx.compose.animation.*
import androidx.compose.animation.core.tween

import com.simple.digisave.ui.components.DigiSaveTopBar
import com.simple.digisave.ui.dashboard.DashboardScreen
import com.simple.digisave.ui.dashboard.DashboardViewModel
import com.simple.digisave.ui.transactions.TransactionsScreen
import com.simple.digisave.domain.sorting.SortOption
import com.simple.digisave.domain.grouping.GroupOption
import com.simple.digisave.ui.transactions.SortGroupBottomSheet

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    rootNavController: NavHostController,
    userId: String
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }

    // Shared DashboardViewModel for Dashboard + Transactions
    val mainEntry = remember(rootNavController) { rootNavController.currentBackStackEntry!! }
    val dashboardViewModel: DashboardViewModel = hiltViewModel(mainEntry)

    var showFilterSheet by remember { mutableStateOf(false) }

    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Budgets,
        BottomNavItem.Analytics,
        BottomNavItem.Transactions,
        BottomNavItem.Profile
    )

    val topBarTitle = when (currentRoute) {
        BottomNavItem.Dashboard.route -> "DigiSave"
        BottomNavItem.Budgets.route -> "Budgets"
        BottomNavItem.Analytics.route -> "Analytics"
        BottomNavItem.Transactions.route -> "All Transactions"
        BottomNavItem.Profile.route -> "Profile"
        else -> ""
    }

    val showFab = currentRoute == BottomNavItem.Dashboard.route

    Scaffold(
        topBar = {
            DigiSaveTopBar(
                title = topBarTitle,
                actions = {
                    if (currentRoute == BottomNavItem.Transactions.route) {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Sort & Filter"
                            )
                        }
                    }
                }
            )
        },

        bottomBar = {
            BubbleBottomNavBar(
                navController = bottomNavController,
                items = items
            )
        },

        snackbarHost = {
            Box(
                modifier = Modifier.fillMaxWidth().zIndex(2f)
            ) {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        },

        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { rootNavController.navigate("add_transaction") },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { innerPadding ->

        // ⭐ Animated Navigation Host
        AnimatedNavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Dashboard.route,
            modifier = Modifier.padding(innerPadding),

            enterTransition = {
                fadeIn(animationSpec = tween(250)) +
                        slideInHorizontally(animationSpec = tween(250)) { it / 8 }
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200)) +
                        slideOutHorizontally(animationSpec = tween(200)) { -it / 8 }
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(250)) +
                        slideInHorizontally(animationSpec = tween(250)) { -it / 8 }
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(200)) +
                        slideOutHorizontally(animationSpec = tween(200)) { it / 8 }
            }
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
                TransactionsScreen(viewModel = dashboardViewModel)
            }

            composable(BottomNavItem.Budgets.route) {
                Text("Budgets Screen")
            }

            composable(BottomNavItem.Analytics.route) {
                Text("Analytics Screen")
            }

            composable(BottomNavItem.Profile.route) {
                Text("Profile Screen")
            }
        }
    }

    // ⭐ Bottom sheet for sorting/grouping
    if (showFilterSheet) {
        SortGroupBottomSheet(
            currentSort = dashboardViewModel.sortOption.collectAsState().value,
            currentGroup = dashboardViewModel.groupOption.collectAsState().value,

            onApply = { sort, group ->
                dashboardViewModel.updateSort(sort)
                dashboardViewModel.updateGroup(group)
            },

            onReset = {
                dashboardViewModel.updateSort(SortOption.DATE_ADDED)
                dashboardViewModel.updateGroup(GroupOption.NONE)
            },

            onDismiss = { showFilterSheet = false }
        )
    }
}
