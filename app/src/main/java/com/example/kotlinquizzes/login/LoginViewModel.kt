package com.example.kotlinquizzes.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val _state = MutableStateFlow(LoginContract.LoginState())
    val state: StateFlow<LoginContract.LoginState> = _state.asStateFlow()
    private val _effect = Channel<LoginContract.LoginEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onAction(action: LoginContract.LoginAction) {
        when (action) {
            is LoginContract.LoginAction.GoogleSignInClicked -> handleGoogleSignIn()
            is LoginContract.LoginAction.BackClicked -> handleBack()
        }
    }

    private fun handleGoogleSignIn() {
        viewModelScope.launch {
            _effect.send(LoginContract.LoginEffect.NavigateToHome)
        }
    }

    private fun handleBack() {
        viewModelScope.launch {
            _effect.send(LoginContract.LoginEffect.NavigateBack)
        }
    }
}
