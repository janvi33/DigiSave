package com.simple.digisave.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    //Sign up new user
    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("No user ID")
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("No user ID")
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    fun logout() {
        firebaseAuth.signOut()
    }

    fun isUserLoggedIn(): Boolean{
        return firebaseAuth.currentUser != null
    }

}