package com.example.kotlinquizzes.feature.quiz.presentation.quizlist

object QuizListContract {
    data class QuizListState(
        val isLoading: Boolean = false,
    )

    sealed interface QuizListAction

    sealed interface QuizListEffect
}
