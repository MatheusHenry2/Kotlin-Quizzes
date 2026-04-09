package com.example.kotlinquizzes.quiz

import androidx.lifecycle.SavedStateHandle
import com.example.kotlinquizzes.R
import com.example.kotlinquizzes.core.navigation.NavigationConstants
import com.example.kotlinquizzes.core.ui.event.UiEventManager
import com.example.kotlinquizzes.feature.quiz.domain.model.Question
import com.example.kotlinquizzes.feature.quiz.domain.model.Quiz
import com.example.kotlinquizzes.feature.quiz.domain.repository.QuizRepository
import com.example.kotlinquizzes.feature.quiz.domain.usecase.FinishQuizUseCase
import com.example.kotlinquizzes.feature.quiz.presentation.quiz.QuizContract
import com.example.kotlinquizzes.feature.quiz.presentation.quiz.QuizViewModel
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class QuizViewModelTest {

    @Mock
    private lateinit var quizRepository: QuizRepository

    @Mock
    private lateinit var finishQuizUseCase: FinishQuizUseCase

    @Mock
    private lateinit var uiEventManager: UiEventManager

    private lateinit var viewModel: QuizViewModel
    private lateinit var closeable: AutoCloseable
    private val testDispatcher = StandardTestDispatcher()

    private val quizId = "quiz_1"

    private val twoQuestionQuiz = Quiz(
        id = quizId,
        title = "Sample Quiz",
        questions = listOf(
            Question(
                id = "q1",
                text = "Question 1",
                options = listOf("A", "B"),
                correctIndex = 0,
                tags = listOf("kotlin"),
            ),
            Question(
                id = "q2",
                text = "Question 2",
                options = listOf("X", "Y"),
                correctIndex = 1,
                tags = listOf("android"),
            ),
        ),
    )

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

    private fun createViewModel(): QuizViewModel {
        val savedStateHandle =
            SavedStateHandle(mapOf(NavigationConstants.Args.QUIZ_ID to quizId))
        return QuizViewModel(savedStateHandle, quizRepository, finishQuizUseCase, uiEventManager)
    }

    @Test
    fun testLoadQuiz_WhenSuccess_PopulatesState() = runTest {
        whenever(quizRepository.getQuizById(quizId)).thenReturn(twoQuestionQuiz)
        whenever(quizRepository.getQuizProgress(quizId)).thenReturn(0)

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Sample Quiz", state.quizTitle)
        assertEquals(2, state.totalQuestions)
        assertEquals(0, state.currentQuestionIndex)
        assertNull(state.errorMessageResId)
    }

    @Test
    fun testLoadQuiz_WhenSavedProgressExists_RestoresIndex() = runTest {
        whenever(quizRepository.getQuizById(quizId)).thenReturn(twoQuestionQuiz)
        whenever(quizRepository.getQuizProgress(quizId)).thenReturn(1)

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.currentQuestionIndex)
    }

    @Test
    fun testLoadQuiz_WhenQuizNotFound_SetsErrorState() = runTest {
        whenever(quizRepository.getQuizById(quizId)).thenReturn(null)

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(R.string.error_quiz_not_found, state.errorMessageResId)
    }

    @Test
    fun testLoadQuiz_WhenThrows_SetsErrorAndShowsSnackbar() = runTest {
        whenever(quizRepository.getQuizById(quizId)).thenThrow(RuntimeException("boom"))

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(R.string.error_failed_load_quiz, state.errorMessageResId)
        verify(uiEventManager).showError(R.string.snackbar_error_generic)
    }

    @Test
    fun testOptionSelected_WhenNotChecking_UpdatesStateAndEmitsHaptic() = runTest {
        whenever(quizRepository.getQuizById(quizId)).thenReturn(twoQuestionQuiz)
        whenever(quizRepository.getQuizProgress(quizId)).thenReturn(0)

        viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<QuizContract.QuizEffect>()
        val job = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(QuizContract.QuizAction.OptionSelected(1))
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.selectedOptionIndex)
        assertTrue(effects.contains(QuizContract.QuizEffect.HapticFeedback))
        job.cancel()
    }

    @Test
    fun testOptionSelected_WhenCheckingAnswer_IsIgnored() = runTest {
        whenever(quizRepository.getQuizById(quizId)).thenReturn(twoQuestionQuiz)
        whenever(quizRepository.getQuizProgress(quizId)).thenReturn(0)

        viewModel = createViewModel()
        advanceUntilIdle()

        // Select correct option (index 0) and trigger Next so VM enters checking state.
        viewModel.onAction(QuizContract.QuizAction.OptionSelected(0))
        viewModel.onAction(QuizContract.QuizAction.NextClicked)
        // Don't advance through the full delay — VM is now in isCheckingAnswer.
        // Try to select another option while checking.
        viewModel.onAction(QuizContract.QuizAction.OptionSelected(1))

        // Selection should not have been overwritten while checking.
        assertEquals(0, viewModel.state.value.selectedOptionIndex)
    }

    @Test
    fun testNextClicked_WhenCorrectAnswer_RecordsAnswerAndAdvances() = runTest {
        whenever(quizRepository.getQuizById(quizId)).thenReturn(twoQuestionQuiz)
        whenever(quizRepository.getQuizProgress(quizId)).thenReturn(0)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(QuizContract.QuizAction.OptionSelected(0)) // correct
        viewModel.onAction(QuizContract.QuizAction.NextClicked)
        advanceUntilIdle()

        verify(quizRepository).recordAnswer(listOf("kotlin"), true)
        verify(quizRepository).saveQuizProgress(quizId, 1)
        val state = viewModel.state.value
        assertEquals(1, state.currentQuestionIndex)
        assertEquals(1, state.correctAnswers)
        assertNull(state.selectedOptionIndex)
    }

    @Test
    fun testNextClicked_WhenIncorrectAnswer_RecordsAndAdvancesWithoutIncrementingScore() = runTest {
        whenever(quizRepository.getQuizById(quizId)).thenReturn(twoQuestionQuiz)
        whenever(quizRepository.getQuizProgress(quizId)).thenReturn(0)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(QuizContract.QuizAction.OptionSelected(1)) // incorrect (correct=0)
        viewModel.onAction(QuizContract.QuizAction.NextClicked)
        advanceUntilIdle()

        verify(quizRepository).recordAnswer(listOf("kotlin"), false)
        assertEquals(1, viewModel.state.value.currentQuestionIndex)
        assertEquals(0, viewModel.state.value.correctAnswers)
    }

    @Test
    fun testNextClicked_OnLastQuestion_CallsFinishQuizAndEmitsQuizFinished() = runTest {
        whenever(quizRepository.getQuizById(quizId)).thenReturn(twoQuestionQuiz)
        whenever(quizRepository.getQuizProgress(quizId)).thenReturn(1) // start on last question

        viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<QuizContract.QuizEffect>()
        val job = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(QuizContract.QuizAction.OptionSelected(1)) // correct on q2
        viewModel.onAction(QuizContract.QuizAction.NextClicked)
        advanceUntilIdle()

        verify(finishQuizUseCase).invoke(quizId)
        val finished = effects.filterIsInstance<QuizContract.QuizEffect.QuizFinished>().firstOrNull()
        assertEquals(2, finished?.totalQuestions)
        assertEquals(1, finished?.correctAnswers)
        job.cancel()
    }

    @Test
    fun testNextClicked_WithoutSelectedOption_DoesNothing() = runTest {
        whenever(quizRepository.getQuizById(quizId)).thenReturn(twoQuestionQuiz)
        whenever(quizRepository.getQuizProgress(quizId)).thenReturn(0)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(QuizContract.QuizAction.NextClicked)
        advanceUntilIdle()

        verify(quizRepository, never()).recordAnswer(any(), any())
        verify(quizRepository, never()).saveQuizProgress(eq(quizId), any())
        assertEquals(0, viewModel.state.value.currentQuestionIndex)
    }

    @Test
    fun testCloseClicked_EmitsNavigateBackEffect() = runTest {
        whenever(quizRepository.getQuizById(quizId)).thenReturn(twoQuestionQuiz)
        whenever(quizRepository.getQuizProgress(quizId)).thenReturn(0)

        viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<QuizContract.QuizEffect>()
        val job = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(QuizContract.QuizAction.CloseClicked)
        advanceUntilIdle()

        assertTrue(effects.contains(QuizContract.QuizEffect.NavigateBack))
        job.cancel()
    }
}
