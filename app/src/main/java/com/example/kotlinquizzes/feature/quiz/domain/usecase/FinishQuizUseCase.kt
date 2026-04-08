package com.example.kotlinquizzes.feature.quiz.domain.usecase

import android.util.Log
import com.example.kotlinquizzes.core.utils.Constants.TAG
import com.example.kotlinquizzes.feature.quiz.domain.repository.QuizRepository
import javax.inject.Inject

/**
 * Encapsulates the bookkeeping that happens when the user answers the last
 * question of a quiz: progress is cleared, the quiz is marked completed, and
 * if it was the initial assessment we flip the assessment flag and eagerly
 * generate the first batch of adaptive quizzes so the list is populated when
 * the user returns home.
 *
 * Centralising this here removes the duplicated [INITIAL_ASSESSMENT_ID]
 * constant and pulls multi-step business logic out of [QuizViewModel].
 */
class FinishQuizUseCase @Inject constructor(
    private val quizRepository: QuizRepository,
) {

    suspend operator fun invoke(quizId: String) {
        try {
            quizRepository.clearQuizProgress(quizId)
            quizRepository.markQuizCompleted(quizId)
        } catch (e: Exception) {
            Log.e(TAG, "FinishQuizUseCase: completion bookkeeping failed", e)
        }

        if (quizId != INITIAL_ASSESSMENT_ID) return

        try {
            quizRepository.markInitialAssessmentCompleted()
            quizRepository.generateAdaptiveQuizzes()
        } catch (e: Exception) {
            Log.e(TAG, "FinishQuizUseCase: initial assessment follow-up failed", e)
        }
    }

    companion object {
        const val INITIAL_ASSESSMENT_ID = "kotlin_android_assessment"
    }
}
