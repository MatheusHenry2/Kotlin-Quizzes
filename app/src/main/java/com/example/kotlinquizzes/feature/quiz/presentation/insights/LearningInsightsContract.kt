package com.example.kotlinquizzes.feature.quiz.presentation.insights

import androidx.annotation.StringRes
import com.example.kotlinquizzes.feature.quiz.domain.model.LearningInsights

object LearningInsightsContract {

    data class LearningInsightsState(
        val isLoading: Boolean = true,
        val insights: LearningInsights? = null,
        @StringRes val errorMessageResId: Int? = null,
    )

    sealed interface LearningInsightsAction {
        data object RetryClicked : LearningInsightsAction
    }
}
