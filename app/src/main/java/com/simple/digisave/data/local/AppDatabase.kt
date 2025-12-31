package com.simple.digisave.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.simple.digisave.data.local.dao.TransactionDao
import com.simple.digisave.data.local.dao.CategoryDao
import com.simple.digisave.data.local.entities.CategoryEntity
import com.simple.digisave.data.local.entities.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class // 👈 new entity added
    ],
    version = 3, // 👈 bumped version because schema changed
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun CategoryDao(): CategoryDao // 👈 new DAO added
}
