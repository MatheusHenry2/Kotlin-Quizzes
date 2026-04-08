package com.example.kotlinquizzes.feature.auth.domain.usecase

import android.content.Context
import com.example.kotlinquizzes.R
import com.example.kotlinquizzes.feature.auth.data.client.GoogleAuthClient
import com.example.kotlinquizzes.feature.auth.data.model.SignInResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Resolves the Google web client id and delegates to [GoogleAuthClient].
 * Keeps the resource lookup and the auth call out of the ViewModel so the
 * VM doesn't need an [android.app.Application] dependency.
 */
class SignInWithGoogleUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val googleAuthClient: GoogleAuthClient,
) {
    suspend operator fun invoke(): SignInResult {
        val webClientId = context.getString(R.string.web_client_id)
        return googleAuthClient.signIn(webClientId)
    }
}
