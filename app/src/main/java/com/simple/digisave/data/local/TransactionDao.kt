package com.simple.digisave.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // Insert a new transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    // Get all transactions for a specific user (latest first)
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllTransactions(userId: String): Flow<List<TransactionEntity>>

    // Get total income for a user
    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND amount > 0")
    fun getTotalIncome(userId: String): Flow<Double?>

    // Get total expenses for a user
    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND amount < 0")
    fun getTotalExpenses(userId: String): Flow<Double?>

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Int)

}
