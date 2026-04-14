package com.example.kotlinquizzes.feature.quiz.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.kotlinquizzes.feature.quiz.data.local.db.dao.QuizDao
import com.example.kotlinquizzes.feature.quiz.data.local.db.dao.QuizProgressDao
import com.example.kotlinquizzes.feature.quiz.data.local.db.dao.TagPerformanceDao
import com.example.kotlinquizzes.feature.quiz.data.local.db.dao.UserStatsDao
import com.example.kotlinquizzes.feature.quiz.data.local.db.entity.QuestionEntity
import com.example.kotlinquizzes.feature.quiz.data.local.db.entity.QuizEntity
import com.example.kotlinquizzes.feature.quiz.data.local.db.entity.QuizProgressEntity
import com.example.kotlinquizzes.feature.quiz.data.local.db.entity.TagPerformanceEntity
import com.example.kotlinquizzes.feature.quiz.data.local.db.entity.UserStatsEntity

@Database(
    entities = [
        QuizEntity::class,
        QuestionEntity::class,
        TagPerformanceEntity::class,
        QuizProgressEntity::class,
        UserStatsEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao
    abstract fun tagPerformanceDao(): TagPerformanceDao
    abstract fun quizProgressDao(): QuizProgressDao
    abstract fun userStatsDao(): UserStatsDao
}
