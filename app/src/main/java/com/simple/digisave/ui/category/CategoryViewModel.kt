package com.simple.digisave.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.simple.digisave.data.local.entities.CategoryEntity
import com.simple.digisave.data.repository.CategoryRepository
import com.simple.digisave.data.repository.CategoryWithTotal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val userId: String? = firebaseAuth.currentUser?.uid

    val categories: StateFlow<List<CategoryWithTotal>> =
        if (userId != null) {
            repository.getCategoriesWithTotals(userId)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
        } else {
            flowOf(emptyList<CategoryWithTotal>())
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
        }

    fun addCategory(name: String, icon: String, type: String, group: String = "Custom") {
        viewModelScope.launch {
            repository.insertCategory(
                CategoryEntity(
                    name = name,
                    icon = icon,
                    type = type,
                    group = group
                )
            )
        }
    }
}
