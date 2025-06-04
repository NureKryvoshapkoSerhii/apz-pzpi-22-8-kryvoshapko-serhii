package com.example.nutritrack.data.auth

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object GoogleAuth {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var appContext: Context
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun initialize(context: Context) {
        appContext = context.applicationContext
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("236833381634-jru8akdea0h7mkip711vs7n40e5u6a1e.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun signIn(launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>) {
        googleSignInClient.signOut().addOnCompleteListener {
            val pendingIntent: PendingIntent = googleSignInClient.signInIntent
                .let { intent ->
                    PendingIntent.getActivity(
                        appContext,
                        0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }

            val intentSender: IntentSender = pendingIntent.intentSender
            val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
            launcher.launch(intentSenderRequest)
        }
    }

    suspend fun handleSignInResult(data: Intent?): String {
        return suspendCancellableCoroutine { continuation ->
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                val googleIdToken = account.idToken

                val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            auth.currentUser?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                                if (tokenTask.isSuccessful) {
                                    val firebaseIdToken = tokenTask.result?.token
                                    if (firebaseIdToken != null) {
                                        continuation.resume(firebaseIdToken)
                                    } else {
                                        continuation.resumeWithException(Exception("Firebase ID token is null"))
                                    }
                                } else {
                                    continuation.resumeWithException(
                                        tokenTask.exception
                                            ?: Exception("Failed to get Firebase ID token")
                                    )
                                }
                            }
                        } else {
                            continuation.resumeWithException(
                                authTask.exception ?: Exception("Firebase Auth failed")
                            )
                        }
                    }
            } catch (e: ApiException) {
                continuation.resumeWithException(e)
            }
        }
    }
}