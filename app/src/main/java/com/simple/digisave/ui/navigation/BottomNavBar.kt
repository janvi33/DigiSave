package com.simple.digisave.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simple.digisave.ui.theme.PastelGreen

@Composable
fun BubbleBottomNavBar(
    navController: NavController,
    items: List<BottomNavItem>
) {
    var selectedRoute by remember { mutableStateOf(items.first().route) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp), // ✅ taller navbar so icons & labels breathe
        contentAlignment = Alignment.BottomCenter
    ) {
        // Background bar only
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp) // navbar body
                .background(PastelGreen, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .align(Alignment.BottomCenter)
        )

        // Nav items row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = selectedRoute == item.route

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            selectedRoute = item.route
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                ) {
                    // Bubble + Icon
                    // Bubble + Icon
                    Box(
                        modifier = Modifier
                            .size(84.dp) // ✅ outer green bubble size
                            .offset(y = (-18).dp), // ✅ lift out of navbar
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            // Outer Green Circle
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(PastelGreen, CircleShape), // ✅ clearly visible now

                                contentAlignment = Alignment.Center
                            ) {
                                // Inner Blue Circle
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(Color(0xFF64B5F6), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
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


                    // Label (only when selected)
                    if (selected) {
                        Text(
                            text = item.label,
                            fontSize = 13.sp,
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.offset(y = (-18).dp) // ✅ reduce gap
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BubbleBottomNavBarPreview() {
    val dummyNavController = rememberNavController()

    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Budgets,
        BottomNavItem.Analytics,
        BottomNavItem.Transactions,
        BottomNavItem.Profile
    )

    BubbleBottomNavBar(navController = dummyNavController, items = items)
}
