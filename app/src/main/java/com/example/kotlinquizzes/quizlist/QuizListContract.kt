package com.example.kotlinquizzes.quizlist

object QuizListContract {
    data class QuizListState(
        val isLoading: Boolean = false,
    )

    sealed interface QuizListAction

    sealed interface QuizListEffect
}
