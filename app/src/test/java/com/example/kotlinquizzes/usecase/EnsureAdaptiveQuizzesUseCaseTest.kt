package com.example.kotlinquizzes.usecase

import com.example.kotlinquizzes.feature.quiz.domain.repository.QuizRepository
import com.example.kotlinquizzes.feature.quiz.domain.usecase.EnsureAdaptiveQuizzesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class EnsureAdaptiveQuizzesUseCaseTest {

    @Mock
    private lateinit var quizRepository: QuizRepository

    private lateinit var ensureAdaptiveQuizzesUseCase: EnsureAdaptiveQuizzesUseCase
    private lateinit var closeable: AutoCloseable
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        closeable = MockitoAnnotations.openMocks(this)
        ensureAdaptiveQuizzesUseCase = EnsureAdaptiveQuizzesUseCase(quizRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        closeable.close()
    }

    @Test
    fun testInvoke_WhenCurrentQuizCountIsPositive_ReturnsFalseAndDoesNotGenerate() = runTest {
        val result = ensureAdaptiveQuizzesUseCase(currentQuizCount = 3)

        assertFalse(result)
        verify(quizRepository, never()).isInitialAssessmentCompleted()
        verify(quizRepository, never()).generateAdaptiveQuizzes()
    }

    @Test
    fun testInvoke_WhenAssessmentNotCompleted_ReturnsFalseAndDoesNotGenerate() = runTest {
        whenever(quizRepository.isInitialAssessmentCompleted()).thenReturn(false)

        val result = ensureAdaptiveQuizzesUseCase(currentQuizCount = 0)

        assertFalse(result)
        verify(quizRepository).isInitialAssessmentCompleted()
        verify(quizRepository, never()).generateAdaptiveQuizzes()
    }

    @Test
    fun testInvoke_WhenEmptyAndAssessmentDone_GeneratesAndReturnsTrue() = runTest {
        whenever(quizRepository.isInitialAssessmentCompleted()).thenReturn(true)

        val result = ensureAdaptiveQuizzesUseCase(currentQuizCount = 0)

        assertTrue(result)
        verify(quizRepository).generateAdaptiveQuizzes()
    }
}
