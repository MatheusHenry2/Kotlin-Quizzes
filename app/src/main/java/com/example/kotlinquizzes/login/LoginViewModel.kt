package com.example.kotlinquizzes.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinquizzes.login.LoginContract.Effect
import com.example.kotlinquizzes.login.LoginContract.Intent
import com.example.kotlinquizzes.login.LoginContract.State
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class LoginViewModel : ViewModel() {
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: Intent) {
        when (intent) {
            is Intent.GoogleSignInClicked -> handleGoogleSignIn()
            is Intent.BackClicked -> handleBack()
        }
    }

    private fun handleGoogleSignIn() {
    }

    private fun handleBack() {
    }
}
