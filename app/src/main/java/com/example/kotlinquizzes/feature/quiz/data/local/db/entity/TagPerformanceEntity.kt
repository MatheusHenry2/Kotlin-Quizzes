package com.example.kotlinquizzes.feature.quiz.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tag_performance")
data class TagPerformanceEntity(
    @PrimaryKey val tag: String,
    val attempts: Int = 0,
    val mistakes: Int = 0,
)
