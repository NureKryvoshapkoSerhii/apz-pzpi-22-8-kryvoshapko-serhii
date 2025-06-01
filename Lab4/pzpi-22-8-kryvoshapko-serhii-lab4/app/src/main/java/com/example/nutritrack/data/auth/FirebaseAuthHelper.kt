package com.example.nutritrack.data.auth

import android.util.Log
import com.example.nutritrack.data.api.ApiService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

object FirebaseAuthHelper {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun isUserAuthenticated(): Boolean {
        return auth.currentUser != null
    }

    suspend fun getIdToken(): String? {
        return try {
            val user = auth.currentUser
            if (user != null) {
                val tokenResult = user.getIdToken(true).await()
                tokenResult.token
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthHelper", "Failed to get idToken: $e")
            null
        }
    }

    fun getUid(): String? {
        return auth.currentUser?.uid
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser

    suspend fun loginAsUser(): Result<Boolean> {
        return try {
            val idToken = getIdToken()
            if (idToken != null) {
                val isUser = ApiService.checkUserExists(idToken)
                if (isUser) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Цей акаунт не зареєстрований як користувач"))
                }
            } else {
                Result.failure(Exception("Помилка автентифікації"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginAsConsultant(): Result<Boolean> {
        return try {
            val idToken = getIdToken()
            if (idToken != null) {
                val isConsultant = ApiService.checkConsultantExists(idToken)
                if (isConsultant) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Цей акаунт не зареєстрований як консультант"))
                }
            } else {
                Result.failure(Exception("Помилка автентифікації"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}