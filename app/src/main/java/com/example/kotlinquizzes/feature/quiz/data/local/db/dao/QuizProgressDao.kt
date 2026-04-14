package com.example.kotlinquizzes.feature.quiz.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.kotlinquizzes.feature.quiz.data.local.db.entity.QuizProgressEntity

@Dao
interface QuizProgressDao {

    @Query("SELECT currentQuestionIndex FROM quiz_progress WHERE quizId = :quizId")
    suspend fun getProgress(quizId: String): Int?

    @Upsert
    suspend fun upsert(entity: QuizProgressEntity)

    @Query("DELETE FROM quiz_progress WHERE quizId = :quizId")
    suspend fun clear(quizId: String)
}
