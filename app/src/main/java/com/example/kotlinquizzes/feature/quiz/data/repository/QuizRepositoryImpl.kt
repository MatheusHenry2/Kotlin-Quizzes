package com.example.kotlinquizzes.feature.quiz.data.repository

import android.util.Log
import com.example.kotlinquizzes.core.utils.Constants.TAG
import com.example.kotlinquizzes.feature.quiz.data.local.AssetsQuizDataSource
import com.example.kotlinquizzes.feature.quiz.data.local.QuizProgressDataStore
import com.example.kotlinquizzes.feature.quiz.data.local.QuizUserStateDataStore
import com.example.kotlinquizzes.feature.quiz.data.mapper.QuizMappers.toDomain
import com.example.kotlinquizzes.feature.quiz.data.model.QuizDto
import com.example.kotlinquizzes.feature.quiz.data.model.QuizzesPayloadDto
import com.example.kotlinquizzes.feature.quiz.domain.model.InsightsSnapshot
import com.example.kotlinquizzes.feature.quiz.domain.model.Quiz
import com.example.kotlinquizzes.feature.quiz.domain.repository.QuizRepository
import com.example.kotlinquizzes.feature.quiz.domain.usecase.GenerateAdaptiveQuizzesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor(
    private val dataSource: AssetsQuizDataSource,
    private val json: Json,
    private val progressDataStore: QuizProgressDataStore,
    private val userState: QuizUserStateDataStore,
    private val generateAdaptiveQuizzesUseCase: GenerateAdaptiveQuizzesUseCase,
) : QuizRepository {

    private companion object {
        const val INITIAL_ASSESSMENT_ID = "kotlin_android_assessment"
    }

    private fun loadStaticQuizDtos(): List<QuizDto> {
        val raw = dataSource.readQuizzesJson()
        return json.decodeFromString(QuizzesPayloadDto.serializer(), raw).quizzes
    }

    override fun observeQuizzes(): Flow<List<Quiz>> = combine(
        userState.initialAssessmentCompleted,
        userState.completedQuizIds,
        userState.generatedQuizzes,
    ) { assessmentDone, completedIds, generated ->
        val staticQuizzes = loadStaticQuizDtos()

        val visibleStatic = staticQuizzes.filter { quiz ->
            // Hide the initial assessment after completion.
            if (quiz.id == INITIAL_ASSESSMENT_ID) !assessmentDone else true
        }

        // Generated quizzes that haven't been completed yet.
        val visibleGenerated = generated.filter { it.id !in completedIds }

        (visibleStatic + visibleGenerated).map { it.toDomain() }
    }

    override fun observeInitialAssessmentCompleted(): Flow<Boolean> =
        userState.initialAssessmentCompleted

    override suspend fun isInitialAssessmentCompleted(): Boolean =
        userState.isInitialAssessmentCompleted()

    override suspend fun getQuizzes(): List<Quiz> {
        val staticQuizzes = loadStaticQuizDtos()
        val generated = userState.generatedQuizzes.first()
        val completed = userState.completedQuizIds.first()
        val assessmentDone = userState.isInitialAssessmentCompleted()

        val visibleStatic = staticQuizzes.filter {
            if (it.id == INITIAL_ASSESSMENT_ID) !assessmentDone else true
        }
        val visibleGenerated = generated.filter { it.id !in completed }
        return (visibleStatic + visibleGenerated).map { it.toDomain() }
    }

    override suspend fun getQuizById(quizId: String): Quiz? {
        // Look in static quizzes first.
        loadStaticQuizDtos().firstOrNull { it.id == quizId }?.let { return it.toDomain() }
        // Then in generated.
        val generated = userState.generatedQuizzes.first()
        return generated.firstOrNull { it.id == quizId }?.toDomain()
    }

    override suspend fun saveQuizProgress(quizId: String, questionIndex: Int) {
        Log.d(TAG, "Progress saved for Quiz $quizId at index $questionIndex")
        progressDataStore.saveProgress(quizId, questionIndex)
    }

    override suspend fun getQuizProgress(quizId: String): Int =
        progressDataStore.getProgress(quizId)

    override suspend fun clearQuizProgress(quizId: String) {
        Log.d(TAG, "Progress cleared for Quiz $quizId")
        progressDataStore.clearProgress(quizId)
    }

    override suspend fun recordAnswer(tags: List<String>, isCorrect: Boolean) {
        userState.recordAnswerTags(tags, isCorrect)
    }

    override suspend fun markQuizCompleted(quizId: String) {
        Log.d(TAG, "Quiz marked as completed: $quizId")
        userState.markQuizCompleted(quizId)
    }

    override suspend fun markInitialAssessmentCompleted() {
        Log.d(TAG, "Initial assessment marked as completed")
        userState.setInitialAssessmentCompleted(true)
    }

    override suspend fun generateAdaptiveQuizzes() {
        Log.d(TAG, "Triggering adaptive quiz generation")
        generateAdaptiveQuizzesUseCase()
    }

    override suspend fun getInsightsSnapshot(): InsightsSnapshot =
        userState.getInsightsSnapshot()
}
