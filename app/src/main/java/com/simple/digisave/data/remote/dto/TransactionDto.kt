package com.simple.digisave.data.remote.dto

import com.simple.digisave.data.local.entities.TransactionEntity

data class TransactionDto(
    val firestoreId: String? = null,
    val userId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val timestamp: Long = 0L,
    val categoryId: Int? = null,
    val createdAt: Long = System.currentTimeMillis(), // ✅ moved before lastModified for clarity
    val lastModified: Long = System.currentTimeMillis()
) {
    // ✅ Ensure Firestore has matching constructor parameter order
    constructor() : this(null, "", "", 0.0, 0L, null, System.currentTimeMillis(), System.currentTimeMillis())

    fun toEntity(): TransactionEntity = TransactionEntity(
        firestoreId = firestoreId,
        userId = userId,
        title = title,
        amount = amount,
        timestamp = if (timestamp > 0) timestamp else System.currentTimeMillis(),
        createdAt = createdAt,
        categoryId = categoryId
    )

    companion object {
        fun fromEntity(entity: TransactionEntity): TransactionDto =
            TransactionDto(
                firestoreId = entity.firestoreId,
                userId = entity.userId,
                title = entity.title,
                amount = entity.amount,
                timestamp = entity.timestamp,
                categoryId = entity.categoryId,
                createdAt = entity.createdAt, // ✅ preserved
                lastModified = System.currentTimeMillis()
            )
    }
}
