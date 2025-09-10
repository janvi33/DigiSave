package com.simple.digisave.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simple.digisave.data.local.TransactionEntity
import com.simple.digisave.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val recentTransactions: List<TransactionUi> = emptyList(),
    val allTransactions: List<TransactionUi> = emptyList() // ✅ new
)


@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repo: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun loadData(userId: String) {
        viewModelScope.launch {
            combine(
                repo.getAllTransactions(userId),
                repo.getTotalIncome(userId),
                repo.getTotalExpenses(userId)
            ) { transactions, income, expenses ->

                val incomeVal = income ?: 0.0
                val expenseVal = expenses ?: 0.0
                val balance = incomeVal - expenseVal

                DashboardUiState(
                    totalBalance = balance,
                    totalIncome = incomeVal,
                    totalExpenses = expenseVal,
                    recentTransactions = transactions.take(5).map { // ✅ only top 5
                        TransactionUi(
                            id = it.id,
                            title = it.title,
                            amount = it.amount,
                            icon = if (it.amount > 0) "💰" else "💸"
                        )
                    },
                    allTransactions = transactions.map { // ✅ full list
                        TransactionUi(
                            id = it.id,
                            title = it.title,
                            amount = it.amount,
                            icon = if (it.amount > 0) "💰" else "💸"
                        )
                    }
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun addTransaction(userId: String, title: String, amount: Double) {
        viewModelScope.launch {
            repo.insertTransaction(
                TransactionEntity(
                    userId = userId,
                    title = title,
                    amount = amount
                )
            )
        }
    }

    fun deleteTransactionById(id: Int) {
        viewModelScope.launch {
            repo.deleteTransactionById(id)
        }
    }



}
