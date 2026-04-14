package com.example.kotlinquizzes.usecase

import com.example.kotlinquizzes.feature.quiz.domain.repository.QuizRepository
import com.example.kotlinquizzes.feature.quiz.domain.usecase.FinishQuizUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
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
class FinishQuizUseCaseTest {

    @Mock
    private lateinit var quizRepository: QuizRepository

    private lateinit var finishQuizUseCase: FinishQuizUseCase
    private lateinit var closeable: AutoCloseable
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        closeable = MockitoAnnotations.openMocks(this)
        finishQuizUseCase = FinishQuizUseCase(quizRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        closeable.close()
    }

    @Test
    fun testInvoke_ClearsProgressMarksCompletedAndUnlocks() = runTest {
        val quizId = "quiz_42"

        finishQuizUseCase(quizId)

        verify(quizRepository).clearQuizProgress(quizId)
        verify(quizRepository).markQuizCompleted(quizId)
        verify(quizRepository).unlockNextQuiz()
    }

    @Test
    fun testInvoke_WhenClearProgressThrows_IsSwallowed() = runTest {
        val quizId = "quiz_42"
        whenever(quizRepository.clearQuizProgress(quizId)).thenThrow(RuntimeException("boom"))

        finishQuizUseCase(quizId)

        verify(quizRepository).clearQuizProgress(quizId)
        // markQuizCompleted is in the same try block, so skipped after throw.
        verify(quizRepository, never()).markQuizCompleted(quizId)
        // unlockNextQuiz should still be attempted in its own try block.
        verify(quizRepository).unlockNextQuiz()
    }

    @Test
    fun testInvoke_WhenUnlockThrows_IsSwallowed() = runTest {
        val quizId = "quiz_42"
        whenever(quizRepository.unlockNextQuiz()).thenThrow(RuntimeException("boom"))

        finishQuizUseCase(quizId)

        verify(quizRepository).clearQuizProgress(quizId)
        verify(quizRepository).markQuizCompleted(quizId)
        verify(quizRepository).unlockNextQuiz()
    }
}
