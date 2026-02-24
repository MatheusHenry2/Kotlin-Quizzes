package com.example.kotlinquizzes.feature.quiz.presentation.quizlist

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class QuizListViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(QuizListContract.QuizListState())
    val state: StateFlow<QuizListContract.QuizListState> = _state.asStateFlow()

    private val _effect = Channel<QuizListContract.QuizListEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onAction(action: QuizListContract.QuizListAction) {
    }
}