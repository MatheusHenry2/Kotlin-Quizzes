package com.example.kotlinquizzes.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.kotlinquizzes.auth.model.SignInResult
import com.example.kotlinquizzes.auth.model.UserData
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleAuthClient(private val context: Context, private val firebaseAuth: FirebaseAuth) {
    private val credentialManager = CredentialManager.create(context)

    suspend fun signIn(webClientId: String): SignInResult {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            handleSignInResult(result)
        } catch (exception: GetCredentialCancellationException) {
            SignInResult.Cancelled
        } catch (exception: Exception) {
            SignInResult.Failure(exception)
        }
    }

    private suspend fun handleSignInResult(result: GetCredentialResponse): SignInResult {
        val credential = result.credential

        if (credential !is CustomCredential || credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            return SignInResult.Failure(IllegalArgumentException("Invalid credential type"))
        }
        val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
        val firebaseCredential = GoogleAuthProvider.getCredential(googleCred.idToken, null)
        val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
        val user =
            authResult.user ?: return SignInResult.Failure(IllegalStateException("User is null"))

        return SignInResult.Success(
            UserData(
                user.uid,
                user.displayName,
                user.email,
                user.photoUrl?.toString()
            )
        )
    }
}
