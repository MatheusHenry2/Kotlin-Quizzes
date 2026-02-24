package com.example.kotlinquizzes.feature.quiz.presentation.quizlist

import com.example.kotlinquizzes.feature.quiz.domain.model.Quiz

object QuizListContract {

    data class QuizListState(
        val isLoading: Boolean = false,
        val userName: String = "User",
        val quizzes: List<Quiz> = emptyList(),
        val errorMessage: String? = null,
    )

    sealed interface QuizListAction {
        data object RetryClicked : QuizListAction
        data class QuizClicked(val quizId: String) : QuizListAction
    }

    sealed interface QuizListEffect {
        data class NavigateToQuiz(val quizId: String) : QuizListEffect
    }
}