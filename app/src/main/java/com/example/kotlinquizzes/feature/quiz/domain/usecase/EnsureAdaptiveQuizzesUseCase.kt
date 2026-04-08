package com.example.kotlinquizzes.feature.quiz.domain.usecase

import com.example.kotlinquizzes.feature.quiz.domain.repository.QuizRepository
import javax.inject.Inject

/**
 * Triggers a new batch of adaptive quizzes when the active list is empty,
 * but only if the user has already completed the initial assessment.
 *
 * Returns `true` when generation was actually requested so the caller can
 * surface a loading indicator. The concurrency guard (avoiding overlapping
 * triggers) is left to the caller, since it is a presentation concern.
 */
class EnsureAdaptiveQuizzesUseCase @Inject constructor(
    private val quizRepository: QuizRepository,
) {
    suspend operator fun invoke(currentQuizCount: Int): Boolean {
        if (currentQuizCount > 0) return false
        if (!quizRepository.isInitialAssessmentCompleted()) return false
        quizRepository.generateAdaptiveQuizzes()
        return true
    }
}
