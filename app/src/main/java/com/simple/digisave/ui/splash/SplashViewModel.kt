package com.simple.digisave.ui.splash

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.simple.digisave.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    val isLoggedIn: Boolean get() = authRepository.isUserLoggedIn()
    val currentUserId: String? get() = firebaseAuth.currentUser?.uid
}
