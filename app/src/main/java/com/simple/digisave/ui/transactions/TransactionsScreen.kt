package com.simple.digisave.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.simple.digisave.ui.components.EmptyState
import com.simple.digisave.ui.dashboard.DashboardViewModel
import com.simple.digisave.ui.dashboard.TransactionRow
import com.simple.digisave.domain.grouping.GroupItem
import kotlinx.coroutines.launch

@Composable
fun TransactionsScreen(
    viewModel: DashboardViewModel   // ⭐ shared ViewModel injected from MainScreen
) {
    val uiState by viewModel.uiState.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        TransactionsContent(
            groupedList = uiState.groupedTransactions,
            onDelete = { localId, firestoreId, deletedTx ->
                viewModel.deleteTransactionById(localId, firestoreId)

                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        "Transaction deleted",
                        actionLabel = "Undo"
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        userId?.let {
                            viewModel.addTransaction(
                                userId = it,
                                title = deletedTx.title,
                                amount = deletedTx.amount,
                                categoryId = deletedTx.categoryId,
                                timestamp = deletedTx.timestamp
                            )
                        }
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
    onDelete: (Int, String?, com.simple.digisave.ui.dashboard.model.TransactionUi) -> Unit,
    modifier: Modifier = Modifier
) {
    if (groupedList.isEmpty()) {
        EmptyState("No transactions yet")
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentPadding = PaddingValues(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(groupedList) { item ->
            when (item) {

                is GroupItem.Header -> {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                is GroupItem.Item -> {
                    TransactionRow(
                        tx = item.tx,
                        onDelete = { id, firestoreId -> onDelete(id, firestoreId, item.tx) }
                    )
                }
            }
        }
    }
}
