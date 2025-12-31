package com.simple.digisave.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simple.digisave.data.local.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    // ✅ Insert list of transactions (for Firestore sync cache)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllTransactions(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND amount > 0")
    fun getTotalIncome(userId: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND amount < 0")
    fun getTotalExpenses(userId: String): Flow<Double?>

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Int)

    // 🔑 NEW: delete using Firestore ID (for sync)
    @Query("DELETE FROM transactions WHERE firestoreId = :firestoreId")
    suspend fun deleteByFirestoreId(firestoreId: String)

    // 🔑 NEW: update a transaction’s Firestore ID after local insert
    @Query("UPDATE transactions SET firestoreId = :firestoreId WHERE id = :localId")
    suspend fun updateFirestoreId(localId: Int, firestoreId: String)

    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Query("""
        SELECT categoryId, SUM(amount) as total
        FROM transactions
        WHERE userId = :userId
        GROUP BY categoryId
    """)
    fun getCategoryTotals(userId: String): Flow<List<CategoryTotal>>

    @Query("SELECT firestoreId FROM transactions WHERE firestoreId IS NOT NULL")
    suspend fun getAllFirestoreIds(): List<String>


    @Query("""
    SELECT t.id, t.firestoreId, t.title, t.amount, t.timestamp, t.categoryId,
           c.name AS categoryName, c.icon AS categoryIcon, t.createdAt
    FROM transactions t
    LEFT JOIN categories c ON t.categoryId = c.id
    WHERE t.userId = :userId
    ORDER BY 
        datetime(t.timestamp / 1000, 'unixepoch') DESC,
        t.id DESC
""")
    fun getTransactionsWithCategory(userId: String): Flow<List<TransactionWithCategory>>
}

// DTO for category totals
data class CategoryTotal(
    val categoryId: Int?,
    val total: Double
)

// DTO for transaction + category details
data class TransactionWithCategory(
    val id: Int,
    val firestoreId: String?,
    val title: String,
    val amount: Double,
    val timestamp: Long,
    val createdAt: Long,
    val categoryId: Int?,
    val categoryName: String?,
    val categoryIcon: String?
)
