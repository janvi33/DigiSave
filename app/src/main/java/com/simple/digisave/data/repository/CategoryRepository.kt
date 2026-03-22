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
                // ── Income ────────────────────────────────────────────
                CategoryEntity(name = "Salary",          icon = "💼", type = "income",  group = "Income"),
                CategoryEntity(name = "Freelance",       icon = "🏢", type = "income",  group = "Income"),
                CategoryEntity(name = "Investments",     icon = "📈", type = "income",  group = "Income"),
                CategoryEntity(name = "Gift / Bonus",    icon = "🎁", type = "income",  group = "Income"),
                CategoryEntity(name = "Refund",          icon = "🔄", type = "income",  group = "Income"),
                CategoryEntity(name = "Other Income",    icon = "💡", type = "income",  group = "Income"),

                // ── Housing ───────────────────────────────────────────
                CategoryEntity(name = "Rent",            icon = "🏠", type = "expense", group = "Housing"),
                CategoryEntity(name = "Utilities",       icon = "⚡", type = "expense", group = "Housing"),
                CategoryEntity(name = "Insurance",       icon = "📄", type = "expense", group = "Housing"),
                CategoryEntity(name = "Maintenance",     icon = "🔧", type = "expense", group = "Housing"),

                // ── Food & Drink ──────────────────────────────────────
                CategoryEntity(name = "Groceries",       icon = "🛒", type = "expense", group = "Food & Drink"),
                CategoryEntity(name = "Dining Out",      icon = "🍕", type = "expense", group = "Food & Drink"),
                CategoryEntity(name = "Coffee & Snacks", icon = "☕", type = "expense", group = "Food & Drink"),

                // ── Transport ─────────────────────────────────────────
                CategoryEntity(name = "Fuel",            icon = "⛽", type = "expense", group = "Transport"),
                CategoryEntity(name = "Public Transport",icon = "🚌", type = "expense", group = "Transport"),
                CategoryEntity(name = "Taxi / Rideshare",icon = "🚕", type = "expense", group = "Transport"),

                // ── Health ────────────────────────────────────────────
                CategoryEntity(name = "Medicine",        icon = "💊", type = "expense", group = "Health"),
                CategoryEntity(name = "Doctor / Hospital",icon ="🏥", type = "expense", group = "Health"),
                CategoryEntity(name = "Gym & Fitness",   icon = "🏋️", type = "expense", group = "Health"),

                // ── Entertainment ─────────────────────────────────────
                CategoryEntity(name = "Movies & Shows",  icon = "🎬", type = "expense", group = "Entertainment"),
                CategoryEntity(name = "Gaming",          icon = "🎮", type = "expense", group = "Entertainment"),
                CategoryEntity(name = "Hobbies",         icon = "🌴", type = "expense", group = "Entertainment"),

                // ── Shopping ──────────────────────────────────────────
                CategoryEntity(name = "Clothing",        icon = "👕", type = "expense", group = "Shopping"),
                CategoryEntity(name = "Electronics",     icon = "📱", type = "expense", group = "Shopping"),
                CategoryEntity(name = "Gifts",           icon = "🎁", type = "expense", group = "Shopping"),

                // ── Finance ───────────────────────────────────────────
                CategoryEntity(name = "Loan Payment",    icon = "💳", type = "expense", group = "Finance"),
                CategoryEntity(name = "Savings Goal",    icon = "🏦", type = "expense", group = "Finance"),
                CategoryEntity(name = "Charity",         icon = "💖", type = "expense", group = "Finance"),

                // ── Education ─────────────────────────────────────────
                CategoryEntity(name = "Tuition / School",icon = "📚", type = "expense", group = "Education"),
                CategoryEntity(name = "Courses & Books", icon = "📖", type = "expense", group = "Education"),

                // ── Other ─────────────────────────────────────────────
                CategoryEntity(name = "Miscellaneous",   icon = "❓", type = "expense", group = "Other")
            )
            categoryDao.insertAll(defaults)
        }
        // Always deduplicate — guards against race conditions between
        // ensureDefaultCategories and ensureUncategorizedCategory both
        // inserting "Miscellaneous" concurrently on first launch
        categoryDao.deduplicateByName()
    }

    // 🔑 Ensure fallback category exists, return its ID
    suspend fun ensureUncategorizedCategory(): Int {
        val categories = categoryDao.getAllCategories().first()
        // Check both old and new name so it works after reinstall or migration
        val fallback = categories.find { it.name == "Miscellaneous" || it.name == "Uncategorized" }
        return if (fallback != null) {
            fallback.id
        } else {
            categoryDao.insertCategory(
                CategoryEntity(name = "Miscellaneous", icon = "❓", type = "expense", group = "Other")
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
