package com.example.kotlinquizzes.feature.quiz.domain.usecase

import android.util.Log
import com.example.kotlinquizzes.core.utils.Constants.TAG
import com.example.kotlinquizzes.feature.quiz.domain.repository.QuizRepository
import javax.inject.Inject

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

        try {
            quizRepository.unlockNextQuiz()
        } catch (e: Exception) {
            Log.e(TAG, "FinishQuizUseCase: unlockNextQuiz failed", e)
        }
    }
}
