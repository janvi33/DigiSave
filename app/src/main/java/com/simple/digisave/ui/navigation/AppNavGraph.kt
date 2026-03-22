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

        composable(
            route = "add_transaction?type={type}&userId={userId}",
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    defaultValue = "none"
                    nullable = true
                },
                navArgument("userId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type")
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            AddTransactionScreen(
                navController = rootNavController,
                preselectedType = type,
                userId = userId
            )
        }

        // Categories — always opened with a type filter from AddTransactionScreen
        composable(
            route = "categories?type={type}",
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    defaultValue = "expense"
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val typeFilter = backStackEntry.arguments?.getString("type") ?: "expense"
            CategoriesScreen(rootNavController, typeFilter = typeFilter)
        }
    }
}
