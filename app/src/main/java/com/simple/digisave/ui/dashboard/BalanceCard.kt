package com.simple.digisave.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.*

// ── Hero card gradient — deep dark teal ───────────────────────────────────
private val HeroTop    = Color(0xFF0B3330)
private val HeroBottom = Color(0xFF1B6B5E)

// ── Stat card colours ─────────────────────────────────────────────────────
private val IncomeGreen   = Color(0xFF2E7D32)
private val IncomeBg      = Color(0xFFF0FBF2)
private val ExpenseRed    = Color(0xFFC62828)
private val ExpenseBg     = Color(0xFFFFF2F1)

// ── Progress bar colours ─────────────────────────────────────────────────
private val ProgressGood    = Color(0xFF43C98C)
private val ProgressWarning = Color(0xFFFFB300)
private val ProgressDanger  = Color(0xFFE53935)

@Composable
fun BalanceCard(
    totalBalance: Double,
    monthlyIncome: Double,
    monthlyExpenses: Double,
    selectedMonthLabel: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

    val spendingRatio by animateFloatAsState(
        targetValue = if (monthlyIncome > 0)
            ((-monthlyExpenses) / monthlyIncome).coerceIn(0.0, 1.0).toFloat()
        else 0f,
        animationSpec = tween(700),
        label = "spendingRatio"
    )

    val progressColor = when {
        spendingRatio > 0.9f -> ProgressDanger
        spendingRatio > 0.7f -> ProgressWarning
        else                 -> ProgressGood
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // ════════════════════════════════════════════════════════
        //  1 · HERO BALANCE CARD
        // ════════════════════════════════════════════════════════
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(HeroTop, HeroBottom)))
            ) {

                // Subtle decorative glow orbs for depth
                Canvas(modifier = Modifier.matchParentSize()) {
                    // Top-right warm orb
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.09f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.88f, size.height * 0.12f),
                            radius = size.width * 0.55f
                        )
                    )
                    // Bottom-left cool orb
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.08f, size.height * 0.92f),
                            radius = size.width * 0.4f
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Label
                    Text(
                        text = "Total Balance",
                        color = Color.White.copy(alpha = 0.55f),
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.2.sp)
                    )

                    Spacer(Modifier.height(10.dp))

                    // Balance — the hero number
                    Text(
                        text = currencyFormatter.format(totalBalance),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 46.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(26.dp))

                    // Month navigator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onPreviousMonth,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = "Previous month",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = selectedMonthLabel,
                            color = Color.White.copy(alpha = 0.88f),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.4.sp
                            )
                        )
                        IconButton(
                            onClick = onNextMonth,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Next month",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // ════════════════════════════════════════════════════════
        //  2 · INCOME & EXPENSE STAT CARDS (side by side)
        // ════════════════════════════════════════════════════════
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Income",
                amount = currencyFormatter.format(monthlyIncome),
                icon = Icons.Default.TrendingUp,
                accentColor = IncomeGreen,
                cardBg = IncomeBg
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Expenses",
                amount = currencyFormatter.format(kotlin.math.abs(monthlyExpenses)),
                icon = Icons.Default.TrendingDown,
                accentColor = ExpenseRed,
                cardBg = ExpenseBg
            )
        }

        // ════════════════════════════════════════════════════════
        //  3 · SPENDING PROGRESS CARD
        // ════════════════════════════════════════════════════════
        if (monthlyIncome > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Spent this month",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${(spendingRatio * 100).toInt()}%",
                            color = if (spendingRatio > 0.7f) ExpenseRed else IncomeGreen,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { spendingRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

// ── Stat card ─────────────────────────────────────────────────────────────
@Composable
private fun StatCard(
    modifier: Modifier,
    label: String,
    amount: String,
    icon: ImageVector,
    accentColor: Color,
    cardBg: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Icon box
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = accentColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = accentColor.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = amount,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
