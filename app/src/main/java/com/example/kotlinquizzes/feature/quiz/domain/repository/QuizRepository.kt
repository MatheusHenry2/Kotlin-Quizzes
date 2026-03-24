package com.example.kotlinquizzes.feature.quiz.domain.repository

import com.example.kotlinquizzes.feature.quiz.domain.model.Quiz

interface QuizRepository {
    suspend fun getQuizzes(): List<Quiz>
    suspend fun getQuizById(quizId: String): Quiz?
    suspend fun saveQuizProgress(quizId: String, questionIndex: Int)
    suspend fun getQuizProgress(quizId: String): Int
    suspend fun clearQuizProgress(quizId: String)
}
