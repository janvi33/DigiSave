package com.simple.digisave.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String , val label: String, val icon: ImageVector) {
    object Dashboard : BottomNavItem("dashboard", "Dashboard", Icons.Default.Home)
    object Budgets : BottomNavItem("budgets", "Budgets", Icons.Default.Wallet)
    object Analytics : BottomNavItem("analytics","Analytics", Icons.Default.PieChart)
    object Transactions : BottomNavItem("transactions", "Transactions", Icons.Default.List)
    object Profile  : BottomNavItem("profile" , "Profile", Icons.Default.Person)
}
