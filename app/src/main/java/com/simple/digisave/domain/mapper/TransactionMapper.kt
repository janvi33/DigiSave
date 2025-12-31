package com.simple.digisave.domain.mapper

import com.simple.digisave.data.local.dao.TransactionWithCategory
import com.simple.digisave.data.local.entities.TransactionEntity
import com.simple.digisave.data.remote.dto.TransactionDto
import com.simple.digisave.domain.model.Transaction
import com.simple.digisave.ui.dashboard.model.TransactionUi
import java.text.SimpleDateFormat
import java.util.*

// -------------------
// ENTITY ↔ DOMAIN
// -------------------

fun TransactionEntity.toDomain(): Transaction =
    Transaction(
        id = id,
        firestoreId = firestoreId,   // ✅ include Firestore sync id
        userId = userId,
        title = title,
        amount = amount,
        timestamp = timestamp,
        categoryId = categoryId
    )

fun Transaction.toEntity(): TransactionEntity =
    TransactionEntity(
        id = id,
        firestoreId = firestoreId,   // ✅ include Firestore sync id
        userId = userId,
        title = title,
        amount = amount,
        timestamp = timestamp,
        categoryId = categoryId
    )

// -------------------
// ENTITY ↔ DTO (Firestore)
// -------------------

fun TransactionEntity.toDto(): TransactionDto =
    TransactionDto(
        firestoreId = firestoreId,
        userId = userId,
        title = title,
        amount = amount,
        timestamp = timestamp,
        categoryId = categoryId
    )

fun TransactionDto.toEntity(): TransactionEntity =
    TransactionEntity(
        id = 0, // let Room autogenerate
        firestoreId = firestoreId,
        userId = userId,
        title = title,
        amount = amount,
        timestamp = timestamp,
        categoryId = categoryId
    )

// -------------------
// DB JOIN ↔ UI
// -------------------

fun TransactionWithCategory.toUi(): TransactionUi {
    val formatter = SimpleDateFormat("MMM d", Locale.getDefault())

    return TransactionUi(
        id = id,
        firestoreId = firestoreId,
        title = title,
        amount = amount,
        icon = categoryIcon ?: if (amount > 0) "💰" else "💸",
        categoryId = categoryId,       // ✅ ADD THIS
        categoryName = categoryName ?: "Uncategorized",
        timestamp = timestamp,         // ✅ ADD THIS
        date = formatter.format(Date(timestamp))
    )
}

