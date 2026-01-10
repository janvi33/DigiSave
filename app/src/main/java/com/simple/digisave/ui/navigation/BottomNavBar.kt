package com.simple.digisave.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.simple.digisave.ui.theme.PastelGreen

// -------------------------------------------------------------
// MAIN NAV BAR
// -------------------------------------------------------------
@Composable
fun BubbleBottomNavBar(
    navController: NavController,
    onTabSelected: (String) -> Unit,
    items: List<BottomNavItem>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp),
        contentAlignment = Alignment.BottomCenter
    ) {

        // Curved pastel background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(PastelGreen)
        )

        // Floating bubble row
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
                        onTabSelected(item.route)   // ⭐ FIXED: delegate to MainScreen
                    }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// INDIVIDUAL NAV ITEM BUBBLE
// -------------------------------------------------------------
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun BubbleNavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = ""
    )

    // Dynamic bubble width
    val itemWidth = when {
        item.label.length > 10 -> 90.dp
        item.label.length > 8 -> 80.dp
        else -> 70.dp
    }

    // Dynamic label size
    val fontSize = when {
        item.label.length > 10 -> 11.sp
        item.label.length > 8 -> 12.sp
        else -> 13.sp
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(itemWidth)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) { onClick() }
    ) {

        // Icon bubble
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(y = (-20).dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = selected,
                transitionSpec = {
                    fadeIn(tween(180)) togetherWith fadeOut(tween(180))
                }, label = ""
            ) { isSelected ->
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(78.dp)
                            .scale(scale)
                            .background(PastelGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .background(Color(0xFF64B5F6), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = Color.Black.copy(alpha = 0.55f),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }

        // Label
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(150))
        ) {
            Text(
                text = item.label,
                fontSize = fontSize,
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.offset(y = (-18).dp)
            )
        }
    }
}
