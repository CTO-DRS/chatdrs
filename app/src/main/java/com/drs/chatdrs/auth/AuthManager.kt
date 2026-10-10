package com.drs.chatdrs.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.drs.chatdrs.R
import com.drs.chatdrs.firebase.ChatDrsFirebase
import com.drs.chatdrs.firebase.FirestoreErrorHandler
import com.drs.chatdrs.firebase.OperationType
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object AuthManager {

    private const val TAG = "ChatDrsAuth"

    /**
     * Unwraps a Context to find the hosting Activity for Credential Manager UI sheets.
     */
    fun Context.findActivity(): Activity? {
        var ctx = this
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    /**
     * Resolves the Web Client ID generated from google-services.json.
     */
    fun getServerClientId(context: Context): String? {
        return try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resolve default_web_client_id", e)
            null
        }
    }

    /**
     * Persists or updates the authenticated Google user's profile in Firestore (/users/{userId}).
     */
    fun syncUserProfileToFirestore(context: Context, user: FirebaseUser) {
        try {
            val db = ChatDrsFirebase.getFirestore(context)
            val userDocRef = db.collection("users").document(user.uid)
            val displayName = (user.displayName ?: user.email?.substringBefore("@") ?: "مستخدم ChatDrs")
                .trim()
                .ifEmpty { "مستخدم ChatDrs" }
                .take(100)

            val payload = mapOf(
                "userId" to user.uid,
                "displayName" to displayName,
                "status" to "متاح في ChatDrs",
                "isOnline" to true,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            userDocRef.set(payload, SetOptions.merge())
                .addOnFailureListener { exception ->
                    FirestoreErrorHandler.handleFirestoreError(
                        exception,
                        OperationType.UPDATE,
                        userDocRef.path
                    )
                }
        } catch (e: Exception) {
            Log.w(TAG, "Could not sync user profile to Firestore", e)
        }
    }

    /**
     * Attempts silent auto-sign-in on app launch using authorized accounts.
     */
    fun attemptAutoSignIn(
        context: Context,
        credentialManager: CredentialManager,
        onAuthSuccess: (FirebaseUser) -> Unit,
        onUnauthenticated: () -> Unit,
        scope: CoroutineScope
    ) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            onAuthSuccess(currentUser)
            return
        }

        val clientId = getServerClientId(context)
        if (clientId == null) {
            onUnauthenticated()
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        syncUserProfileToFirestore(context, user)
                        onAuthSuccess(user)
                    } else {
                        onUnauthenticated()
                    }
                } else {
                    onUnauthenticated()
                }
            } catch (e: Exception) {
                // Expected on clean devices or when no accounts are pre-authorized
                onUnauthenticated()
            }
        }
    }

    /**
     * Interactive Google Sign-In triggered by clicking the Sign In button.
     */
    fun signInWithGoogle(
        activity: Activity,
        credentialManager: CredentialManager,
        onAuthSuccess: (FirebaseUser) -> Unit,
        onAuthError: (String) -> Unit,
        onCancelled: () -> Unit,
        scope: CoroutineScope
    ) {
        val clientId = getServerClientId(activity)
        if (clientId == null) {
            onAuthError("إعدادات Google Sign-In غير متوفرة (default_web_client_id مفقود).")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        syncUserProfileToFirestore(activity, user)
                        onAuthSuccess(user)
                    } else {
                        onAuthError("فشل استرداد بيانات المستخدم بعد تسجيل الدخول.")
                    }
                } else {
                    onAuthError("نوع بيانات الاعتماد غير معروف.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In cancelled or dismissed: ${e.message}", e)
                onCancelled()
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                onAuthError(e.localizedMessage ?: "حدث خطأ أثناء تسجيل الدخول.")
            }
        }
    }

    /**
     * Signs out from Firebase and clears Credential Manager saved states.
     */
    fun signOut(
        context: Context,
        credentialManager: CredentialManager,
        onComplete: () -> Unit,
        scope: CoroutineScope
    ) {
        Firebase.auth.signOut()
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear credential state", e)
            } finally {
                onComplete()
            }
        }
    }
}
