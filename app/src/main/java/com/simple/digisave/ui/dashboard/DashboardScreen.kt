package com.simple.digisave.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.simple.digisave.ui.components.EmptyState
import com.simple.digisave.ui.dashboard.model.DashboardUiState
import com.simple.digisave.ui.dashboard.model.TransactionUi
import com.simple.digisave.ui.navigation.BottomNavItem
import com.simple.digisave.ui.theme.AccentTeal
import com.simple.digisave.ui.theme.PastelGreen
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@Composable
fun DashboardScreen(
    mainNavController: NavController,
    bottomNavController: NavController,
    userId: String,
    snackbarHostState: SnackbarHostState, // ✅ now received from MainScreen
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) { viewModel.loadData(userId) }

    DashboardContent(
        uiState = uiState,
        onViewAllClick = { bottomNavController.navigate(BottomNavItem.Transactions.route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(bottomNavController.graph.startDestinationId) {
                saveState = true
            }
        }
        },
        onDelete = { localId, firestoreId, deletedTx ->
            viewModel.deleteTransactionById(localId, firestoreId)

            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Transaction deleted",
                    actionLabel = "Undo",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.addTransaction(
                        userId = userId,
                        title = deletedTx.title,
                        amount = deletedTx.amount,
                        categoryId = deletedTx.categoryId,     // ✅ RESTORE ORIGINAL CATEGORY
                        timestamp = deletedTx.timestamp        // ✅ RESTORE ORIGINAL TIMESTAMP
                    )

                }
            }
        }
    )
}



@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onViewAllClick: () -> Unit,
    onDelete: (Int, String?, TransactionUi) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BalanceCard(
                totalBalance = uiState.totalBalance,
                totalIncome = uiState.totalIncome,
                totalExpenses = uiState.totalExpenses
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = onViewAllClick) { Text("View All") }
            }
        }

        if (uiState.recentTransactions.isEmpty()) {
            item { EmptyState("No recent transactions") }
        } else {
            items(uiState.recentTransactions, key = { it.id }) { tx ->
                TransactionRow(
                    tx = tx,
                    onDelete = { localId, firestoreId ->
                        onDelete(localId, firestoreId, tx)
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
fun TransactionRow(
    tx: TransactionUi,
    onDelete: (Int, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = PastelGreen.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = tx.icon, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tx.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "${tx.categoryName} • ${tx.date}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val formattedAmount = currencyFormatter.format(kotlin.math.abs(tx.amount))
                Text(
                    text = if (tx.amount > 0) "+$formattedAmount" else "-$formattedAmount",
                    color = if (tx.amount > 0) AccentTeal else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = { onDelete(tx.id, tx.firestoreId) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Transaction",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}
