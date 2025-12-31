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

/**
 * BubbleBottomNavBar
 * Reusable navigation bar with a dual-bubble effect (outer green + inner blue).
 */
@Composable
fun BubbleBottomNavBar(
    navController: NavController,
    items: List<BottomNavItem>
) {
    var selectedRoute by remember { mutableStateOf(items.first().route) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp), // enough height so bubbles don’t clip
        contentAlignment = Alignment.BottomCenter
    ) {
        // Background bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(PastelGreen)
                .align(Alignment.BottomCenter)
        )

        // Row of nav items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                BubbleNavItem(
                    item = item,
                    selected = selectedRoute == item.route,
                    onClick = {
                        selectedRoute = item.route
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.weight(1f) // ✅ pass weight here
                )
            }
        }

    }
}

/**
 * Single navigation item with bubble effect.
 */
@Composable
private fun BubbleNavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable { onClick() }
    ) {
        // Bubble + Icon
        Box(
            modifier = Modifier
                .size(84.dp)
                .offset(y = (-18).dp),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                // Outer pastel green circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(PastelGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner blue circle
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
                modifier = Modifier.offset(y = (-18).dp)
            )
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
