package com.simple.digisave.data.repository

import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.simple.digisave.data.local.dao.TransactionDao
import com.simple.digisave.data.local.dao.TransactionWithCategory
import com.simple.digisave.data.local.entities.TransactionEntity
import com.simple.digisave.data.remote.dto.TransactionDto
import com.simple.digisave.domain.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val dao: TransactionDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val categoryRepository: CategoryRepository,
    private val prefs: SharedPreferences
) {

    // ✅ Insert full entity (used internally)
    suspend fun insertTransaction(transaction: TransactionEntity) {
        var safeTx = transaction.copy(
            categoryId = transaction.categoryId?.takeIf { it > 0 }
                ?: categoryRepository.ensureUncategorizedCategory(),
            createdAt = System.currentTimeMillis()
        )

        if (auth.currentUser != null) {
            val dto = TransactionDto.fromEntity(safeTx)
            val docRef = if (safeTx.firestoreId.isNullOrEmpty()) {
                firestore.collection("transactions").add(dto).await()
            } else {
                firestore.collection("transactions")
                    .document(safeTx.firestoreId)
                    .set(dto)
                    .await()
                null
            }

            if (docRef != null) {
                safeTx = safeTx.copy(firestoreId = docRef.id)
            }
        }

        dao.insert(safeTx)
    }

    // ✅ Insert from UI parameters (Firestore first, then Room once)
    suspend fun insertTransaction(
        userId: String,
        title: String,
        amount: Double,
        categoryId: Int? = null,
        note: String? = null,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val safeCategoryId = categoryId?.takeIf { it > 0 }
            ?: categoryRepository.ensureUncategorizedCategory()

        var tx = TransactionEntity(
            userId = userId,
            title = title,
            amount = amount,
            categoryId = safeCategoryId,
            timestamp = timestamp,
            createdAt = System.currentTimeMillis()
        )

        if (auth.currentUser != null) {
            val dto = TransactionDto.fromEntity(tx)
            val docRef = firestore.collection("transactions").add(dto).await()
            tx = tx.copy(firestoreId = docRef.id)
        }

        dao.insert(tx)
    }

    // ✅ Smart incremental sync — no duplicates, newest-first
    private var lastSyncTime = 0L

    suspend fun syncTransactions(userId: String, forceRefresh: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!forceRefresh && now - lastSyncTime < 10_000) return

        if (auth.currentUser != null) {
            val lastSyncedAt = prefs.getLong("last_synced_createdAt_$userId", 0L)

            var query = firestore.collection("transactions")
                .whereEqualTo("userId", userId)

            if (lastSyncedAt > 0) {
                query = query.whereGreaterThan("createdAt", lastSyncedAt)
            }

            val snapshot = query.get().await()
            val uncategorizedId = categoryRepository.ensureUncategorizedCategory()

            // ✅ Get all existing Firestore IDs once
            val existingIds = dao.getAllFirestoreIds().toSet()

            val newTransactions = snapshot.documents.mapNotNull { doc ->
                val dto = doc.toObject(TransactionDto::class.java)?.copy(firestoreId = doc.id)
                val entity = dto?.toEntity()

                // Skip if Firestore ID already exists locally
                if (entity?.firestoreId in existingIds) return@mapNotNull null

                entity?.copy(
                    categoryId = entity.categoryId?.takeIf { it > 0 } ?: uncategorizedId,
                    createdAt = dto?.createdAt ?: System.currentTimeMillis()
                )
            }

            if (newTransactions.isNotEmpty()) {
                val sorted = newTransactions.sortedByDescending { it.createdAt }
                dao.insertAll(sorted)

                val newestCreatedAt = sorted.firstOrNull()?.createdAt ?: lastSyncedAt
                prefs.edit().putLong("last_synced_createdAt_$userId", newestCreatedAt).apply()
            }

            lastSyncTime = now
        }
    }

    // ✅ Queries
    fun getTransactionsWithCategory(userId: String): Flow<List<TransactionWithCategory>> =
        dao.getTransactionsWithCategory(userId)

    fun getAllTransactions(userId: String): Flow<List<TransactionEntity>> =
        dao.getAllTransactions(userId)

    fun getTotalIncome(userId: String): Flow<Double?> =
        dao.getTotalIncome(userId)

    fun getTotalExpenses(userId: String): Flow<Double?> =
        dao.getTotalExpenses(userId)

    // ✅ Delete locally + Firestore
    suspend fun deleteTransactionById(id: Int, firestoreId: String?) {
        dao.deleteById(id)
        if (auth.currentUser != null && firestoreId != null) {
            firestore.collection("transactions").document(firestoreId).delete().await()
        }
    }
}
