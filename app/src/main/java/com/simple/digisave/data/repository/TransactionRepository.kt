package com.simple.digisave.data.repository

import com.simple.digisave.data.local.TransactionDao
import com.simple.digisave.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val dao: TransactionDao
) {

    suspend fun insertTransaction(transaction: TransactionEntity) {
        dao.insert(transaction)
    }

    fun getAllTransactions(userId: String): Flow<List<TransactionEntity>> {
        return dao.getAllTransactions(userId)
    }

    fun getTotalIncome(userId: String): Flow<Double?> {
        return dao.getTotalIncome(userId)
    }

    fun getTotalExpenses(userId: String): Flow<Double?> {
        return dao.getTotalExpenses(userId)
    }

    suspend fun deleteTransactionById(id: Int) {
        dao.deleteById(id)
    }


}
