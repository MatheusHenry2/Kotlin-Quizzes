package com.example.kotlinquizzes.feature.quiz.data.model

import kotlinx.serialization.Serializable

@Serializable
data class QuestionDto(
    val id: String,
    val text: String,
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val tags: List<String> = emptyList()
)
