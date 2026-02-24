package com.example.kotlinquizzes.feature.quiz.data.model

import kotlinx.serialization.Serializable

@Serializable
data class QuizDto(
    val id: String,
    val title: String,
    val questions: List<QuestionDto> = emptyList()
)
