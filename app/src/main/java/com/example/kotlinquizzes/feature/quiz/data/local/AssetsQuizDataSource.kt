package com.example.kotlinquizzes.feature.quiz.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AssetsQuizDataSource @Inject constructor(@ApplicationContext private val context: Context) {

    private companion object {
        const val QUIZZES_FILE_NAME = "quizzes.json"
    }

    fun readQuizzesJson(): String {
        return context.assets.open(QUIZZES_FILE_NAME).bufferedReader().use { it.readText() }
    }
}
