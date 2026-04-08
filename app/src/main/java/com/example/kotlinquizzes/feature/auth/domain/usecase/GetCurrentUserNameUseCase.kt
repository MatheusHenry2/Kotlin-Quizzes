package com.example.kotlinquizzes.feature.auth.domain.usecase

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

/**
 * Resolves the display name to show in the UI, applying a fallback when the
 * Firebase user is missing or has no display name. Encapsulates the only
 * decision point so the ViewModel doesn't need to depend on [FirebaseAuth].
 */
class GetCurrentUserNameUseCase @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {
    operator fun invoke(): String {
        val displayName = firebaseAuth.currentUser?.displayName?.trim().orEmpty()
        return displayName.ifBlank { FALLBACK_USER_NAME }
    }

    private companion object {
        const val FALLBACK_USER_NAME = "User"
    }
}
