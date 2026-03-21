package com.simple.digisave.ui.dashboard.model

data class TransactionUi(
    val id: Int,
    val firestoreId: String?,
    val title: String,
    val amount: Double,
    val icon: String,
    val categoryId: Int?,
    val categoryName: String,
    val timestamp: Long,
    val createdAt: Long,
    val date: String
)
