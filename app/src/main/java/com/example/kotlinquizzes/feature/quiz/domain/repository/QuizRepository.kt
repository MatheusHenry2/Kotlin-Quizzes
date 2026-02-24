package com.example.kotlinquizzes.feature.quiz.domain.repository

import com.example.kotlinquizzes.feature.quiz.domain.model.Quiz

interface QuizRepository {
    suspend fun getQuizzes(): List<Quiz>
    suspend fun getQuizById(quizId: String): Quiz?
}
