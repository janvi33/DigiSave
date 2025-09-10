package com.simple.digisave.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.simple.digisave.ui.components.DigiSaveTopBar
import com.simple.digisave.ui.dashboard.DashboardScreen
import com.simple.digisave.ui.transactions.TransactionsScreen

@Composable
fun MainScreen(rootNavController: NavHostController) {
    val mainNavController = rememberNavController()
    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Map route → title
    val topBarTitle = when (currentRoute) {
        BottomNavItem.Dashboard.route -> "DigiSave"
        BottomNavItem.Budgets.route -> "Budgets"
        BottomNavItem.Analytics.route -> "Analytics"
        BottomNavItem.Transactions.route -> "All Transactions"
        BottomNavItem.Profile.route -> "Profile"
        else -> ""
    }

    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Budgets,
        BottomNavItem.Analytics,
        BottomNavItem.Transactions,
        BottomNavItem.Profile
    )

    Scaffold(
        topBar = { DigiSaveTopBar(topBarTitle) },
        bottomBar = {
            BubbleBottomNavBar(
                navController = mainNavController,
                items = items // ✅ pass list here
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = mainNavController,
            startDestination = BottomNavItem.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Dashboard.route) {
                DashboardScreen(
                    rootNavController = rootNavController,
                    mainNavController = mainNavController
                )
            }
            composable(BottomNavItem.Budgets.route) { Text("Budgets Screen") }
            composable(BottomNavItem.Analytics.route) { Text("Analytics Screen") }
            composable(BottomNavItem.Transactions.route) { TransactionsScreen() }
            composable(BottomNavItem.Profile.route) { Text("Profile Screen") }
        }
    }
}
