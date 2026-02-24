package com.example.kotlinquizzes.feature.presentation.login

object LoginContract {
    data class LoginState(
        val isLoading: Boolean = false,
    )

    sealed interface LoginAction {
        data object GoogleSignInClicked : LoginAction

        data object BackClicked : LoginAction
    }

    sealed interface LoginEffect {
        data object NavigateToHome : LoginEffect

        data object NavigateBack : LoginEffect
    }
}
