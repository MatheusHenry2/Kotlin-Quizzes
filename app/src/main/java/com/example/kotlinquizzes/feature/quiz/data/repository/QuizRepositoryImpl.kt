package com.example.kotlinquizzes.feature.quiz.data.repository

import com.example.kotlinquizzes.feature.quiz.data.local.AssetsQuizDataSource
import com.example.kotlinquizzes.feature.quiz.data.mapper.QuizMappers.toDomain
import com.example.kotlinquizzes.feature.quiz.data.model.QuizzesPayloadDto
import com.example.kotlinquizzes.feature.quiz.domain.model.Quiz
import com.example.kotlinquizzes.feature.quiz.domain.repository.QuizRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor(
    private val dataSource: AssetsQuizDataSource,
    private val json: Json
) : QuizRepository {

    override suspend fun getQuizzes(): List<Quiz> {
        val raw = dataSource.readQuizzesJson()
        val payload = json.decodeFromString<QuizzesPayloadDto>(raw)
        return payload.quizzes.map { it.toDomain() }
    }

    override suspend fun getQuizById(quizId: String): Quiz? {
        return getQuizzes().firstOrNull { it.id == quizId }
    }
}
