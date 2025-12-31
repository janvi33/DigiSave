package com.simple.digisave.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simple.digisave.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
): ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email:String, password:String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repo.login(email,password)
            _authState.value = if (result.isSuccess) {
                AuthState.Success(result.getOrNull()!!) // ✅ pass userId
            } else {
                AuthState.Error(result.exceptionOrNull()?.message)
            }
        }
    }

    fun signUp(email:String, password:String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repo.signUp(email, password)
            _authState.value = if (result.isSuccess) {
                AuthState.Success(result.getOrNull()!!) // ✅ pass userId
            } else {
                AuthState.Error(result.exceptionOrNull()?.message)
            }
        }
    }

    fun logout() {
        repo.logout()
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val userId: String) : AuthState() // ✅ include userId
    data class Error(val message: String?) : AuthState()
}
