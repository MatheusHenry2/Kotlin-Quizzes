package com.example.kotlinquizzes.feature.quiz.presentation.quizlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class QuizListViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val firebaseAuth: FirebaseAuth,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizListState())
    val state: StateFlow<QuizListState> = _state.asStateFlow()

    private val _effect = Channel<QuizListEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private companion object {
        private const val FALLBACK_USER_NAME = "User"
    }

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

    private fun loadUserName(): String {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Log.d(TAG, "QuizListViewModel: Firebase currentUser is null, using fallback user name")
            return FALLBACK_USER_NAME
        }
        val userName = currentUser.displayName?.trim().orEmpty()
        if (userName.isBlank()) {
            Log.d(TAG, "QuizListViewModel: Firebase displayName is blank, using fallback user name")
            return FALLBACK_USER_NAME
        }
        Log.d(TAG, "QuizListViewModel: Firebase user name loaded successfully")
        return userName
    }

    private fun loadQuizzes() {
        viewModelScope.launch {
            Log.d(TAG, "QuizListViewModel: loadQuizzes started")

            _state.update {
                it.copy(isLoading = true, errorMessage = null, userName = loadUserName())
            }

            try {
                val quizzes = quizRepository.getQuizzes()
                Log.d(TAG, "QuizListViewModel: loadQuizzes success, count=${quizzes.size}")

                _state.update {
                    it.copy(isLoading = false, quizzes = quizzes)
                }

            } catch (e: CancellationException) {
                Log.d(TAG, "QuizListViewModel: loadQuizzes cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "QuizListViewModel: loadQuizzes failed", e)
                _state.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load quizzes")
                }
            }
        }
    }
}