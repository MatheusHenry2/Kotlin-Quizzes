package com.example.kotlinquizzes.feature.quiz.domain.repository

import com.example.kotlinquizzes.feature.quiz.domain.model.InsightsSnapshot
import com.example.kotlinquizzes.feature.quiz.domain.model.Quiz
import kotlinx.coroutines.flow.Flow

interface QuizRepository {
    /**
     * Reactive list of quizzes the user can currently take. Emits whenever
     * the underlying state changes (completed quizzes, generated quizzes, etc.).
     * The initial assessment is hidden once it has been completed.
     */
    fun observeQuizzes(): Flow<List<Quiz>>

    /** Reactive flag indicating whether the initial assessment has been completed. */
    fun observeInitialAssessmentCompleted(): Flow<Boolean>

    suspend fun isInitialAssessmentCompleted(): Boolean

    /** One-shot fetch (used by quiz screens that need a snapshot). */
    suspend fun getQuizzes(): List<Quiz>

    suspend fun getQuizById(quizId: String): Quiz?

    suspend fun saveQuizProgress(quizId: String, questionIndex: Int)
    suspend fun getQuizProgress(quizId: String): Int
    suspend fun clearQuizProgress(quizId: String)

    /** Records an answer for tag-level mistake tracking. */
    suspend fun recordAnswer(tags: List<String>, isCorrect: Boolean)

    /** Marks a quiz as completed so it disappears from the active list. */
    suspend fun markQuizCompleted(quizId: String)

    /** Flips the initial-assessment flag (called after the assessment quiz is finished). */
    suspend fun markInitialAssessmentCompleted()

    /**
     * Generates and persists a new batch of adaptive quizzes based on the
     * user's weakest tags. Safe to call when the active list becomes empty.
     */
    suspend fun generateAdaptiveQuizzes()

    /**
     * Returns a raw snapshot of the user's answer counters. Aggregation/derived
     * analytics belong in [com.example.kotlinquizzes.feature.quiz.domain.usecase.GetLearningInsightsUseCase].
     */
    suspend fun getInsightsSnapshot(): InsightsSnapshot
}
