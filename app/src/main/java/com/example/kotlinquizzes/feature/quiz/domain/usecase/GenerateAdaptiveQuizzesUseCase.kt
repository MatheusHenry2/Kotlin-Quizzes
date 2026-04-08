package com.example.kotlinquizzes.feature.quiz.domain.usecase

import android.util.Log
import com.example.kotlinquizzes.core.utils.Constants.TAG
import com.example.kotlinquizzes.feature.quiz.data.local.AssetsQuizDataSource
import com.example.kotlinquizzes.feature.quiz.data.local.QuizUserStateDataStore
import com.example.kotlinquizzes.feature.quiz.data.model.QuestionDto
import com.example.kotlinquizzes.feature.quiz.data.model.QuizDto
import com.example.kotlinquizzes.feature.quiz.data.model.QuizzesPayloadDto
import com.example.kotlinquizzes.feature.quiz.data.remote.ClaudeQuizApiClient
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates adaptive quizzes focused on the user's weakest tags.
 * Uses Claude API when configured, otherwise falls back to a deterministic
 * local generator that reshuffles the existing question bank, prioritising
 * weak tags so the feature still works without an API key.
 */
@Singleton
class GenerateAdaptiveQuizzesUseCase @Inject constructor(
    private val userState: QuizUserStateDataStore,
    private val claudeClient: ClaudeQuizApiClient,
    private val assetsDataSource: AssetsQuizDataSource,
    private val json: Json,
) {

    private companion object {
        const val DEFAULT_QUIZ_COUNT = 2
        const val DEFAULT_QUESTIONS_PER_QUIZ = 10
        const val WEAK_TAGS_LIMIT = 5
        const val GENERATED_PREFIX = "generated"
    }

    suspend operator fun invoke(
        quizCount: Int = DEFAULT_QUIZ_COUNT,
        questionsPerQuiz: Int = DEFAULT_QUESTIONS_PER_QUIZ,
    ) {
        val weakTags = userState.getWeakestTags(WEAK_TAGS_LIMIT)
        val quizzes = try {
            if (claudeClient.isConfigured) {
                Log.d(TAG, "GenerateAdaptiveQuizzes: using Claude API. weakTags=$weakTags")
                claudeClient.generateQuizzes(weakTags, quizCount, questionsPerQuiz)
            } else {
                Log.d(TAG, "GenerateAdaptiveQuizzes: API key missing, using local fallback. weakTags=$weakTags")
                buildLocalQuizzes(weakTags, quizCount, questionsPerQuiz)
            }
        } catch (e: Exception) {
            Log.e(TAG, "GenerateAdaptiveQuizzes: remote generation failed, using local fallback", e)
            buildLocalQuizzes(weakTags, quizCount, questionsPerQuiz)
        }

        val withUniqueIds = quizzes.mapIndexed { index, quiz ->
            quiz.copy(
                id = "${GENERATED_PREFIX}_${System.currentTimeMillis()}_$index",
            )
        }
        userState.addGeneratedQuizzes(withUniqueIds)
    }

    private fun buildLocalQuizzes(
        weakTags: List<String>,
        quizCount: Int,
        questionsPerQuiz: Int,
    ): List<QuizDto> {
        val raw = assetsDataSource.readQuizzesJson()
        val payload = json.decodeFromString(QuizzesPayloadDto.serializer(), raw)
        val pool: List<QuestionDto> = payload.quizzes.flatMap { it.questions }
        if (pool.isEmpty()) return emptyList()

        val weakSet = weakTags.toSet()
        val prioritized = pool.sortedByDescending { question ->
            question.tags.count { it in weakSet }
        }

        return List(quizCount) { quizIndex ->
            val timestamp = System.currentTimeMillis()
            val rotated = prioritized.drop(quizIndex * questionsPerQuiz) + prioritized
            val questions = rotated.take(questionsPerQuiz).mapIndexed { qIndex, q ->
                q.copy(id = "${GENERATED_PREFIX}_${timestamp}_${quizIndex}_$qIndex")
            }
            val titleTag = weakTags.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Practice"
            QuizDto(
                id = "${GENERATED_PREFIX}_${timestamp}_$quizIndex",
                title = "$titleTag Practice ${quizIndex + 1}",
                questions = questions,
            )
        }
    }
}
