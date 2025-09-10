package com.simple.digisave.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.simple.digisave.ui.components.DigiSaveTopBar
import com.simple.digisave.ui.components.EmptyState
import com.simple.digisave.ui.navigation.BottomNavItem
import com.simple.digisave.ui.theme.AccentTeal
import com.simple.digisave.ui.theme.ActiveBlue
import com.simple.digisave.ui.theme.PastelGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    rootNavController: NavController,    // for add_transaction
    mainNavController: NavController,    // for bottom nav
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(userId) {
        userId?.let { viewModel.loadData(it) }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { rootNavController.navigate("add_transaction") },
                containerColor = ActiveBlue
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Balance Card
            BalanceCard(
                totalBalance = uiState.totalBalance,
                totalIncome = uiState.totalIncome,
                totalExpenses = uiState.totalExpenses
            )


            Spacer(modifier = Modifier.height(24.dp))

            // Recent Transactions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(
                    onClick = { mainNavController.navigate(BottomNavItem.Transactions.route) }
                ) {
                    Text("View All")
                }
            }

            if (uiState.recentTransactions.isEmpty()) {
                EmptyState("No recent transactions")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.recentTransactions) { tx ->
                        TransactionRow(
                            tx = tx,
                            onDelete = { id -> viewModel.deleteTransactionById(id) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun TransactionRow(
    tx: TransactionUi,
    onDelete: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: icon + title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tx.icon,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = tx.title,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Right: amount + delete button
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (tx.amount > 0) "+$${tx.amount}" else "-$${-tx.amount}",
                    color = if (tx.amount > 0) AccentTeal else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                IconButton(onClick = { onDelete(tx.id) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Transaction",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}



data class TransactionUi(
    val id: Int,
    val title: String,
    val amount: Double,
    val icon: String
)


