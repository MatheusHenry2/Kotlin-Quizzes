package com.example.kotlinquizzes.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinquizzes.R
import com.example.kotlinquizzes.auth.GoogleAuthClient
import com.example.kotlinquizzes.auth.model.SignInResult
import com.example.kotlinquizzes.login.LoginContract.LoginAction
import com.example.kotlinquizzes.login.LoginContract.LoginEffect
import com.example.kotlinquizzes.login.LoginContract.LoginState
import com.example.kotlinquizzes.utils.Constants.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val googleAuthClient: GoogleAuthClient,
    private val application: Application,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _effect = Channel<LoginEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.GoogleSignInClicked -> handleGoogleSignIn()
            is LoginAction.BackClicked -> handleBack()
        }
    }

    private fun handleGoogleSignIn() {
        Log.i(TAG, "LoginViewModel handleGoogleSignIn: start")
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val webClientId = application.getString(R.string.web_client_id)
            Log.d(TAG, "Web Client ID: $webClientId")
            val result = googleAuthClient.signIn(webClientId)
            _state.update { it.copy(isLoading = false) }

            when (result) {
                is SignInResult.Success -> {
                    Log.i(TAG, "LoginViewModel handleGoogleSignIn: success")
                    _effect.send(LoginEffect.NavigateToHome)
                }

                is SignInResult.Cancelled -> {
                    Log.i(TAG, "LoginViewModel handleGoogleSignIn: cancelled")
                }

                is SignInResult.Failure -> {
                    Log.e(TAG, "LoginViewModel handleGoogleSignIn: failure", result.error)
                }
            }
        }
    }

    private fun handleBack() {
        Log.i(TAG, "LoginViewModel handleBack: start")
        viewModelScope.launch {
            _effect.send(LoginEffect.NavigateBack)
        }
    }
}