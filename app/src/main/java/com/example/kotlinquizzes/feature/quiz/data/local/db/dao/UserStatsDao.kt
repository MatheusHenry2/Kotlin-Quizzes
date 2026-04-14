package com.example.kotlinquizzes.feature.quiz.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.kotlinquizzes.feature.quiz.data.local.db.entity.UserStatsEntity

@Dao
interface UserStatsDao {

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun get(): UserStatsEntity?

    @Upsert
    suspend fun upsert(entity: UserStatsEntity)

    @Query("UPDATE user_stats SET totalCorrect = totalCorrect + 1 WHERE id = 1")
    suspend fun incrementCorrect()

    @Query("UPDATE user_stats SET totalIncorrect = totalIncorrect + 1 WHERE id = 1")
    suspend fun incrementIncorrect()

    @Query("UPDATE user_stats SET completedQuizCount = completedQuizCount + 1 WHERE id = 1")
    suspend fun incrementCompletedQuizCount()
}
