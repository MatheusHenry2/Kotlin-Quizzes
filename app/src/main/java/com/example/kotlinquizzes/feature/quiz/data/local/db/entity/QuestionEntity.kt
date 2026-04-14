package com.example.kotlinquizzes.feature.quiz.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = QuizEntity::class,
            parentColumns = ["quizId"],
            childColumns = ["quizId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("quizId")],
)
data class QuestionEntity(
    @PrimaryKey val questionId: String,
    val quizId: String,
    val text: String,
    val optionsJson: String,
    val correctIndex: Int,
    val tagsJson: String,
    val sortOrder: Int = 0,
)
