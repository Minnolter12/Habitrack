package com.minnolter.habitrack.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class AuthUserState(
    val isAuthenticated: Boolean = false,
    val uid: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null
)

class FirebaseAuthManager(
    private val context: Context,
    private val webClientId: String = "123456789012-abcdefghijklmnopqrstuvwxyz.apps.googleusercontent.com"
) {
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val credentialManager: CredentialManager by lazy { CredentialManager.create(context) }

    val authState: Flow<AuthUserState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            trySend(
                AuthUserState(
                    isAuthenticated = user != null,
                    uid = user?.uid,
                    displayName = user?.displayName ?: user?.email?.substringBefore("@"),
                    email = user?.email,
                    photoUrl = user?.photoUrl?.toString()
                )
            )
        }
        runCatching { firebaseAuth.addAuthStateListener(listener) }
        awaitClose { runCatching { firebaseAuth.removeAuthStateListener(listener) } }
    }

    val currentUser: FirebaseUser? get() = runCatching { firebaseAuth.currentUser }.getOrNull()

    private fun getWebClientId(): String? {
        return runCatching {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else null
        }.getOrNull()
    }

    suspend fun signInWithGoogle(activityContext: Context): Result<AuthUserState> {
        return runCatching {
            val clientId = getWebClientId()
                ?: throw IllegalStateException("Google Sign-In is not enabled in your Firebase Console. Please enable Google Sign-In in Firebase Console and re-download google-services.json.")

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = activityContext,
                request = request
            )

            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user ?: throw IllegalStateException("Firebase sign in returned null user")

            AuthUserState(
                isAuthenticated = true,
                uid = user.uid,
                displayName = user.displayName ?: user.email?.substringBefore("@"),
                email = user.email,
                photoUrl = user.photoUrl?.toString()
            )
        }
    }

    fun signOut() {
        runCatching { firebaseAuth.signOut() }
    }
}
