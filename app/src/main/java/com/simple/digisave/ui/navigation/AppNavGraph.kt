package com.simple.digisave.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.simple.digisave.ui.auth.LoginScreen
import com.simple.digisave.ui.auth.SignUpScreen
import com.simple.digisave.ui.category.CategoriesScreen
import com.simple.digisave.ui.splash.SplashScreen
import com.simple.digisave.ui.transactions.AddTransactionScreen

@Composable
fun AppNavGraph(rootNavController: NavHostController) {
    NavHost(
        navController = rootNavController,
        startDestination = "splash"
    ) {

        // Authentication flow
        composable("splash") { SplashScreen(rootNavController) }
        composable("login") { LoginScreen(rootNavController) }
        composable("signup") { SignUpScreen(rootNavController) }

        // Main app entry point with userId
        composable(
            route = "main/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            MainScreen(rootNavController, userId)
        }

        // ⭐ UPDATED add-transaction route (supports income/expense)
        composable(
            route = "add_transaction?type={type}",
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    defaultValue = "none"
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type")
            AddTransactionScreen(
                navController = rootNavController,
                preselectedType = type
            )
        }

        // Categories
        composable("categories") {
            CategoriesScreen(rootNavController)
        }
    }
}
