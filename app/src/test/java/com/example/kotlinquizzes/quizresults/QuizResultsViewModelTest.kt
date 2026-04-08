package com.example.kotlinquizzes.quizresults

import androidx.lifecycle.SavedStateHandle
import com.example.kotlinquizzes.core.navigation.NavigationConstants
import com.example.kotlinquizzes.feature.quiz.presentation.quizresults.QuizResultsContract
import com.example.kotlinquizzes.feature.quiz.presentation.quizresults.QuizResultsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class QuizResultsViewModelTest {

    private lateinit var viewModel: QuizResultsViewModel
    private lateinit var closeable: AutoCloseable
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        closeable = MockitoAnnotations.openMocks(this)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        closeable.close()
    }

    private fun createViewModel(totalQuestions: Int, correctAnswers: Int): QuizResultsViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                NavigationConstants.Args.TOTAL_QUESTIONS to totalQuestions,
                NavigationConstants.Args.CORRECT_ANSWERS to correctAnswers,
            )
        )
        return QuizResultsViewModel(savedStateHandle)
    }

    @Test
    fun testInitialState_ReflectsValuesFromSavedStateHandle() {
        viewModel = createViewModel(totalQuestions = 10, correctAnswers = 7)

        val state = viewModel.state.value
        assertEquals(10, state.totalQuestions)
        assertEquals(7, state.correctAnswers)
    }

    @Test
    fun testBackToHomeClicked_EmitsNavigateToHomeEffect() = runTest {
        viewModel = createViewModel(totalQuestions = 5, correctAnswers = 3)

        val effects = mutableListOf<QuizResultsContract.QuizResultsEffect>()
        val job = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(QuizResultsContract.QuizResultsAction.BackToHomeClicked)
        advanceUntilIdle()

        assertTrue(effects.contains(QuizResultsContract.QuizResultsEffect.NavigateToHome))
        job.cancel()
    }
}
