package com.simple.digisave.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.simple.digisave.ui.auth.LoginScreen
import com.simple.digisave.ui.auth.SignUpScreen
import com.simple.digisave.ui.splash.SplashScreen
import com.simple.digisave.ui.transactions.AddTransactionScreen

@Composable
fun AppNavGraph(rootNavController: NavHostController) {
    NavHost(
        navController = rootNavController,
        startDestination = "splash"
    ) {
        composable("splash") { SplashScreen(rootNavController) }
        composable("login") { LoginScreen(rootNavController) }
        composable("signup") { SignUpScreen(rootNavController) }

        // ✅ Pass root controller into MainScreen
        composable("main") { MainScreen(rootNavController) }

        composable("add_transaction") {
            AddTransactionScreen(rootNavController)
        }
    }
}
