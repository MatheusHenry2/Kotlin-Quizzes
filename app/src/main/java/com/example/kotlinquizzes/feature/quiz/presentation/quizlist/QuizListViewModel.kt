package com.example.kotlinquizzes.feature.quiz.presentation.quizlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinquizzes.R
import com.example.kotlinquizzes.core.ui.event.UiEventManager
import com.example.kotlinquizzes.core.utils.Constants.TAG
import com.example.kotlinquizzes.feature.quiz.domain.repository.QuizRepository
import com.example.kotlinquizzes.feature.quiz.presentation.quizlist.QuizListContract.QuizListAction
import com.example.kotlinquizzes.feature.quiz.presentation.quizlist.QuizListContract.QuizListEffect
import com.example.kotlinquizzes.feature.quiz.presentation.quizlist.QuizListContract.QuizListState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class QuizListViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val firebaseAuth: FirebaseAuth,
    private val uiEventManager: UiEventManager,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizListState())
    val state: StateFlow<QuizListState> = _state.asStateFlow()

    private val _effect = Channel<QuizListEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private companion object {
        private const val FALLBACK_USER_NAME = "User"
        private const val REFRESH_DELAY_MS = 800L
    }

    init {
        loadQuizzes()
    }

    fun onAction(action: QuizListAction) {
        when (action) {
            is QuizListAction.QuizClicked -> {
                Log.d(TAG, "Quiz selected with ID: ${action.quizId}")
                viewModelScope.launch {
                    _effect.send(QuizListEffect.NavigateToQuiz(action.quizId))
                }
            }
            QuizListAction.DismissLevelingDialog -> {
                Log.d(TAG, "Leveling Dialog dismissed")
                _state.update { it.copy(showLevelingDialog = false) }
            }
            QuizListAction.StartLevelingQuiz -> {
                Log.d(TAG, "Leveling Quiz started from Dialog")
                _state.update { it.copy(showLevelingDialog = false) }
                viewModelScope.launch {
                    _effect.send(QuizListEffect.NavigateToQuiz("kotlin_android_assessment"))
                }
            }
            QuizListAction.RetryClicked -> loadQuizzes()
            QuizListAction.RefreshPulled -> refreshQuizzes()
        }
    }

    private fun loadUserName(): String {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Log.d(TAG, "QuizListViewModel: Firebase currentUser is null")
            return FALLBACK_USER_NAME
        }
        val userName = currentUser.displayName?.trim().orEmpty()
        if (userName.isBlank()) {
            Log.d(TAG, "QuizListViewModel: Firebase displayName is blank")
            return FALLBACK_USER_NAME
        }
        return userName
    }

    private fun refreshQuizzes() {
        viewModelScope.launch {
            Log.d(TAG, "QuizListViewModel: refreshQuizzes started")
            _state.update { it.copy(isRefreshing = true) }
            try {
                delay(REFRESH_DELAY_MS)
                val quizzes = quizRepository.getQuizzes()
                Log.d(TAG, "QuizListViewModel: refreshQuizzes success, count=${quizzes.size}")
                _state.update {
                    it.copy(isRefreshing = false, quizzes = quizzes, errorMessageResId = null)
                }
                uiEventManager.showSuccess(R.string.snackbar_refresh_success)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "QuizListViewModel: refreshQuizzes failed", e)
                _state.update { it.copy(isRefreshing = false) }
                uiEventManager.showError(R.string.snackbar_refresh_failed)
            }
        }
    }

    private fun loadQuizzes() {
        viewModelScope.launch {
            Log.d(TAG, "QuizListViewModel: loadQuizzes started")
            _state.update {
                it.copy(isLoading = true, errorMessageResId = null, userName = loadUserName())
            }
            try {
                val quizzes = quizRepository.getQuizzes()
                Log.d(TAG, "QuizListViewModel: loadQuizzes success, count=${quizzes.size}")
                _state.update {
                    it.copy(isLoading = false, quizzes = quizzes, showLevelingDialog = true)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "QuizListViewModel: loadQuizzes failed", e)
                _state.update {
                    it.copy(isLoading = false, errorMessageResId = R.string.error_failed_load_quizzes)
                }
                uiEventManager.showError(R.string.snackbar_error_generic)
            }
        }
    }
}
