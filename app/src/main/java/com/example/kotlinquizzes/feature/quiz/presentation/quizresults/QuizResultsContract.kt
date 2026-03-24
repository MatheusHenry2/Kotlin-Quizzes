package com.example.kotlinquizzes.feature.quiz.presentation.quizresults

object QuizResultsContract {

    data class QuizResultsState(val totalQuestions: Int = 0, val correctAnswers: Int = 0) {
        val incorrectAnswers: Int
            get() = totalQuestions - correctAnswers

        val scorePercentage: Int
            get() = if (totalQuestions > 0) (correctAnswers * 100) / totalQuestions else 0
    }

    sealed interface QuizResultsAction {
        data object BackToHomeClicked : QuizResultsAction
    }

    sealed interface QuizResultsEffect {
        data object NavigateToHome : QuizResultsEffect
    }
}
