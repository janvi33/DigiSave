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
import java.text.SimpleDateFormat
import java.util.*
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

    // Month navigation — starts on the current calendar month
    private val _selectedPeriod = MutableStateFlow(
        Calendar.getInstance().let { it.get(Calendar.YEAR) to it.get(Calendar.MONTH) }
    )

    private var dataJob: Job? = null

    fun loadData(userId: String) {
        if (dataJob?.isActive == true) return

        dataJob = viewModelScope.launch {
            repo.syncTransactions(userId)

            // Split into two sub-flows so we can combine 6 sources cleanly
            val baseFlow = combine(
                repo.getTransactionsWithCategory(userId),
                sortOption,
                groupOption
            ) { transactions, sort, group ->
                Triple(transactions, sort, group)
            }

            val totalsFlow = combine(
                repo.getTotalIncome(userId),
                repo.getTotalExpenses(userId)
            ) { income, expenses ->
                Pair(income ?: 0.0, expenses ?: 0.0)
            }

            combine(baseFlow, totalsFlow, _selectedPeriod) { base, totals, period ->
                val (transactions, sort, group) = base
                val (income, expenses) = totals
                val (selectedYear, selectedMonth) = period

                val list = transactions.map { it.toUi() }
                val sorted = sortTransactions(list, sort)
                val grouped = groupTransactions(sorted, group)

                // Filter to selected month for the card stats
                val monthlyList = list.filter { tx ->
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = tx.timestamp
                    cal.get(Calendar.YEAR) == selectedYear &&
                            cal.get(Calendar.MONTH) == selectedMonth
                }
                val monthlyIncome = monthlyList.filter { it.amount > 0 }.sumOf { it.amount }
                val monthlyExpenses = monthlyList.filter { it.amount < 0 }.sumOf { it.amount }

                // Build the month label (e.g. "March 2026")
                val labelCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, selectedMonth)
                }
                val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                    .format(labelCal.time)

                DashboardUiState(
                    isLoading = false,
                    totalBalance = income + expenses,
                    totalIncome = income,
                    totalExpenses = expenses,
                    monthlyIncome = monthlyIncome,
                    monthlyExpenses = monthlyExpenses,
                    selectedMonthLabel = monthLabel,
                    recentTransactions = sorted.take(5),
                    allTransactions = sorted,
                    groupedTransactions = grouped
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun navigateMonth(delta: Int) {
        val (year, month) = _selectedPeriod.value
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            add(Calendar.MONTH, delta)
        }
        _selectedPeriod.value = cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
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
