package com.example.kotlinquizzes.feature.quiz.domain.model

data class LearningInsights(
    val totalQuizzesCompleted: Int,
    val totalCorrect: Int,
    val totalIncorrect: Int,
    val accuracyPercent: Int,
    val masteryByCategory: List<CategoryMastery>,
    val topicsToImprove: List<WeakTopic>,
)
