// domain/model/Transaction.kt
package com.simple.digisave.domain.model

data class Transaction(
    val id: Int = 0,
    val firestoreId: String? = null,
    val userId: String,
    val title: String,
    val amount: Double,
    val timestamp: Long,
    val categoryId: Int? = null
)

