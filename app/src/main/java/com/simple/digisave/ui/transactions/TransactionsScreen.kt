package com.simple.digisave.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simple.digisave.ui.components.EmptyState
import com.simple.digisave.ui.dashboard.DashboardViewModel
import com.simple.digisave.ui.dashboard.SwipeableTransactionRow
import com.simple.digisave.domain.grouping.GroupItem
import kotlinx.coroutines.launch

@Composable
fun TransactionsScreen(
    viewModel: DashboardViewModel,
    userId: String
) {
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        TransactionsContent(
            groupedList = uiState.groupedTransactions,
            isLoading = uiState.isLoading,
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
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun TransactionsContent(
    groupedList: List<GroupItem>,
    isLoading: Boolean = false,
    onDelete: (Int, String?, com.simple.digisave.ui.dashboard.model.TransactionUi) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (groupedList.isEmpty()) {
        EmptyState(
            message = "No transactions yet",
            subtitle = "Swipe back to the dashboard\nand tap + to get started",
            icon = "🧾"
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentPadding = PaddingValues(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            groupedList,
            key = { item ->
                when (item) {
                    is GroupItem.Header -> "header_${item.label}"
                    is GroupItem.Item   -> "item_${item.tx.id}"
                }
            }
        ) { item ->
            when (item) {

                is GroupItem.Header -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp)
                    ) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                is GroupItem.Item -> {
                    SwipeableTransactionRow(
                        tx = item.tx,
                        onDelete = { id, firestoreId -> onDelete(id, firestoreId, item.tx) }
                    )
                }
            }
        }
    }
}
