package com.simple.digisave.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.draw.clip
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
import com.simple.digisave.ui.theme.PastelRed
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

        when {
            uiState.isLoading -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            uiState.recentTransactions.isEmpty() -> {
                item {
                    EmptyState(
                        message = "No transactions yet",
                        subtitle = "Tap + to add your first one",
                        icon = "💸"
                    )
                }
            }
            else -> {
                items(uiState.recentTransactions, key = { it.id }) { tx ->
                    SwipeableTransactionRow(
                        tx = tx,
                        onDelete = { localId, firestoreId ->
                            onDelete(localId, firestoreId, tx)
                        }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTransactionRow(
    tx: TransactionUi,
    onDelete: (Int, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var dismissed by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                dismissed = true
                onDelete(tx.id, tx.firestoreId)
            }
            // Always return false — AnimatedVisibility handles the visual collapse,
            // so we never leave the box stuck in a permanently-dismissed state.
            false
        }
    )

    AnimatedVisibility(
        visible = !dismissed,
        exit = shrinkVertically(animationSpec = tween(250)) + fadeOut(animationSpec = tween(200)),
        modifier = modifier
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(end = 20.dp)
                    )
                }
            }
        ) {
            TransactionRow(tx = tx)
        }
    }
}

@Composable
fun TransactionRow(
    tx: TransactionUi,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val isIncome = tx.amount > 0
    val iconBg = if (isIncome) PastelGreen.copy(alpha = 0.25f) else PastelRed.copy(alpha = 0.18f)

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(color = iconBg, shape = RoundedCornerShape(12.dp)),
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

            val formattedAmount = currencyFormatter.format(kotlin.math.abs(tx.amount))
            Text(
                text = if (isIncome) "+$formattedAmount" else "-$formattedAmount",
                color = if (isIncome) AccentTeal else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
