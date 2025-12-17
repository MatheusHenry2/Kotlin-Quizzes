package com.example.kotlinquizzes.auth.model

sealed interface SignInResult {
    data class Success(val user: UserData) : SignInResult
    data object Cancelled : SignInResult
    data class Failure(val error: Throwable) : SignInResult
}