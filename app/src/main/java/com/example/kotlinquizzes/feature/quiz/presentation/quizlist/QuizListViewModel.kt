package com.example.kotlinquizzes.feature.quiz.presentation.quizlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import javax.inject.Inject

@HiltViewModel
class QuizListViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val firebaseAuth: FirebaseAuth,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizListState())
    val state: StateFlow<QuizListState> = _state.asStateFlow()

    private val _effect = Channel<QuizListEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadQuizzes()
    }

    fun onAction(action: QuizListAction) {
        when (action) {
            is QuizListAction.QuizClicked -> {
                viewModelScope.launch {
                    _effect.send(QuizListEffect.NavigateToQuiz(action.quizId))
                }
            }
            QuizListAction.RetryClicked -> loadQuizzes()
        }
    }

    private fun loadQuizzes() {
        viewModelScope.launch {
            val name = firebaseAuth.currentUser?.displayName?.trim().orEmpty()

            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    userName = if (name.isBlank()) "User" else name
                )
            }

            runCatching { quizRepository.getQuizzes() }
                .onSuccess { quizzes ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            quizzes = quizzes
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load quizzes"
                        )
                    }
                }
        }
    }
}