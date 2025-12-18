package com.example.kotlinquizzes.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.kotlinquizzes.auth.model.SignInResult
import com.example.kotlinquizzes.auth.model.UserData
import com.example.kotlinquizzes.utils.Constants.TAG
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleAuthClient(private val context: Context, private val firebaseAuth: FirebaseAuth) {
    private val credentialManager = CredentialManager.create(context)

    suspend fun signIn(webClientId: String): SignInResult {
        Log.i(TAG, "GoogleAuthClient signIn: start")
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            Log.i(TAG, "GoogleAuthClient signIn: requesting credential")
            val result = credentialManager.getCredential(context, request)
            handleSignInResult(result)
        } catch (exception: GetCredentialCancellationException) {
            Log.i(TAG, "GoogleAuthClient signIn: cancelled by user")
            SignInResult.Cancelled
        } catch (exception: Exception) {
            Log.d(TAG, "GoogleAuthClient signIn: failure", exception)
            SignInResult.Failure(exception)
        }
    }

    private suspend fun handleSignInResult(result: GetCredentialResponse): SignInResult {
        Log.i(TAG, "GoogleAuthClient handleSignInResult: start")
        val credential = result.credential
        if (credential !is CustomCredential || credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            Log.i(TAG, "GoogleAuthClient.handleSignInResult: invalid credential type")
            return SignInResult.Failure(IllegalArgumentException("Invalid credential type"))
        }
        val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
        val firebaseCredential = GoogleAuthProvider.getCredential(googleCred.idToken, null)
        val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
        val user = authResult.user
        if (user == null) {
            Log.i(TAG, "GoogleAuthClient handleSignInResult: firebase user is null")
            return SignInResult.Failure(IllegalStateException("User is null"))
        }
        Log.i(TAG, "GoogleAuthClient handleSignInResult: success")
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
