package com.example.kotlinquizzes.feature.quiz.presentation.quizresults

import androidx.lifecycle.SavedStateHandle
import com.example.kotlinquizzes.core.navigation.NavigationConstants
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinquizzes.feature.quiz.presentation.quizresults.QuizResultsContract.QuizResultsAction
import com.example.kotlinquizzes.feature.quiz.presentation.quizresults.QuizResultsContract.QuizResultsEffect
import com.example.kotlinquizzes.feature.quiz.presentation.quizresults.QuizResultsContract.QuizResultsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizResultsViewModel @Inject constructor(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val totalQuestions: Int =
        checkNotNull(savedStateHandle[NavigationConstants.Args.TOTAL_QUESTIONS])
    private val correctAnswers: Int =
        checkNotNull(savedStateHandle[NavigationConstants.Args.CORRECT_ANSWERS])

    private val _state = MutableStateFlow(
        QuizResultsState(
            totalQuestions = totalQuestions, correctAnswers = correctAnswers,
        )
    )
    val state: StateFlow<QuizResultsState> = _state.asStateFlow()

    private val _effect = Channel<QuizResultsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onAction(action: QuizResultsAction) {
        when (action) {
            QuizResultsAction.BackToHomeClicked -> {
                viewModelScope.launch {
                    _effect.send(QuizResultsEffect.NavigateToHome)
                }
            }
        }
    }
}
