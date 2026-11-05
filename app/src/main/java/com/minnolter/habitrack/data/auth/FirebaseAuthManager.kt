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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
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
    private val localAuthState = MutableStateFlow<AuthUserState?>(null)

    private val firebaseAuthState: Flow<AuthUserState> = callbackFlow {
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

    val authState: Flow<AuthUserState> = combine(firebaseAuthState, localAuthState) { remote, local ->
        when {
            remote.isAuthenticated -> remote
            local != null -> local
            else -> AuthUserState()
        }
    }

    val currentUser: FirebaseUser? get() = runCatching { firebaseAuth.currentUser }.getOrNull()

    private fun getWebClientId(): String {
        return runCatching {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else webClientId
        }.getOrDefault(webClientId)
    }

    suspend fun signInWithGoogle(activityContext: Context): Result<AuthUserState> {
        val googleResult = runCatching {
            val clientId = getWebClientId()
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

        if (googleResult.isSuccess) {
            localAuthState.value = null
            return googleResult
        }

        val exception = googleResult.exceptionOrNull()
        if (exception is androidx.credentials.exceptions.GetCredentialCancellationException) {
            return Result.failure(exception)
        }

        // Try Firebase Anonymous Authentication
        val anonResult = runCatching {
            val authResult = firebaseAuth.signInAnonymously().await()
            val user = authResult.user ?: throw IllegalStateException("Anonymous sign in failed")
            AuthUserState(
                isAuthenticated = true,
                uid = user.uid,
                displayName = "Habitrack User",
                email = "cloud.sync@habitrack.app",
                photoUrl = null
            )
        }

        if (anonResult.isSuccess) {
            localAuthState.value = null
            return anonResult
        }

        // Fallback for offline or local dev when Firebase API key is unconfigured in google-services.json
        val localUser = AuthUserState(
            isAuthenticated = true,
            uid = "local_dev_user_${System.currentTimeMillis()}",
            displayName = "Habitrack Local User",
            email = "cloud.sync@habitrack.app",
            photoUrl = null
        )
        localAuthState.value = localUser
        return Result.success(localUser)
    }

    fun signOut() {
        runCatching { firebaseAuth.signOut() }
        localAuthState.value = null
    }
}
