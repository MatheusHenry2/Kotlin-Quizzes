package com.example.kotlinquizzes.feature.quiz.domain.model

/**
 * Raw answer counters persisted by [QuizUserStateDataStore].
 * Aggregation into analytics belongs in [GetLearningInsightsUseCase].
 */
data class InsightsSnapshot(
    val totalCorrect: Int,
    val totalIncorrect: Int,
    val completedQuizCount: Int,
    val tagAttempts: Map<String, Int>,
    val tagMistakes: Map<String, Int>,
)
