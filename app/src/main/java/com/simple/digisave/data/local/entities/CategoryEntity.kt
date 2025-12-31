package com.simple.digisave.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val icon: String,
    val type: String,   // "income" or "expense"
    val group: String   // "Bills", "Needs", "Wants", "Inflow"
)

