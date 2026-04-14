package com.example.kotlinquizzes.feature.quiz.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey val quizId: String,
    val title: String,
    val isAvailable: Boolean = false,
    val isCompleted: Boolean = false,
    val sortOrder: Int = 0,
)
