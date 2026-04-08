package com.example.kotlinquizzes.insights

import com.example.kotlinquizzes.R
import com.example.kotlinquizzes.feature.quiz.domain.model.CategoryMastery
import com.example.kotlinquizzes.feature.quiz.domain.model.LearningInsights
import com.example.kotlinquizzes.feature.quiz.domain.model.WeakTopic
import com.example.kotlinquizzes.feature.quiz.domain.usecase.GetLearningInsightsUseCase
import com.example.kotlinquizzes.feature.quiz.presentation.insights.LearningInsightsContract
import com.example.kotlinquizzes.feature.quiz.presentation.insights.LearningInsightsViewModel
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
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class LearningInsightsViewModelTest {

    @Mock
    private lateinit var getLearningInsights: GetLearningInsightsUseCase

    private lateinit var viewModel: LearningInsightsViewModel
    private lateinit var closeable: AutoCloseable
    private val testDispatcher = StandardTestDispatcher()

    private val sampleInsights = LearningInsights(
        totalQuizzesCompleted = 4,
        totalCorrect = 12,
        totalIncorrect = 3,
        accuracyPercent = 80,
        masteryByCategory = listOf(CategoryMastery(tag = "kotlin", masteryPercent = 90)),
        topicsToImprove = listOf(WeakTopic(tag = "android", errorRatePercent = 60)),
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

    @Test
    fun testInitialState_BeforeAnyDispatch_IsLoading() = runTest {
        whenever(getLearningInsights()).thenReturn(sampleInsights)

        viewModel = LearningInsightsViewModel(getLearningInsights)

        // Before advancing the dispatcher the launched coroutine has not run.
        assertTrue(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.insights)
    }

    @Test
    fun testLoadInsights_WhenSuccess_PopulatesStateWithInsights() = runTest {
        whenever(getLearningInsights()).thenReturn(sampleInsights)

        viewModel = LearningInsightsViewModel(getLearningInsights)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(sampleInsights, state.insights)
        assertNull(state.errorMessageResId)
    }

    @Test
    fun testLoadInsights_WhenThrows_SetsErrorMessage() = runTest {
        whenever(getLearningInsights()).thenThrow(RuntimeException("boom"))

        viewModel = LearningInsightsViewModel(getLearningInsights)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.insights)
        assertEquals(R.string.error_failed_load_insights, state.errorMessageResId)
    }

    @Test
    fun testRetryClicked_InvokesUseCaseAgain() = runTest {
        whenever(getLearningInsights()).thenReturn(sampleInsights)

        viewModel = LearningInsightsViewModel(getLearningInsights)
        advanceUntilIdle()

        viewModel.onAction(LearningInsightsContract.LearningInsightsAction.RetryClicked)
        advanceUntilIdle()

        verify(getLearningInsights, times(2)).invoke()
    }

    @Test
    fun testRetryClicked_AfterError_RecoversToSuccess() = runTest {
        whenever(getLearningInsights())
            .thenThrow(RuntimeException("boom"))
            .thenReturn(sampleInsights)

        viewModel = LearningInsightsViewModel(getLearningInsights)
        advanceUntilIdle()
        assertEquals(R.string.error_failed_load_insights, viewModel.state.value.errorMessageResId)

        viewModel.onAction(LearningInsightsContract.LearningInsightsAction.RetryClicked)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.errorMessageResId)
        assertEquals(sampleInsights, state.insights)
    }

    @Test
    fun testBackToHomeClicked_EmitsNavigateToHomeEffect() = runTest {
        whenever(getLearningInsights()).thenReturn(sampleInsights)

        viewModel = LearningInsightsViewModel(getLearningInsights)
        advanceUntilIdle()

        val effects = mutableListOf<LearningInsightsContract.LearningInsightsEffect>()
        val job = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(LearningInsightsContract.LearningInsightsAction.BackToHomeClicked)
        advanceUntilIdle()

        assertTrue(effects.contains(LearningInsightsContract.LearningInsightsEffect.NavigateToHome))
        job.cancel()
    }
}
