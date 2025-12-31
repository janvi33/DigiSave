package com.simple.digisave.data.repository

import android.os.Parcelable
import com.simple.digisave.data.local.dao.CategoryDao
import com.simple.digisave.data.local.dao.TransactionDao
import com.simple.digisave.data.local.dao.CategoryTotal
import com.simple.digisave.data.local.entities.CategoryEntity
import kotlinx.parcelize.Parcelize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@Parcelize
data class CategoryWithTotal(
    val id: Int,
    val name: String,
    val icon: String,
    val type: String,
    val group: String,
    val total: Double
) : Parcelable

class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) {
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun insertCategory(category: CategoryEntity): Long =
        categoryDao.insertCategory(category)

    suspend fun ensureDefaultCategories() {
        val current = categoryDao.getAllCategories().first()
        if (current.isEmpty()) {
            val defaults = listOf(
                CategoryEntity(name = "Uncategorized", icon = "❓", type = "expense", group = "Misc"), // 👈 fallback
                CategoryEntity(name = "Ready to Assign", icon = "📥", type = "income", group = "Inflow"),
                CategoryEntity(name = "Rent", icon = "🏠", type = "expense", group = "Bills"),
                CategoryEntity(name = "Utilities", icon = "⚡", type = "expense", group = "Bills"),
                CategoryEntity(name = "Insurance", icon = "📄", type = "expense", group = "Bills"),
                CategoryEntity(name = "Student loans", icon = "🎓", type = "expense", group = "Bills"),
                CategoryEntity(name = "Personal loans", icon = "💰", type = "expense", group = "Bills"),
                CategoryEntity(name = "Groceries", icon = "🛒", type = "expense", group = "Needs"),
                CategoryEntity(name = "Emergency fund", icon = "❗", type = "expense", group = "Needs"),
                CategoryEntity(name = "Hobbies", icon = "🌴", type = "expense", group = "Wants"),
                CategoryEntity(name = "Charity", icon = "💖", type = "expense", group = "Wants"),
                CategoryEntity(name = "Salary", icon = "💵", type = "income", group = "Inflow")
            )
            defaults.forEach { categoryDao.insertCategory(it) }
        }
    }

    // 🔑 Ensure "Uncategorized" exists, return its ID
    suspend fun ensureUncategorizedCategory(): Int {
        val categories = categoryDao.getAllCategories().first()
        val uncategorized = categories.find { it.name == "Uncategorized" }
        return if (uncategorized != null) {
            uncategorized.id
        } else {
            categoryDao.insertCategory(
                CategoryEntity(name = "Uncategorized", icon = "❓", type = "expense", group = "Misc")
            ).toInt()
        }
    }

    // ✅ Combine categories with their totals
    fun getCategoriesWithTotals(userId: String): Flow<List<CategoryWithTotal>> =
        combine(
            categoryDao.getAllCategories(),
            transactionDao.getCategoryTotals(userId)
        ) { categories, totals ->
            categories.map { category ->
                val total = totals.find { it.categoryId == category.id }?.total ?: 0.0
                CategoryWithTotal(
                    id = category.id,
                    name = category.name,
                    icon = category.icon,
                    type = category.type,
                    group = category.group,
                    total = total
                )
            }
        }
}
