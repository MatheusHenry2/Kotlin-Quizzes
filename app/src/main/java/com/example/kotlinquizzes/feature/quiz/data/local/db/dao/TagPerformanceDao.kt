package com.example.kotlinquizzes.feature.quiz.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.kotlinquizzes.feature.quiz.data.local.db.entity.TagPerformanceEntity

@Dao
interface TagPerformanceDao {

    @Query("SELECT * FROM tag_performance")
    suspend fun getAll(): List<TagPerformanceEntity>

    @Query("SELECT * FROM tag_performance WHERE tag = :tag")
    suspend fun getByTag(tag: String): TagPerformanceEntity?

    @Upsert
    suspend fun upsert(entity: TagPerformanceEntity)

    @Query("SELECT * FROM tag_performance WHERE mistakes > 0 ORDER BY CAST(mistakes AS REAL) / CAST(attempts AS REAL) DESC LIMIT :limit")
    suspend fun getWeakestTags(limit: Int): List<TagPerformanceEntity>
}
