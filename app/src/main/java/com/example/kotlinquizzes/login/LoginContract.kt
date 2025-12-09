package com.example.kotlinquizzes.login

object LoginContract {
    data class State(
        val isLoading: Boolean = false,
    )

    sealed class Intent {
        data object GoogleSignInClicked : Intent()
        data object BackClicked : Intent()
    }

    sealed class Effect {
        data object NavigateToHome : Effect()
        data object NavigateBack : Effect()
    }
}
