package com.simple.digisave.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
    snackbarHostState: SnackbarHostState,
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) { viewModel.loadData(userId) }

    DashboardContent(
        uiState = uiState,
        onViewAllClick = {
            bottomNavController.navigate(BottomNavItem.Transactions.route) {
                launchSingleTop = true
                restoreState = true
                popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
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
                        categoryId = deletedTx.categoryId,
                        timestamp = deletedTx.timestamp
                    )
                }
            }
        },
        onPreviousMonth = { viewModel.navigateMonth(-1) },
        onNextMonth = { viewModel.navigateMonth(1) }
    )
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onViewAllClick: () -> Unit,
    onDelete: (Int, String?, TransactionUi) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Balance card ─────────────────────────────────────────
        item {
            BalanceCard(
                totalBalance = uiState.totalBalance,
                monthlyIncome = uiState.monthlyIncome,
                monthlyExpenses = uiState.monthlyExpenses,
                selectedMonthLabel = uiState.selectedMonthLabel,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth
            )
        }

        // ── Section header ───────────────────────────────────────
        item {
            Text(
                "Recent Transactions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // ── Transaction list / loading / empty ───────────────────
        item {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.recentTransactions.isEmpty() -> {
                    EmptyState(
                        message = "No transactions yet",
                        subtitle = "Tap + to add your first one",
                        icon = "💸"
                    )
                }

                else -> {
                    // All recent rows grouped inside ONE card — cleaner, less visual noise
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column {
                            uiState.recentTransactions.forEachIndexed { index, tx ->
                                key(tx.id) {
                                    SwipeableTransactionRow(
                                        tx = tx,
                                        inCard = true,
                                        showDivider = true, // always show — footer divider follows
                                        onDelete = { localId, firestoreId ->
                                            onDelete(localId, firestoreId, tx)
                                        }
                                    )
                                }
                            }

                            // ── "View all" footer row ─────────────────
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onViewAllClick() }
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "View all transactions",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}

// ── Swipeable wrapper ────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTransactionRow(
    tx: TransactionUi,
    onDelete: (Int, String?) -> Unit,
    // inCard = true  → used on Dashboard (flat row inside a grouped Card)
    // inCard = false → used on All Transactions (each row is its own Card)
    inCard: Boolean = false,
    showDivider: Boolean = false,
    modifier: Modifier = Modifier
) {
    var dismissed         by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showConfirmDialog = true  // show dialog; swipe snaps back automatically
            }
            false // never lock in dismissed state — AnimatedVisibility handles collapse
        }
    )

    // ── Confirmation dialog ───────────────────────────────────────────────
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = "Delete Transaction",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    text = "Delete \"${tx.title}\"? This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        dismissed = true
                        onDelete(tx.id, tx.firestoreId)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    AnimatedVisibility(
        visible = !dismissed,
        exit = shrinkVertically(animationSpec = tween(250)) + fadeOut(animationSpec = tween(200)),
        modifier = modifier
    ) {
        Column {
            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = false,
                backgroundContent = {
                    // When inside a grouped card, let the parent card clip rounded corners.
                    // When standalone, clip each row individually.
                    val bgModifier = if (inCard)
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.errorContainer)
                    else
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)

                    Box(
                        modifier = bgModifier,
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(end = 24.dp)
                        )
                    }
                }
            ) {
                TransactionRow(tx = tx, inCard = inCard)
            }

            // Divider lives inside AnimatedVisibility so it collapses with its row
            if (showDivider) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

// ── Transaction row ──────────────────────────────────────────────

@Composable
fun TransactionRow(
    tx: TransactionUi,
    inCard: Boolean = false,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val isIncome = tx.amount > 0
    val iconBg = if (isIncome) PastelGreen.copy(alpha = 0.28f) else PastelRed.copy(alpha = 0.20f)

    val rowContent = @Composable {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
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
                        .size(46.dp)
                        .background(color = iconBg, shape = RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = tx.icon, fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = tx.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${tx.categoryName} · ${tx.date}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            val formattedAmount = currencyFormatter.format(kotlin.math.abs(tx.amount))
            Text(
                text = if (isIncome) "+$formattedAmount" else "-$formattedAmount",
                color = if (isIncome) AccentTeal else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }

    if (inCard) {
        // Flat — no Card wrapper; parent card handles shape and elevation
        rowContent()
    } else {
        // Standalone card for the All Transactions screen
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            rowContent()
        }
    }
}
