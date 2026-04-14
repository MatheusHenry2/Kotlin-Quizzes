package com.example.kotlinquizzes.feature.quiz.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_progress")
data class QuizProgressEntity(
    @PrimaryKey val quizId: String,
    val currentQuestionIndex: Int = 0,
)
