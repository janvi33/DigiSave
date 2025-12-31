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
import com.simple.digisave.ui.transactions.TransactionsScreen // ✅ import this

@Composable
fun AppNavGraph(rootNavController: NavHostController) {
    NavHost(
        navController = rootNavController,
        startDestination = "splash"
    ) {
        composable("splash") { SplashScreen(rootNavController) }
        composable("login") { LoginScreen(rootNavController) }
        composable("signup") { SignUpScreen(rootNavController) }

        // ✅ main with userId param
        composable(
            route = "main/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            MainScreen(rootNavController, userId)
        }

        composable("add_transaction") {
            AddTransactionScreen(rootNavController)
        }

        composable("categories") {
            CategoriesScreen(rootNavController)
        }

//        // ✅ NEW: transactions route (fixes crash)
//        composable(BottomNavItem.Transactions.route) {
//            TransactionsScreen()
//        }
    }
}
