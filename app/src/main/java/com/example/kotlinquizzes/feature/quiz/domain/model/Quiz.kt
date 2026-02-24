package com.example.kotlinquizzes.feature.quiz.domain.model

data class Quiz(
    val id: String,
    val title: String,
    val questions: List<Question>
)
