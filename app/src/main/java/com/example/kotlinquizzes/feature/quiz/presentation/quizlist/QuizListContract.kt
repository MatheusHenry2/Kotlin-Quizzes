package com.example.kotlinquizzes.feature.quiz.presentation.quizlist

import androidx.annotation.StringRes
import com.example.kotlinquizzes.feature.quiz.domain.model.Quiz

object QuizListContract {

    data class QuizListState(
        val isLoading: Boolean = false,
        val userName: String = "",
        val quizzes: List<Quiz> = emptyList(),
        @StringRes val errorMessageResId: Int? = null,
    )

    sealed interface QuizListAction {
        data object RetryClicked : QuizListAction
        data class QuizClicked(val quizId: String) : QuizListAction
    }

    sealed interface QuizListEffect {
        data class NavigateToQuiz(val quizId: String) : QuizListEffect
    }
}