package com.example.kotlinquizzes.feature.quiz.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.quizProgressDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "quiz_progress"
)

class QuizProgressDataStore @Inject constructor(private val context: Context) {

    suspend fun saveProgress(quizId: String, questionIndex: Int) {
        val key = intPreferencesKey(quizId)
        context.quizProgressDataStore.edit { prefs ->
            prefs[key] = questionIndex
        }
    }

    suspend fun getProgress(quizId: String): Int {
        val key = intPreferencesKey(quizId)
        return context.quizProgressDataStore.data
            .map { prefs -> prefs[key] ?: 0 }
            .first()
    }

    suspend fun clearProgress(quizId: String) {
        val key = intPreferencesKey(quizId)
        context.quizProgressDataStore.edit { prefs ->
            prefs.remove(key)
        }
    }
}
