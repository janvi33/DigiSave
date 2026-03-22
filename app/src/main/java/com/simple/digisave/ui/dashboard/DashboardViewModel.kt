package com.simple.digisave.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simple.digisave.data.repository.TransactionRepository
import com.simple.digisave.domain.mapper.toUi
import com.simple.digisave.ui.dashboard.model.DashboardUiState
import com.simple.digisave.domain.sorting.SortOption
import com.simple.digisave.domain.sorting.sortTransactions
import com.simple.digisave.domain.grouping.GroupOption
import com.simple.digisave.domain.grouping.groupTransactions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repo: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Sorting + grouping states
    private val _sortOption = MutableStateFlow(SortOption.DATE_ADDED)
    val sortOption = _sortOption.asStateFlow()

    private val _groupOption = MutableStateFlow(GroupOption.NONE)
    val groupOption = _groupOption.asStateFlow()

    private var dataJob: Job? = null

    fun loadData(userId: String) {
        // Guard: if the collection pipeline is already running, only sync if needed
        if (dataJob?.isActive == true) return

        dataJob = viewModelScope.launch {
            // Sync from Firestore once per session
            repo.syncTransactions(userId)

            combine(
                repo.getTransactionsWithCategory(userId),
                repo.getTotalIncome(userId),
                repo.getTotalExpenses(userId),
                sortOption,
                groupOption
            ) { transactions, income, expenses, sort, group ->

                val list = transactions.map { it.toUi() }
                val sorted = sortTransactions(list, sort)
                val grouped = groupTransactions(sorted, group)

                val inc = income ?: 0.0
                val exp = expenses ?: 0.0

                DashboardUiState(
                    isLoading = false,
                    totalBalance = inc + exp,
                    totalIncome = inc,
                    totalExpenses = exp,
                    recentTransactions = sorted.take(5),
                    allTransactions = sorted,
                    groupedTransactions = grouped
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateSort(option: SortOption) {
        _sortOption.value = option
    }

    fun updateGroup(option: GroupOption) {
        _groupOption.value = option
    }

    fun addTransaction(
        userId: String,
        title: String,
        amount: Double,
        categoryId: Int? = null,
        note: String? = null,
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repo.insertTransaction(
                userId = userId,
                title = title,
                amount = amount,
                categoryId = categoryId,
                note = note,
                timestamp = timestamp
            )
        }
    }

    fun deleteTransactionById(localId: Int, firestoreId: String?) {
        viewModelScope.launch {
            repo.deleteTransactionById(localId, firestoreId)
        }
    }
}
