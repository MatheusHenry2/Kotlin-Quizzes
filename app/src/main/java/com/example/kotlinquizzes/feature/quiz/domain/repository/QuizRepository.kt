package com.example.kotlinquizzes.feature.quiz.domain.repository

import com.example.kotlinquizzes.feature.quiz.domain.model.InsightsSnapshot
import com.example.kotlinquizzes.feature.quiz.domain.model.Quiz
import kotlinx.coroutines.flow.Flow

interface QuizRepository {

    fun observeAvailableQuizzes(): Flow<List<Quiz>>

    suspend fun getAvailableQuizzes(): List<Quiz>

    suspend fun getQuizById(quizId: String): Quiz?

    suspend fun saveQuizProgress(quizId: String, questionIndex: Int)
    suspend fun getQuizProgress(quizId: String): Int
    suspend fun clearQuizProgress(quizId: String)

    suspend fun recordAnswer(tags: List<String>, isCorrect: Boolean)

    suspend fun markQuizCompleted(quizId: String)

    suspend fun unlockNextQuiz()

    suspend fun getInsightsSnapshot(): InsightsSnapshot
}
