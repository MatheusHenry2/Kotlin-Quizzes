package com.example.kotlinquizzes.feature.quiz.domain.usecase

import com.example.kotlinquizzes.feature.quiz.domain.model.CategoryMastery
import com.example.kotlinquizzes.feature.quiz.domain.model.InsightsSnapshot
import com.example.kotlinquizzes.feature.quiz.domain.model.LearningInsights
import com.example.kotlinquizzes.feature.quiz.domain.model.WeakTopic
import com.example.kotlinquizzes.feature.quiz.domain.repository.QuizRepository
import javax.inject.Inject

/**
 * Aggregates the user's raw answer counters into the analytics shown on the
 * insights screen: overall accuracy, top-3 most-practiced categories with their
 * mastery %, and top-3 categories with the highest error rate.
 */
class GetLearningInsightsUseCase @Inject constructor(
    private val quizRepository: QuizRepository,
) {

    private companion object {
        const val TOP_LIMIT = 3
    }

    suspend operator fun invoke(): LearningInsights {
        val snapshot = quizRepository.getInsightsSnapshot()
        return snapshot.toInsights()
    }

    private fun InsightsSnapshot.toInsights(): LearningInsights {
        val totalAnswered = totalCorrect + totalIncorrect
        val accuracyPercent = if (totalAnswered == 0) 0 else (totalCorrect * 100) / totalAnswered

        val mastery = tagAttempts.entries
            .sortedByDescending { it.value }
            .take(TOP_LIMIT)
            .map { (tag, attempts) ->
                val mistakes = tagMistakes[tag] ?: 0
                val correct = (attempts - mistakes).coerceAtLeast(0)
                val pct = if (attempts == 0) 0 else (correct * 100) / attempts
                CategoryMastery(tag = tag, masteryPercent = pct)
            }

        val weak = tagAttempts.entries
            .mapNotNull { (tag, attempts) ->
                val mistakes = tagMistakes[tag] ?: 0
                if (mistakes == 0 || attempts == 0) return@mapNotNull null
                val rate = (mistakes * 100) / attempts
                WeakTopic(tag = tag, errorRatePercent = rate)
            }
            .sortedByDescending { it.errorRatePercent }
            .take(TOP_LIMIT)

        return LearningInsights(
            totalQuizzesCompleted = completedQuizCount,
            totalCorrect = totalCorrect,
            totalIncorrect = totalIncorrect,
            accuracyPercent = accuracyPercent,
            masteryByCategory = mastery,
            topicsToImprove = weak,
        )
    }
}
