package com.simple.digisave.ui.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simple.digisave.ui.theme.AccentTeal
import java.text.NumberFormat
import java.util.*

@Composable
fun BalanceCard(
    totalBalance: Double,
    totalIncome: Double,
    totalExpenses: Double
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val absBalance = kotlin.math.abs(totalBalance)
    val formattedBalance = currencyFormatter.format(absBalance)

    // 🎨 Animated color transitions
    val balanceColor by animateColorAsState(
        targetValue = if (totalBalance < 0)
            MaterialTheme.colorScheme.error
        else
            AccentTeal,
        label = "balanceColor"
    )

    val incomeColor = AccentTeal
    val expenseColor = MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )

            // 💫 Smooth animated color and sign
            Text(
                text = if (totalBalance < 0) "-$formattedBalance" else formattedBalance,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                ),
                color = balanceColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Income: ${currencyFormatter.format(totalIncome)}",
                    color = incomeColor,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    "Expenses: ${currencyFormatter.format(kotlin.math.abs(totalExpenses))}",
                    color = expenseColor,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}
