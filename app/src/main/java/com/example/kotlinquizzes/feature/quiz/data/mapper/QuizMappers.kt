package com.example.kotlinquizzes.feature.quiz.data.mapper

import com.example.kotlinquizzes.feature.quiz.data.local.db.entity.QuestionEntity
import com.example.kotlinquizzes.feature.quiz.data.local.db.entity.QuizEntity
import com.example.kotlinquizzes.feature.quiz.data.model.QuestionDto
import com.example.kotlinquizzes.feature.quiz.data.model.QuizDto
import com.example.kotlinquizzes.feature.quiz.domain.model.Question
import com.example.kotlinquizzes.feature.quiz.domain.model.Quiz
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

object QuizMappers {

    private val json = Json { ignoreUnknownKeys = true }
    private val stringListSerializer = ListSerializer(String.serializer())

    fun QuizDto.toDomain(): Quiz = Quiz(
        id = id,
        title = title,
        questions = questions.map { it.toDomain() }
    )

    private fun QuestionDto.toDomain(): Question = Question(
        id = id,
        text = text,
        options = options,
        correctIndex = correctIndex,
        tags = tags
    )

    fun QuizEntity.toDomain(questions: List<QuestionEntity>): Quiz = Quiz(
        id = quizId,
        title = title,
        questions = questions.map { it.toDomain() }
    )

    fun QuestionEntity.toDomain(): Question = Question(
        id = questionId,
        text = text,
        options = json.decodeFromString(stringListSerializer, optionsJson),
        correctIndex = correctIndex,
        tags = json.decodeFromString(stringListSerializer, tagsJson),
    )
}
