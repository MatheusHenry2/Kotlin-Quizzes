package com.example.kotlinquizzes.feature.quiz.domain.model

data class LearningInsights(
    val totalQuizzesCompleted: Int,
    val totalCorrect: Int,
    val totalIncorrect: Int,
    val accuracyPercent: Int,
    val masteryByCategory: List<CategoryMastery>,
    val topicsToImprove: List<WeakTopic>,
)

data class CategoryMastery(
    val tag: String,
    val masteryPercent: Int,
)

data class WeakTopic(
    val tag: String,
    val errorRatePercent: Int,
)
