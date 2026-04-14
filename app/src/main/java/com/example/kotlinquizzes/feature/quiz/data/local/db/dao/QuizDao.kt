package com.example.kotlinquizzes.feature.quiz.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.kotlinquizzes.feature.quiz.data.local.db.entity.QuestionEntity
import com.example.kotlinquizzes.feature.quiz.data.local.db.entity.QuizEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuizzes(quizzes: List<QuizEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("SELECT COUNT(*) FROM quizzes")
    suspend fun quizCount(): Int

    @Query("SELECT * FROM quizzes WHERE isAvailable = 1 AND isCompleted = 0 ORDER BY sortOrder")
    fun observeAvailableQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE isAvailable = 1 AND isCompleted = 0 ORDER BY sortOrder")
    suspend fun getAvailableQuizzes(): List<QuizEntity>

    @Query("SELECT * FROM quizzes WHERE quizId = :quizId")
    suspend fun getQuizById(quizId: String): QuizEntity?

    @Query("SELECT * FROM questions WHERE quizId = :quizId ORDER BY sortOrder")
    suspend fun getQuestionsForQuiz(quizId: String): List<QuestionEntity>

    @Query("UPDATE quizzes SET isCompleted = 1 WHERE quizId = :quizId")
    suspend fun markCompleted(quizId: String)

    @Query("UPDATE quizzes SET isAvailable = 1 WHERE quizId = :quizId")
    suspend fun makeAvailable(quizId: String)

    @Query("SELECT COUNT(*) FROM quizzes WHERE isCompleted = 1")
    suspend fun completedCount(): Int

    @Query("SELECT * FROM quizzes WHERE isAvailable = 0 AND isCompleted = 0 ORDER BY sortOrder")
    suspend fun getLockedQuizzes(): List<QuizEntity>

    @Query("SELECT DISTINCT tagsJson FROM questions WHERE quizId = :quizId")
    suspend fun getTagsJsonForQuiz(quizId: String): List<String>
}
