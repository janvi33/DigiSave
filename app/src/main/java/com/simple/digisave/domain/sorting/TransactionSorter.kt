package com.simple.digisave.domain.sorting

import com.simple.digisave.ui.dashboard.model.TransactionUi

// Sorting options for the bottom sheet
enum class SortOption(val label: String) {
    NONE("None"),
    CATEGORY("Category (A → Z)"),
    AMOUNT_ASC("Amount: Low → High"),
    AMOUNT_DESC("Amount: High → Low"),
    DATE_ADDED("Date Added (Newest)"),
    DATE_TRANSACTION("Transaction Date (Newest)")
}

fun sortTransactions(
    list: List<TransactionUi>,
    option: SortOption
): List<TransactionUi> {

    return when (option) {

        SortOption.NONE ->
            list // ⭐ no sorting, return original order

        SortOption.CATEGORY ->
            list.sortedBy { it.categoryName.lowercase() }

        SortOption.AMOUNT_ASC ->
            list.sortedBy { it.amount }

        SortOption.AMOUNT_DESC ->
            list.sortedByDescending { it.amount }

        SortOption.DATE_ADDED ->
            list.sortedByDescending { it.createdAt }

        SortOption.DATE_TRANSACTION ->
            list.sortedByDescending { it.timestamp }
    }
}
