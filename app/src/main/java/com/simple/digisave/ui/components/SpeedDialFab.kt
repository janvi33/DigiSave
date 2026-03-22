package com.simple.digisave.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simple.digisave.ui.theme.AccentTeal

private val BudgetPurple     = Color(0xFF9575CD)
private val BudgetPurpleChip = Color(0xFFEDE7F6)
private val TealChip         = Color(0xFFDFF7F5)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SpeedDialFAB(
    visible: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAddTransaction: () -> Unit,
    onSetBudget: () -> Unit,
) {
    if (!visible) return

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        // ------------------ MINI FAB: ADD TRANSACTION ------------------
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(250)) + slideInVertically { it / 4 },
            exit = fadeOut(tween(150)) + slideOutVertically { it / 4 }
        ) {
            MiniFab(
                label = "Add Transaction",
                icon = Icons.Default.Receipt,
                fabColor = AccentTeal,
                chipColor = TealChip,
                onClick = {
                    onExpandedChange(false)
                    onAddTransaction()
                }
            )
        }

        // ------------------ MINI FAB: SET BUDGET ------------------
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(250, delayMillis = 70)) + slideInVertically { it / 4 },
            exit = fadeOut(tween(150)) + slideOutVertically { it / 4 }
        ) {
            MiniFab(
                label = "Set Budget",
                icon = Icons.Default.Savings,
                fabColor = BudgetPurple,
                chipColor = BudgetPurpleChip,
                onClick = {
                    onExpandedChange(false)
                    onSetBudget()
                }
            )
        }

        // ------------------ MAIN BIG FAB ------------------
        val rotation by animateFloatAsState(
            targetValue = if (expanded) 45f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ), label = "fabRotation"
        )

        FloatingActionButton(
            onClick = { onExpandedChange(!expanded) },
            modifier = Modifier
                .size(64.dp)
                .shadow(20.dp, CircleShape),
            containerColor = AccentTeal
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add transaction",
                modifier = Modifier.rotate(rotation),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun MiniFab(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    fabColor: Color,
    chipColor: Color,
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Label chip
        Box(
            modifier = Modifier
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(18.dp))
                .background(color = chipColor, shape = RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                fontSize = 14.sp
            )
        }

        // Mini FAB button
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(46.dp),
            containerColor = fabColor
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Color.White)
        }
    }
}
