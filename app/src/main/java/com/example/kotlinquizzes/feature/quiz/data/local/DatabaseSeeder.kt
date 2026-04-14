package com.example.kotlinquizzes.feature.quiz.data.local

import android.util.Log
import com.example.kotlinquizzes.core.utils.Constants.TAG
import com.example.kotlinquizzes.feature.quiz.data.local.db.dao.QuizDao
import com.example.kotlinquizzes.feature.quiz.data.local.db.dao.UserStatsDao
import com.example.kotlinquizzes.feature.quiz.data.local.db.entity.QuestionEntity
import com.example.kotlinquizzes.feature.quiz.data.local.db.entity.QuizEntity
import com.example.kotlinquizzes.feature.quiz.data.local.db.entity.UserStatsEntity
import com.example.kotlinquizzes.feature.quiz.data.model.QuizzesPayloadDto
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val assetsDataSource: AssetsQuizDataSource,
    private val json: Json,
    private val quizDao: QuizDao,
    private val userStatsDao: UserStatsDao,
) {

    suspend fun seedIfEmpty() {
        if (quizDao.quizCount() > 0) {
            Log.d(TAG, "DatabaseSeeder: database already seeded, skipping")
            return
        }

        Log.d(TAG, "DatabaseSeeder: seeding database from assets")

        val raw = assetsDataSource.readQuizzesJson()
        val payload = json.decodeFromString(QuizzesPayloadDto.serializer(), raw)

        val quizEntities = payload.quizzes.mapIndexed { index, dto ->
            QuizEntity(
                quizId = dto.id,
                title = dto.title,
                isAvailable = index == 0,
                isCompleted = false,
                sortOrder = index,
            )
        }

        val questionEntities = payload.quizzes.flatMap { quiz ->
            quiz.questions.mapIndexed { qIndex, q ->
                QuestionEntity(
                    questionId = q.id,
                    quizId = quiz.id,
                    text = q.text,
                    optionsJson = json.encodeToString(
                        ListSerializer(String.serializer()),
                        q.options,
                    ),
                    correctIndex = q.correctIndex,
                    tagsJson = json.encodeToString(
                        ListSerializer(String.serializer()),
                        q.tags,
                    ),
                    sortOrder = qIndex,
                )
            }
        }

        quizDao.insertQuizzes(quizEntities)
        quizDao.insertQuestions(questionEntities)

        // Ensure user stats row exists
        if (userStatsDao.get() == null) {
            userStatsDao.upsert(UserStatsEntity())
        }

        Log.d(TAG, "DatabaseSeeder: seeded ${quizEntities.size} quizzes, ${questionEntities.size} questions")
    }
}
