package com.simple.digisave.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simple.digisave.data.local.AppDatabase
import com.simple.digisave.data.local.dao.TransactionDao
import com.simple.digisave.data.local.dao.CategoryDao
import com.simple.digisave.data.repository.CategoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // ✅ Step 3: Migration from version 2 → 3 (adds createdAt column)
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Adds new column 'createdAt' with default timestamp in milliseconds
            db.execSQL(
                "ALTER TABLE transactions ADD COLUMN createdAt INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)"
            )
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "digisave_db"
        )
            .addMigrations(MIGRATION_2_3) // ✅ apply safe migration
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.CategoryDao()

    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryDao: CategoryDao,
        transactionDao: TransactionDao
    ): CategoryRepository {
        return CategoryRepository(categoryDao, transactionDao)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences("digisave_prefs", Context.MODE_PRIVATE)
    }

}
