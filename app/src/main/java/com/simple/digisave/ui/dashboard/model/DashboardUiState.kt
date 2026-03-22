package com.simple.digisave.ui.dashboard.model

import com.simple.digisave.domain.grouping.GroupItem

data class DashboardUiState(
    val isLoading: Boolean = true,
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val recentTransactions: List<TransactionUi> = emptyList(),
    val allTransactions: List<TransactionUi> = emptyList(),
    val groupedTransactions: List<GroupItem> = emptyList()
)
