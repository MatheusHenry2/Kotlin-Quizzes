package com.example.kotlinquizzes.feature.quiz.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1,
    val totalCorrect: Int = 0,
    val totalIncorrect: Int = 0,
    val completedQuizCount: Int = 0,
)
