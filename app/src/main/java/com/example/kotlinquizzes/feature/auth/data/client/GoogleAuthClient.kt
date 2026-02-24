package com.example.kotlinquizzes.feature.auth.data.client

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.kotlinquizzes.core.utils.Constants
import com.example.kotlinquizzes.feature.auth.data.model.SignInResult
import com.example.kotlinquizzes.feature.auth.data.model.UserData
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class GoogleAuthClient(private val context: Context, private val firebaseAuth: FirebaseAuth) {
    private val credentialManager = CredentialManager.Companion.create(context)

    suspend fun signIn(webClientId: String): SignInResult {
        Log.i(Constants.TAG, "GoogleAuthClient signIn: start")
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            Log.i(Constants.TAG, "GoogleAuthClient signIn: requesting credential")
            val result = credentialManager.getCredential(context, request)
            handleSignInResult(result)
        } catch (exception: GetCredentialCancellationException) {
            Log.i(Constants.TAG, "GoogleAuthClient signIn: cancelled by user")
            SignInResult.Cancelled
        } catch (exception: Exception) {
            Log.d(Constants.TAG, "GoogleAuthClient signIn: failure", exception)
            SignInResult.Failure(exception)
        }
    }

    private suspend fun handleSignInResult(result: GetCredentialResponse): SignInResult {
        Log.i(Constants.TAG, "GoogleAuthClient handleSignInResult: start")
        val credential = result.credential
        if (credential !is CustomCredential || credential.type != GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            Log.i(Constants.TAG, "GoogleAuthClient.handleSignInResult: invalid credential type")
            return SignInResult.Failure(IllegalArgumentException("Invalid credential type"))
        }
        val googleCred = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.createFrom(credential.data)
        val firebaseCredential = com.google.firebase.auth.GoogleAuthProvider.getCredential(googleCred.idToken, null)
        val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
        val user = authResult.user
        if (user == null) {
            Log.i(Constants.TAG, "GoogleAuthClient handleSignInResult: firebase user is null")
            return SignInResult.Failure(IllegalStateException("User is null"))
        }
        Log.i(Constants.TAG, "GoogleAuthClient handleSignInResult: success")
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