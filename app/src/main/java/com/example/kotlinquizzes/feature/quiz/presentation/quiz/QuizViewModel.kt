package com.example.kotlinquizzes.feature.quiz.presentation.quiz

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinquizzes.R
import com.example.kotlinquizzes.core.navigation.NavigationConstants
import com.example.kotlinquizzes.core.ui.event.UiEventManager
import com.example.kotlinquizzes.core.utils.Constants.TAG
import com.example.kotlinquizzes.feature.quiz.domain.repository.QuizRepository
import com.example.kotlinquizzes.feature.quiz.domain.usecase.FinishQuizUseCase
import com.example.kotlinquizzes.feature.quiz.presentation.quiz.QuizContract.QuizAction
import com.example.kotlinquizzes.feature.quiz.presentation.quiz.QuizContract.QuizEffect
import com.example.kotlinquizzes.feature.quiz.presentation.quiz.QuizContract.QuizState
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
class QuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quizRepository: QuizRepository,
    private val finishQuiz: FinishQuizUseCase,
    private val uiEventManager: UiEventManager,
) : ViewModel() {

    private val quizId: String = checkNotNull(savedStateHandle[NavigationConstants.Args.QUIZ_ID])

    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()

    private val _effect = Channel<QuizEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadQuiz()
    }

    fun onAction(action: QuizAction) {
        when (action) {
            is QuizAction.OptionSelected -> {
                // Only allow selecting an option if not currently checking an answer
                if (!_state.value.isCheckingAnswer) {
                    _state.update { it.copy(selectedOptionIndex = action.index) }
                    viewModelScope.launch { _effect.send(QuizEffect.HapticFeedback) }
                }
            }
            QuizAction.NextClicked -> handleNext()
            QuizAction.CloseClicked -> {
                viewModelScope.launch { _effect.send(QuizEffect.NavigateBack) }
            }
        }
    }

    private fun loadQuiz() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val quiz = quizRepository.getQuizById(quizId)
                if (quiz == null) {
                    _state.update {
                        it.copy(isLoading = false, errorMessageResId = R.string.error_quiz_not_found)
                    }
                    return@launch
                }
                val savedProgress = quizRepository.getQuizProgress(quizId)
                val startIndex = savedProgress.coerceIn(0, quiz.questions.size - 1)
                
                Log.d(TAG, "Quiz loaded: ${quiz.title} at index $startIndex")
                
                _state.update {
                    it.copy(
                        isLoading = false,
                        quizTitle = quiz.title,
                        questions = quiz.questions,
                        currentQuestionIndex = startIndex,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "QuizViewModel: loadQuiz failed", e)
                _state.update {
                    it.copy(isLoading = false, errorMessageResId = R.string.error_failed_load_quiz)
                }
                uiEventManager.showError(R.string.snackbar_error_generic)
            }
        }
    }

    private fun handleNext() {
        val currentState = _state.value
        val selectedIndex = currentState.selectedOptionIndex ?: return
        val currentQuestion = currentState.currentQuestion ?: return

        // Prevent multiple clicks while checking answer
        if (currentState.isCheckingAnswer) return

        val isCorrect = selectedIndex == currentQuestion.correctIndex
        val newCorrectAnswers = if (isCorrect) currentState.correctAnswers + 1 else currentState.correctAnswers

        _state.update { it.copy(isCheckingAnswer = true, selectedOptionIsCorrect = isCorrect) }

        viewModelScope.launch {
            // Record tag-level statistics for adaptive generation.
            try {
                quizRepository.recordAnswer(currentQuestion.tags, isCorrect)
            } catch (e: Exception) {
                Log.e(TAG, "QuizViewModel: recordAnswer failed", e)
            }

            delay(1500L) // Show feedback for 1.5 seconds

            _state.update { it.copy(isCheckingAnswer = false, selectedOptionIsCorrect = null) }

            if (currentState.isLastQuestion) {
                Log.d(TAG, "Quiz finished. Final score: $newCorrectAnswers/${currentState.totalQuestions}")
                finishQuiz(quizId)
                _effect.send(
                    QuizEffect.QuizFinished(
                        totalQuestions = currentState.totalQuestions,
                        correctAnswers = newCorrectAnswers,
                    )
                )
            } else {
                Log.d(TAG, "Navigating to next question. Correct answers so far: $newCorrectAnswers")
                val nextIndex = currentState.currentQuestionIndex + 1
                _state.update {
                    it.copy(
                        currentQuestionIndex = nextIndex,
                        selectedOptionIndex = null,
                        correctAnswers = newCorrectAnswers,
                    )
                }
                try {
                    quizRepository.saveQuizProgress(quizId, nextIndex)
                } catch (e: Exception) {
                    Log.e(TAG, "QuizViewModel: saveQuizProgress failed", e)
                    uiEventManager.showError(R.string.snackbar_error_save_progress)
                }
            }
        }
    }
}
