package com.example.kotlinquizzes.feature.quiz.presentation.quiz

import androidx.annotation.StringRes
import com.example.kotlinquizzes.feature.quiz.domain.model.Question

object QuizContract {

    data class QuizState(
        val isLoading: Boolean = false,
        val quizTitle: String = "",
        val questions: List<Question> = emptyList(),
        val currentQuestionIndex: Int = 0,
        val selectedOptionIndex: Int? = null,
        val correctAnswers: Int = 0,
        val isCheckingAnswer: Boolean = false,
        val selectedOptionIsCorrect: Boolean? = null,
        @StringRes val errorMessageResId: Int? = null,
    ) {
        val currentQuestion: Question?
            get() = questions.getOrNull(currentQuestionIndex)

        val totalQuestions: Int
            get() = questions.size

        val progress: Float
            get() = if (totalQuestions > 0) (currentQuestionIndex + 1).toFloat() / totalQuestions else 0f

        val isLastQuestion: Boolean
            get() = currentQuestionIndex >= totalQuestions - 1
    }

    sealed interface QuizAction {
        data class OptionSelected(val index: Int) : QuizAction
        data object NextClicked : QuizAction
        data object CloseClicked : QuizAction
    }

    sealed interface QuizEffect {
        data object NavigateBack : QuizEffect
        data class QuizFinished(val totalQuestions: Int, val correctAnswers: Int) : QuizEffect
        data object HapticFeedback : QuizEffect
    }
}
