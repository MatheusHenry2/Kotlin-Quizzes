package com.example.kotlinquizzes.quizlist

import com.example.kotlinquizzes.R
import com.example.kotlinquizzes.core.ui.event.UiEventManager
import com.example.kotlinquizzes.feature.auth.domain.usecase.GetCurrentUserNameUseCase
import com.example.kotlinquizzes.feature.quiz.domain.model.Question
import com.example.kotlinquizzes.feature.quiz.domain.model.Quiz
import com.example.kotlinquizzes.feature.quiz.domain.repository.QuizRepository
import com.example.kotlinquizzes.feature.quiz.presentation.quizlist.QuizListContract
import com.example.kotlinquizzes.feature.quiz.presentation.quizlist.QuizListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class QuizListViewModelTest {

    @Mock
    private lateinit var quizRepository: QuizRepository

    @Mock
    private lateinit var getCurrentUserNameUseCase: GetCurrentUserNameUseCase

    @Mock
    private lateinit var uiEventManager: UiEventManager

    private lateinit var viewModel: QuizListViewModel
    private lateinit var closeable: AutoCloseable
    private val testDispatcher = StandardTestDispatcher()

    private val sampleQuiz = Quiz(
        id = "q1",
        title = "Sample Quiz",
        questions = listOf(
            Question(
                id = "q1_1",
                text = "Q?",
                options = listOf("A", "B"),
                correctIndex = 0,
                tags = listOf("kotlin"),
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

    private fun createViewModel(): QuizListViewModel = QuizListViewModel(
        quizRepository,
        getCurrentUserNameUseCase,
        uiEventManager,
    )

    @Test
    fun testObserveQuizzes_WhenSuccess_UpdatesStateWithQuizzesAndUserName() = runTest {
        whenever(getCurrentUserNameUseCase()).thenReturn("Matheus")
        whenever(quizRepository.observeAvailableQuizzes()).thenReturn(flowOf(listOf(sampleQuiz)))

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Matheus", state.userName)
        assertEquals(listOf(sampleQuiz), state.quizzes)
        assertNull(state.errorMessageResId)
    }

    @Test
    fun testObserveQuizzes_WhenFails_SetsErrorAndShowsSnackbar() = runTest {
        whenever(getCurrentUserNameUseCase()).thenReturn("Matheus")
        whenever(quizRepository.observeAvailableQuizzes()).thenReturn(
            flow { throw RuntimeException("boom") }
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(R.string.error_failed_load_quizzes, state.errorMessageResId)
        verify(uiEventManager).showError(R.string.snackbar_error_generic)
    }

    @Test
    fun testQuizClicked_EmitsNavigateToQuizEffect() = runTest {
        whenever(getCurrentUserNameUseCase()).thenReturn("Matheus")
        whenever(quizRepository.observeAvailableQuizzes()).thenReturn(flowOf(emptyList()))

        viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<QuizListContract.QuizListEffect>()
        val job = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(QuizListContract.QuizListAction.QuizClicked("quiz_42"))
        advanceUntilIdle()

        assertTrue(effects.any { it is QuizListContract.QuizListEffect.NavigateToQuiz && it.quizId == "quiz_42" })
        job.cancel()
    }

    @Test
    fun testRefreshPulled_WhenSuccess_ShowsSuccessSnackbar() = runTest {
        whenever(getCurrentUserNameUseCase()).thenReturn("Matheus")
        whenever(quizRepository.observeAvailableQuizzes()).thenReturn(flowOf(listOf(sampleQuiz)))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(QuizListContract.QuizListAction.RefreshPulled)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isRefreshing)
        verify(uiEventManager).showSuccess(R.string.snackbar_refresh_success)
    }

    @Test
    fun testRefreshPulled_WhenFails_ShowsErrorSnackbar() = runTest {
        whenever(getCurrentUserNameUseCase()).thenReturn("Matheus")
        whenever(quizRepository.observeAvailableQuizzes())
            .thenReturn(flowOf(listOf(sampleQuiz)))
            .thenReturn(flow { throw RuntimeException("refresh failed") })

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(QuizListContract.QuizListAction.RefreshPulled)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isRefreshing)
        verify(uiEventManager).showError(R.string.snackbar_refresh_failed)
    }

    @Test
    fun testRetryClicked_TriggersObserveQuizzesAgain() = runTest {
        whenever(getCurrentUserNameUseCase()).thenReturn("Matheus")
        whenever(quizRepository.observeAvailableQuizzes()).thenReturn(flowOf(listOf(sampleQuiz)))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(QuizListContract.QuizListAction.RetryClicked)
        advanceUntilIdle()

        verify(quizRepository, times(2)).observeAvailableQuizzes()
    }
}
