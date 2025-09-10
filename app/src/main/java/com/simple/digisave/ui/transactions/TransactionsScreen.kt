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
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.simple.digisave.ui.components.DigiSaveTopBar
import com.simple.digisave.ui.components.EmptyState
import com.simple.digisave.ui.dashboard.DashboardViewModel
import com.simple.digisave.ui.dashboard.TransactionRow
import com.simple.digisave.ui.dashboard.TransactionUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Load transactions when screen opens
    LaunchedEffect(userId) {
        userId?.let { viewModel.loadData(it) }
    }

    Scaffold(
        ) { innerPadding ->
        if (uiState.allTransactions.isEmpty()) {
            EmptyState("No transactions yet")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.allTransactions) { tx: TransactionUi ->
                    TransactionRow(
                        tx = tx,
                        onDelete = { id ->
                            viewModel.deleteTransactionById(id)
                        }
                    )
                }

            }
        }
    }
}
