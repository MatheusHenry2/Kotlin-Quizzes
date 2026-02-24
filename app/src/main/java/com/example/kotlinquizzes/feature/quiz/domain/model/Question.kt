package com.example.kotlinquizzes.feature.quiz.domain.model

data class Question(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val tags: List<String>
)
