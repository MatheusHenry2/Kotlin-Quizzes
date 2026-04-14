package com.example.kotlinquizzes.feature.quiz.data.repository

import android.util.Log
import com.example.kotlinquizzes.core.utils.Constants.TAG
import com.example.kotlinquizzes.feature.quiz.data.local.db.dao.QuizDao
import com.example.kotlinquizzes.feature.quiz.data.local.db.dao.QuizProgressDao
import com.example.kotlinquizzes.feature.quiz.data.local.db.dao.TagPerformanceDao
import com.example.kotlinquizzes.feature.quiz.data.local.db.dao.UserStatsDao
import com.example.kotlinquizzes.feature.quiz.data.local.db.entity.QuizProgressEntity
import com.example.kotlinquizzes.feature.quiz.data.local.db.entity.TagPerformanceEntity
import com.example.kotlinquizzes.feature.quiz.data.local.db.entity.UserStatsEntity
import com.example.kotlinquizzes.feature.quiz.data.mapper.QuizMappers.toDomain
import com.example.kotlinquizzes.feature.quiz.domain.model.InsightsSnapshot
import com.example.kotlinquizzes.feature.quiz.domain.model.Quiz
import com.example.kotlinquizzes.feature.quiz.domain.repository.QuizRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor(
    private val quizDao: QuizDao,
    private val quizProgressDao: QuizProgressDao,
    private val tagPerformanceDao: TagPerformanceDao,
    private val userStatsDao: UserStatsDao,
    private val json: Json,
) : QuizRepository {

    private val stringListSerializer = ListSerializer(String.serializer())

    override fun observeAvailableQuizzes(): Flow<List<Quiz>> =
        quizDao.observeAvailableQuizzes().map { quizEntities ->
            quizEntities.map { entity ->
                val questions = quizDao.getQuestionsForQuiz(entity.quizId)
                entity.toDomain(questions)
            }
        }

    override suspend fun getAvailableQuizzes(): List<Quiz> =
        quizDao.getAvailableQuizzes().map { entity ->
            val questions = quizDao.getQuestionsForQuiz(entity.quizId)
            entity.toDomain(questions)
        }

    override suspend fun getQuizById(quizId: String): Quiz? {
        val entity = quizDao.getQuizById(quizId) ?: return null
        val questions = quizDao.getQuestionsForQuiz(quizId)
        return entity.toDomain(questions)
    }

    override suspend fun saveQuizProgress(quizId: String, questionIndex: Int) {
        Log.d(TAG, "Progress saved for Quiz $quizId at index $questionIndex")
        quizProgressDao.upsert(QuizProgressEntity(quizId = quizId, currentQuestionIndex = questionIndex))
    }

    override suspend fun getQuizProgress(quizId: String): Int =
        quizProgressDao.getProgress(quizId) ?: 0

    override suspend fun clearQuizProgress(quizId: String) {
        Log.d(TAG, "Progress cleared for Quiz $quizId")
        quizProgressDao.clear(quizId)
    }

    override suspend fun recordAnswer(tags: List<String>, isCorrect: Boolean) {
        // Update global stats
        ensureUserStatsExist()
        if (isCorrect) {
            userStatsDao.incrementCorrect()
        } else {
            userStatsDao.incrementIncorrect()
        }

        // Update per-tag performance
        tags.forEach { tag ->
            val existing = tagPerformanceDao.getByTag(tag)
            if (existing != null) {
                tagPerformanceDao.upsert(
                    existing.copy(
                        attempts = existing.attempts + 1,
                        mistakes = if (!isCorrect) existing.mistakes + 1 else existing.mistakes,
                    )
                )
            } else {
                tagPerformanceDao.upsert(
                    TagPerformanceEntity(
                        tag = tag,
                        attempts = 1,
                        mistakes = if (!isCorrect) 1 else 0,
                    )
                )
            }
        }
    }

    override suspend fun markQuizCompleted(quizId: String) {
        Log.d(TAG, "Quiz marked as completed: $quizId")
        quizDao.markCompleted(quizId)
        ensureUserStatsExist()
        userStatsDao.incrementCompletedQuizCount()
    }

    override suspend fun unlockNextQuiz() {
        val locked = quizDao.getLockedQuizzes()
        if (locked.isEmpty()) {
            Log.d(TAG, "No locked quizzes to unlock")
            return
        }

        val weakTags = tagPerformanceDao.getWeakestTags(5).map { it.tag }.toSet()

        if (weakTags.isEmpty()) {
            // No performance data yet — unlock the next in order
            val next = locked.first()
            Log.d(TAG, "No weak tags, unlocking next in order: ${next.quizId}")
            quizDao.makeAvailable(next.quizId)
            return
        }

        // Score each locked quiz by how many of its questions match weak tags
        val scored = locked.map { quiz ->
            val tagsJsonList = quizDao.getTagsJsonForQuiz(quiz.quizId)
            val quizTags = tagsJsonList.flatMap { tagsJson ->
                runCatching { json.decodeFromString(stringListSerializer, tagsJson) }
                    .getOrDefault(emptyList())
            }.toSet()
            val overlap = quizTags.intersect(weakTags).size
            quiz to overlap
        }

        val best = scored.maxByOrNull { it.second }?.first ?: locked.first()
        Log.d(TAG, "Unlocking quiz based on weak tags: ${best.quizId}")
        quizDao.makeAvailable(best.quizId)
    }

    override suspend fun getInsightsSnapshot(): InsightsSnapshot {
        val stats = userStatsDao.get() ?: UserStatsEntity()
        val allTags = tagPerformanceDao.getAll()
        return InsightsSnapshot(
            totalCorrect = stats.totalCorrect,
            totalIncorrect = stats.totalIncorrect,
            completedQuizCount = stats.completedQuizCount,
            tagAttempts = allTags.associate { it.tag to it.attempts },
            tagMistakes = allTags.associate { it.tag to it.mistakes },
        )
    }

    private suspend fun ensureUserStatsExist() {
        if (userStatsDao.get() == null) {
            userStatsDao.upsert(UserStatsEntity())
        }
    }
}
