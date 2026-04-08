package com.example.kotlinquizzes.feature.quiz.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.kotlinquizzes.feature.quiz.data.model.QuizDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.quizUserStateDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "quiz_user_state"
)

data class InsightsSnapshot(
    val totalCorrect: Int,
    val totalIncorrect: Int,
    val completedQuizCount: Int,
    val tagAttempts: Map<String, Int>,
    val tagMistakes: Map<String, Int>,
)

@Singleton
class QuizUserStateDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) {

    private companion object {
        val KEY_INITIAL_ASSESSMENT_COMPLETED = booleanPreferencesKey("initial_assessment_completed")
        val KEY_COMPLETED_QUIZ_IDS = stringSetPreferencesKey("completed_quiz_ids")
        val KEY_TAG_MISTAKES = stringPreferencesKey("tag_mistakes_json")
        val KEY_TAG_ATTEMPTS = stringPreferencesKey("tag_attempts_json")
        val KEY_GENERATED_QUIZZES = stringPreferencesKey("generated_quizzes_json")
        val KEY_TOTAL_CORRECT = intPreferencesKey("total_correct_answers")
        val KEY_TOTAL_INCORRECT = intPreferencesKey("total_incorrect_answers")
    }

    private val tagMapSerializer = MapSerializer(String.serializer(), Int.serializer())
    private val quizListSerializer = ListSerializer(QuizDto.serializer())

    val initialAssessmentCompleted: Flow<Boolean> =
        context.quizUserStateDataStore.data.map { it[KEY_INITIAL_ASSESSMENT_COMPLETED] ?: false }

    val completedQuizIds: Flow<Set<String>> =
        context.quizUserStateDataStore.data.map { it[KEY_COMPLETED_QUIZ_IDS] ?: emptySet() }

    val generatedQuizzes: Flow<List<QuizDto>> =
        context.quizUserStateDataStore.data.map { prefs ->
            val raw = prefs[KEY_GENERATED_QUIZZES] ?: return@map emptyList()
            runCatching { json.decodeFromString(quizListSerializer, raw) }.getOrDefault(emptyList())
        }

    suspend fun isInitialAssessmentCompleted(): Boolean =
        initialAssessmentCompleted.first()

    suspend fun setInitialAssessmentCompleted(value: Boolean) {
        context.quizUserStateDataStore.edit { prefs ->
            prefs[KEY_INITIAL_ASSESSMENT_COMPLETED] = value
        }
    }

    suspend fun markQuizCompleted(quizId: String) {
        context.quizUserStateDataStore.edit { prefs ->
            val current = prefs[KEY_COMPLETED_QUIZ_IDS] ?: emptySet()
            prefs[KEY_COMPLETED_QUIZ_IDS] = current + quizId
        }
    }

    suspend fun recordAnswerTags(tags: List<String>, isCorrect: Boolean) {
        context.quizUserStateDataStore.edit { prefs ->
            // Always update overall counters, even when there are no tags.
            if (isCorrect) {
                prefs[KEY_TOTAL_CORRECT] = (prefs[KEY_TOTAL_CORRECT] ?: 0) + 1
            } else {
                prefs[KEY_TOTAL_INCORRECT] = (prefs[KEY_TOTAL_INCORRECT] ?: 0) + 1
            }
            if (tags.isEmpty()) return@edit
            val attempts = decodeMap(prefs[KEY_TAG_ATTEMPTS]).toMutableMap()
            val mistakes = decodeMap(prefs[KEY_TAG_MISTAKES]).toMutableMap()
            tags.forEach { tag ->
                attempts[tag] = (attempts[tag] ?: 0) + 1
                if (!isCorrect) {
                    mistakes[tag] = (mistakes[tag] ?: 0) + 1
                }
            }
            prefs[KEY_TAG_ATTEMPTS] = json.encodeToString(tagMapSerializer, attempts)
            prefs[KEY_TAG_MISTAKES] = json.encodeToString(tagMapSerializer, mistakes)
        }
    }

    suspend fun getInsightsSnapshot(): InsightsSnapshot {
        val prefs = context.quizUserStateDataStore.data.first()
        return InsightsSnapshot(
            totalCorrect = prefs[KEY_TOTAL_CORRECT] ?: 0,
            totalIncorrect = prefs[KEY_TOTAL_INCORRECT] ?: 0,
            completedQuizCount = (prefs[KEY_COMPLETED_QUIZ_IDS] ?: emptySet()).size,
            tagAttempts = decodeMap(prefs[KEY_TAG_ATTEMPTS]),
            tagMistakes = decodeMap(prefs[KEY_TAG_MISTAKES]),
        )
    }

    suspend fun getWeakestTags(limit: Int): List<String> {
        val prefs = context.quizUserStateDataStore.data.first()
        val mistakes = decodeMap(prefs[KEY_TAG_MISTAKES])
        if (mistakes.isEmpty()) return emptyList()
        return mistakes.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }
    }

    suspend fun addGeneratedQuizzes(quizzes: List<QuizDto>) {
        if (quizzes.isEmpty()) return
        context.quizUserStateDataStore.edit { prefs ->
            val current = prefs[KEY_GENERATED_QUIZZES]
                ?.let { runCatching { json.decodeFromString(quizListSerializer, it) }.getOrNull() }
                ?: emptyList()
            val merged = current + quizzes
            prefs[KEY_GENERATED_QUIZZES] = json.encodeToString(quizListSerializer, merged)
        }
    }

    private fun decodeMap(raw: String?): Map<String, Int> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching { json.decodeFromString(tagMapSerializer, raw) }.getOrDefault(emptyMap())
    }
}
