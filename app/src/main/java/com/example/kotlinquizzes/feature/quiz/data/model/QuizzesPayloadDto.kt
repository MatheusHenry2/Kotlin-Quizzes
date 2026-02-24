package com.example.kotlinquizzes.feature.quiz.data.model

import kotlinx.serialization.Serializable

@Serializable
data class QuizzesPayloadDto(
    val quizzes: List<QuizDto> = emptyList()
)
