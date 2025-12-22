package com.example.kotlinquizzes.quizlist

import androidx.lifecycle.ViewModel
import com.example.kotlinquizzes.quizlist.QuizListContract.QuizListAction
import com.example.kotlinquizzes.quizlist.QuizListContract.QuizListEffect
import com.example.kotlinquizzes.quizlist.QuizListContract.QuizListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class QuizListViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(QuizListState())
    val state: StateFlow<QuizListState> = _state.asStateFlow()

    private val _effect = Channel<QuizListEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onAction(action: QuizListAction) {
    }
}