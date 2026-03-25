package com.example.kotlinquizzes.feature.quiz.presentation.quizlist

import androidx.annotation.StringRes
import com.example.kotlinquizzes.feature.quiz.domain.model.Quiz

object QuizListContract {

    data class QuizListState(
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val userName: String = "",
        val quizzes: List<Quiz> = emptyList(),
        val showLevelingDialog: Boolean = false,
        @StringRes val errorMessageResId: Int? = null
    )

    sealed interface QuizListAction {
        data object RetryClicked : QuizListAction
        data object RefreshPulled : QuizListAction
        data class QuizClicked(val quizId: String) : QuizListAction
        data object DismissLevelingDialog : QuizListAction
        data object StartLevelingQuiz : QuizListAction
    }

    sealed interface QuizListEffect {
        data class NavigateToQuiz(val quizId: String) : QuizListEffect
    }
}