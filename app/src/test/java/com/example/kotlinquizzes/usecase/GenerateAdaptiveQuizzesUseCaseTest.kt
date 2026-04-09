package com.example.kotlinquizzes.usecase

import com.example.kotlinquizzes.feature.quiz.data.local.AssetsQuizDataSource
import com.example.kotlinquizzes.feature.quiz.data.local.QuizUserStateDataStore
import com.example.kotlinquizzes.feature.quiz.data.model.QuestionDto
import com.example.kotlinquizzes.feature.quiz.data.model.QuizDto
import com.example.kotlinquizzes.feature.quiz.data.remote.ClaudeQuizApiClient
import com.example.kotlinquizzes.feature.quiz.domain.usecase.GenerateAdaptiveQuizzesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class GenerateAdaptiveQuizzesUseCaseTest {

    @Mock
    private lateinit var userState: QuizUserStateDataStore

    @Mock
    private lateinit var claudeClient: ClaudeQuizApiClient

    @Mock
    private lateinit var assetsDataSource: AssetsQuizDataSource

    private lateinit var generateAdaptiveQuizzesUseCase: GenerateAdaptiveQuizzesUseCase
    private lateinit var closeable: AutoCloseable
    private val testDispatcher = StandardTestDispatcher()
    private val json = Json { ignoreUnknownKeys = true }

    private val sampleStaticJson = """
        {
          "quizzes": [
            {
              "id": "static_1",
              "title": "Static Quiz",
              "questions": [
                { "id": "s_q1", "text": "Q1?", "options": ["A","B"], "correctIndex": 0, "tags": ["kotlin"] },
                { "id": "s_q2", "text": "Q2?", "options": ["A","B"], "correctIndex": 1, "tags": ["android"] },
                { "id": "s_q3", "text": "Q3?", "options": ["A","B"], "correctIndex": 0, "tags": ["compose"] }
              ]
            }
          ]
        }
    """.trimIndent()

    private val claudeQuizzes = listOf(
        QuizDto(
            id = "remote_a",
            title = "Remote A",
            questions = listOf(
                QuestionDto("rq1", "RQ?", listOf("A", "B"), 0, listOf("kotlin"))
            ),
        ),
        QuizDto(
            id = "remote_b",
            title = "Remote B",
            questions = listOf(
                QuestionDto("rq2", "RQ?", listOf("A", "B"), 1, listOf("android"))
            ),
        ),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        closeable = MockitoAnnotations.openMocks(this)
        generateAdaptiveQuizzesUseCase = GenerateAdaptiveQuizzesUseCase(
            userState = userState,
            claudeClient = claudeClient,
            assetsDataSource = assetsDataSource,
            json = json,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        closeable.close()
    }

    @Test
    fun testInvoke_WhenClaudeConfigured_UsesClaudeAndAssignsUniqueIds() = runTest {
        whenever(userState.getWeakestTags(5)).thenReturn(listOf("kotlin"))
        whenever(claudeClient.isConfigured).thenReturn(true)
        whenever(claudeClient.generateQuizzes(listOf("kotlin"), 2, 10))
            .thenReturn(claudeQuizzes)

        generateAdaptiveQuizzesUseCase()

        verify(assetsDataSource, never()).readQuizzesJson()

        val captor = argumentCaptor<List<QuizDto>>()
        verify(userState).addGeneratedQuizzes(captor.capture())
        val saved = captor.firstValue
        assertEquals(2, saved.size)
        // Unique ids assigned by index, original ids overwritten.
        assertTrue(saved.all { it.id.startsWith("generated_") })
        assertEquals(saved.size, saved.map { it.id }.distinct().size)
        // Original titles preserved.
        assertEquals(listOf("Remote A", "Remote B"), saved.map { it.title })
    }

    @Test
    fun testInvoke_WhenClaudeNotConfigured_UsesLocalFallback() = runTest {
        whenever(userState.getWeakestTags(5)).thenReturn(listOf("kotlin"))
        whenever(claudeClient.isConfigured).thenReturn(false)
        whenever(assetsDataSource.readQuizzesJson()).thenReturn(sampleStaticJson)

        generateAdaptiveQuizzesUseCase(quizCount = 2, questionsPerQuiz = 2)

        verify(claudeClient, never()).generateQuizzes(
            org.mockito.kotlin.any(),
            org.mockito.kotlin.any(),
            org.mockito.kotlin.any(),
        )
        verify(assetsDataSource).readQuizzesJson()

        val captor = argumentCaptor<List<QuizDto>>()
        verify(userState).addGeneratedQuizzes(captor.capture())
        val saved = captor.firstValue
        assertEquals(2, saved.size)
        assertTrue(saved.all { it.id.startsWith("generated_") })
        // Title derived from weakest tag (capitalized).
        assertTrue(saved.all { it.title.startsWith("Kotlin Practice") })
        // Each quiz has the requested number of questions.
        assertTrue(saved.all { it.questions.size == 2 })
        // The kotlin-tagged question must be prioritised first.
        assertEquals("kotlin", saved[0].questions[0].tags.first())
    }

    @Test
    fun testInvoke_WhenClaudeThrows_FallsBackToLocalGenerator() = runTest {
        whenever(userState.getWeakestTags(5)).thenReturn(listOf("kotlin"))
        whenever(claudeClient.isConfigured).thenReturn(true)
        whenever(claudeClient.generateQuizzes(listOf("kotlin"), 2, 10))
            .thenThrow(RuntimeException("network down"))
        whenever(assetsDataSource.readQuizzesJson()).thenReturn(sampleStaticJson)

        // Should not propagate.
        generateAdaptiveQuizzesUseCase()

        verify(assetsDataSource).readQuizzesJson()
        val captor = argumentCaptor<List<QuizDto>>()
        verify(userState).addGeneratedQuizzes(captor.capture())
        assertEquals(2, captor.firstValue.size)
    }

    @Test
    fun testInvoke_LocalFallback_WhenNoWeakTags_UsesPracticeTitle() = runTest {
        whenever(userState.getWeakestTags(5)).thenReturn(emptyList())
        whenever(claudeClient.isConfigured).thenReturn(false)
        whenever(assetsDataSource.readQuizzesJson()).thenReturn(sampleStaticJson)

        generateAdaptiveQuizzesUseCase(quizCount = 1, questionsPerQuiz = 1)

        val captor = argumentCaptor<List<QuizDto>>()
        verify(userState).addGeneratedQuizzes(captor.capture())
        val saved = captor.firstValue
        assertEquals(1, saved.size)
        assertTrue(saved[0].title.startsWith("Practice"))
    }

    @Test
    fun testInvoke_LocalFallback_WhenQuestionPoolEmpty_SavesEmptyList() = runTest {
        whenever(userState.getWeakestTags(5)).thenReturn(listOf("kotlin"))
        whenever(claudeClient.isConfigured).thenReturn(false)
        whenever(assetsDataSource.readQuizzesJson()).thenReturn("""{"quizzes":[]}""")

        generateAdaptiveQuizzesUseCase()

        verify(userState).addGeneratedQuizzes(eq(emptyList()))
    }

    @Test
    fun testInvoke_PassesCustomQuizCountAndQuestionsPerQuizToClaude() = runTest {
        whenever(userState.getWeakestTags(5)).thenReturn(listOf("compose"))
        whenever(claudeClient.isConfigured).thenReturn(true)
        whenever(claudeClient.generateQuizzes(listOf("compose"), 4, 5))
            .thenReturn(emptyList())

        generateAdaptiveQuizzesUseCase(quizCount = 4, questionsPerQuiz = 5)

        verify(claudeClient).generateQuizzes(listOf("compose"), 4, 5)
    }
}
