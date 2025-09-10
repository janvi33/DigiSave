package com.simple.digisave.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.simple.digisave.ui.theme.PastelGreen

@Composable
fun BubbleBottomNavBar(
    navController: NavController,
    items: List<BottomNavItem>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Outer container with rounded corners at the top
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) // 👈 rounded navbar
            .background(PastelGreen)
    ) {
        NavigationBar(
            containerColor = Color.Transparent, // 👈 handled by Box
            tonalElevation = 0.dp,
            modifier = Modifier
                .height(110.dp) // 👈 taller so bubble isn’t cut
                .padding(top = 4.dp), // space for bubble pop
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(60.dp) // outer hit area
                        ) {
                            // Inner bubble
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(Color(0xFF64B5F6), CircleShape), // Blue bubble
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = Color.Black.copy(alpha = 0.6f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    },
                    label = {
                        if (selected) {
                            Text(
                                text = item.label,
                                color = Color.Black,
                                fontSize = 11.sp
                            )
                        }
                    },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
