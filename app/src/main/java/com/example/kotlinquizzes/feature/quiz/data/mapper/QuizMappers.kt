package com.example.kotlinquizzes.feature.quiz.data.mapper

import com.example.kotlinquizzes.feature.quiz.data.model.QuestionDto
import com.example.kotlinquizzes.feature.quiz.data.model.QuizDto
import com.example.kotlinquizzes.feature.quiz.domain.model.Question
import com.example.kotlinquizzes.feature.quiz.domain.model.Quiz

object QuizMappers {
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
}
