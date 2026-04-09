package com.example.kotlinquizzes.feature.quiz.presentation.quizlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinquizzes.R
import com.example.kotlinquizzes.core.ui.event.UiEventManager
import com.example.kotlinquizzes.core.utils.Constants.TAG
import com.example.kotlinquizzes.feature.auth.domain.usecase.GetCurrentUserNameUseCase
import com.example.kotlinquizzes.feature.quiz.domain.repository.QuizRepository
import com.example.kotlinquizzes.feature.quiz.domain.usecase.EnsureAdaptiveQuizzesUseCase
import com.example.kotlinquizzes.feature.quiz.presentation.quizlist.QuizListContract.QuizListAction
import com.example.kotlinquizzes.feature.quiz.presentation.quizlist.QuizListContract.QuizListEffect
import com.example.kotlinquizzes.feature.quiz.presentation.quizlist.QuizListContract.QuizListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class QuizListViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val getCurrentUserNameUseCase: GetCurrentUserNameUseCase,
    private val ensureAdaptiveQuizzesUseCase: EnsureAdaptiveQuizzesUseCase,
    private val uiEventManager: UiEventManager,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizListState())
    val state: StateFlow<QuizListState> = _state.asStateFlow()

    private val _effect = Channel<QuizListEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private companion object {
        const val REFRESH_DELAY_MS = 800L
    }

    private var isGenerating = false
    private var hasShownLevelingDialog = false

    init {
        observeQuizzes()
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
            QuizListAction.RetryClicked -> observeQuizzes()
            QuizListAction.RefreshPulled -> refreshQuizzes()
        }
    }

    private fun refreshQuizzes() {
        viewModelScope.launch {
            Log.d(TAG, "QuizListViewModel: refreshQuizzes started")
            _state.update { it.copy(isRefreshing = true) }
            try {
                delay(REFRESH_DELAY_MS)
                val quizzes = quizRepository.observeQuizzes().first()
                Log.d(TAG, "QuizListViewModel: refreshQuizzes success, count=${quizzes.size}")
                _state.update {
                    it.copy(isRefreshing = false, errorMessageResId = null)
                }
                ensureQuizzesAvailable(quizzes.size)
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

    private fun observeQuizzes() {
        viewModelScope.launch {
            Log.d(TAG, "QuizListViewModel: observeQuizzes started")
            _state.update {
                it.copy(isLoading = true, errorMessageResId = null, userName = getCurrentUserNameUseCase())
            }
            try {
                quizRepository.observeQuizzes().collect { quizzes ->
                    val assessmentDone = quizRepository.isInitialAssessmentCompleted()
                    Log.d(
                        TAG,
                        "QuizListViewModel: list updated count=${quizzes.size} assessmentDone=$assessmentDone",
                    )
                    // Show the leveling dialog only the first time the list loads
                    // and only if the initial assessment has not been completed.
                    val shouldShowDialog = !assessmentDone && !hasShownLevelingDialog
                    if (shouldShowDialog) hasShownLevelingDialog = true
                    _state.update {
                        it.copy(
                            isLoading = false,
                            quizzes = quizzes,
                            showLevelingDialog = if (assessmentDone) false else it.showLevelingDialog || shouldShowDialog,
                        )
                    }
                    ensureQuizzesAvailable(quizzes.size)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "QuizListViewModel: observeQuizzes failed", e)
                _state.update {
                    it.copy(isLoading = false, errorMessageResId = R.string.error_failed_load_quizzes)
                }
                uiEventManager.showError(R.string.snackbar_error_generic)
            }
        }
    }

    /**
     * Delegates the "should we generate adaptive quizzes?" decision to
     * [EnsureAdaptiveQuizzesUseCase] and only manages the UI loading flag here.
     */
    private fun ensureQuizzesAvailable(currentCount: Int) {
        if (isGenerating) return
        viewModelScope.launch {
            isGenerating = true
            _state.update { it.copy(isGenerating = true) }
            try {
                val triggered = ensureAdaptiveQuizzesUseCase(currentCount)
                if (triggered) {
                    Log.d(TAG, "QuizListViewModel: adaptive quiz generation triggered")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "QuizListViewModel: ensureAdaptiveQuizzesUseCase failed", e)
                uiEventManager.showError(R.string.snackbar_error_generic)
            } finally {
                isGenerating = false
                _state.update { it.copy(isGenerating = false) }
            }
        }
    }
}
