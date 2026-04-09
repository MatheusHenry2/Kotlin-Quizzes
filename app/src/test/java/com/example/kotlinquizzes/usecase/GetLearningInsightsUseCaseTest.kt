package com.example.kotlinquizzes.usecase

import com.example.kotlinquizzes.feature.quiz.domain.model.InsightsSnapshot
import com.example.kotlinquizzes.feature.quiz.domain.repository.QuizRepository
import com.example.kotlinquizzes.feature.quiz.domain.usecase.GetLearningInsightsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class GetLearningInsightsUseCaseTest {

    @Mock
    private lateinit var quizRepository: QuizRepository

    private lateinit var getLearningInsightsUseCase: GetLearningInsightsUseCase
    private lateinit var closeable: AutoCloseable
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        closeable = MockitoAnnotations.openMocks(this)
        getLearningInsightsUseCase = GetLearningInsightsUseCase(quizRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        closeable.close()
    }

    @Test
    fun testInvoke_WhenEmptySnapshot_ReturnsZeroedInsights() = runTest {
        whenever(quizRepository.getInsightsSnapshot()).thenReturn(
            InsightsSnapshot(
                totalCorrect = 0,
                totalIncorrect = 0,
                completedQuizCount = 0,
                tagAttempts = emptyMap(),
                tagMistakes = emptyMap(),
            )
        )

        val insights = getLearningInsightsUseCase()

        assertEquals(0, insights.totalQuizzesCompleted)
        assertEquals(0, insights.totalCorrect)
        assertEquals(0, insights.totalIncorrect)
        assertEquals(0, insights.accuracyPercent)
        assertTrue(insights.masteryByCategory.isEmpty())
        assertTrue(insights.topicsToImprove.isEmpty())
    }

    @Test
    fun testInvoke_WhenAllCorrect_ReturnsHundredPercentAccuracyAndNoWeakTopics() = runTest {
        whenever(quizRepository.getInsightsSnapshot()).thenReturn(
            InsightsSnapshot(
                totalCorrect = 10,
                totalIncorrect = 0,
                completedQuizCount = 2,
                tagAttempts = mapOf("kotlin" to 6, "android" to 4),
                tagMistakes = emptyMap(),
            )
        )

        val insights = getLearningInsightsUseCase()

        assertEquals(100, insights.accuracyPercent)
        assertEquals(2, insights.totalQuizzesCompleted)
        // All categories at 100%, no weak topics.
        assertEquals(2, insights.masteryByCategory.size)
        assertTrue(insights.masteryByCategory.all { it.masteryPercent == 100 })
        assertTrue(insights.topicsToImprove.isEmpty())
    }

    @Test
    fun testInvoke_WhenMixedAnswers_ComputesAccuracyAndMasterySorting() = runTest {
        whenever(quizRepository.getInsightsSnapshot()).thenReturn(
            InsightsSnapshot(
                totalCorrect = 12,
                totalIncorrect = 8,
                completedQuizCount = 4,
                tagAttempts = mapOf(
                    "kotlin" to 10,   // most attempts
                    "android" to 6,
                    "compose" to 4,
                ),
                tagMistakes = mapOf(
                    "kotlin" to 2,    // 80% mastery
                    "android" to 3,   // 50% mastery
                    "compose" to 1,   // 75% mastery
                ),
            )
        )

        val insights = getLearningInsightsUseCase()

        assertEquals(60, insights.accuracyPercent) // 12 / 20
        // Mastery is sorted by attempts desc, top 3.
        assertEquals(listOf("kotlin", "android", "compose"), insights.masteryByCategory.map { it.tag })
        assertEquals(80, insights.masteryByCategory[0].masteryPercent)
        assertEquals(50, insights.masteryByCategory[1].masteryPercent)
        assertEquals(75, insights.masteryByCategory[2].masteryPercent)
    }

    @Test
    fun testInvoke_WeakTopics_AreSortedByErrorRateAndLimitedToTop3() = runTest {
        whenever(quizRepository.getInsightsSnapshot()).thenReturn(
            InsightsSnapshot(
                totalCorrect = 10,
                totalIncorrect = 20,
                completedQuizCount = 5,
                tagAttempts = mapOf(
                    "a" to 10,  // 50%
                    "b" to 10,  // 80%
                    "c" to 10,  // 30%
                    "d" to 10,  // 70%
                    "e" to 10,  // 10%
                ),
                tagMistakes = mapOf(
                    "a" to 5,
                    "b" to 8,
                    "c" to 3,
                    "d" to 7,
                    "e" to 1,
                ),
            )
        )

        val insights = getLearningInsightsUseCase()

        assertEquals(3, insights.topicsToImprove.size)
        assertEquals(listOf("b", "d", "a"), insights.topicsToImprove.map { it.tag })
        assertEquals(listOf(80, 70, 50), insights.topicsToImprove.map { it.errorRatePercent })
    }

    @Test
    fun testInvoke_WeakTopics_ExcludesTagsWithZeroMistakes() = runTest {
        whenever(quizRepository.getInsightsSnapshot()).thenReturn(
            InsightsSnapshot(
                totalCorrect = 10,
                totalIncorrect = 2,
                completedQuizCount = 1,
                tagAttempts = mapOf("kotlin" to 5, "android" to 7),
                tagMistakes = mapOf("kotlin" to 0, "android" to 2),
            )
        )

        val insights = getLearningInsightsUseCase()

        assertEquals(1, insights.topicsToImprove.size)
        assertEquals("android", insights.topicsToImprove[0].tag)
    }

    @Test
    fun testInvoke_WhenTotalAnsweredIsZero_AccuracyIsZeroNotDivByZero() = runTest {
        whenever(quizRepository.getInsightsSnapshot()).thenReturn(
            InsightsSnapshot(
                totalCorrect = 0,
                totalIncorrect = 0,
                completedQuizCount = 1,
                tagAttempts = mapOf("kotlin" to 0),
                tagMistakes = emptyMap(),
            )
        )

        val insights = getLearningInsightsUseCase()

        assertEquals(0, insights.accuracyPercent)
        // Tag with 0 attempts gets mastery 0 and is excluded from weak.
        assertEquals(0, insights.masteryByCategory[0].masteryPercent)
        assertTrue(insights.topicsToImprove.isEmpty())
    }

    @Test
    fun testInvoke_MasteryClampsCorrectAtZeroWhenMistakesExceedAttempts() = runTest {
        whenever(quizRepository.getInsightsSnapshot()).thenReturn(
            InsightsSnapshot(
                totalCorrect = 0,
                totalIncorrect = 5,
                completedQuizCount = 1,
                tagAttempts = mapOf("kotlin" to 3),
                tagMistakes = mapOf("kotlin" to 99),
            )
        )

        val insights = getLearningInsightsUseCase()

        assertEquals(0, insights.masteryByCategory[0].masteryPercent)
    }
}
