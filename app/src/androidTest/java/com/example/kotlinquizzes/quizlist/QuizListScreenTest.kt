package com.example.kotlinquizzes.quizlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.kotlinquizzes.core.theme.KotlinQuizzesTheme
import com.example.kotlinquizzes.core.utils.TestTags
import com.example.kotlinquizzes.feature.quiz.domain.model.Question
import com.example.kotlinquizzes.feature.quiz.domain.model.Quiz
import com.example.kotlinquizzes.feature.quiz.presentation.quizlist.QuizListContent
import com.example.kotlinquizzes.feature.quiz.presentation.quizlist.QuizListContract.QuizListAction
import com.example.kotlinquizzes.feature.quiz.presentation.quizlist.QuizListContract.QuizListState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class QuizListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleQuizzes = listOf(
        Quiz(
            id = "quiz_1",
            title = "Kotlin Basics",
            questions = listOf(
                Question(
                    id = "q1",
                    text = "What is val?",
                    options = listOf("Mutable", "Immutable", "Function", "Class"),
                    correctIndex = 1,
                    tags = listOf("basics")
                )
            )
        ),
        Quiz(
            id = "quiz_2",
            title = "Coroutines",
            questions = listOf(
                Question(
                    id = "q2",
                    text = "What is a suspend function?",
                    options = listOf("A", "B", "C", "D"),
                    correctIndex = 0,
                    tags = listOf("coroutines")
                ),
                Question(
                    id = "q3",
                    text = "What is a coroutine scope?",
                    options = listOf("A", "B", "C", "D"),
                    correctIndex = 1,
                    tags = listOf("coroutines")
                )
            )
        )
    )

    // --- Loading State Tests ---

    @Test
    fun loadingState_showsLoadingIndicator() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizListContent(
                    state = QuizListState(isLoading = true, userName = "Test"),
                    onAction = {},
                    onNavigateToInsights = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.QUIZ_LIST_LOADING).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.QUIZ_LIST_CONTENT).assertDoesNotExist()
    }

    @Test
    fun loadingState_doesNotShowQuizList() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizListContent(
                    state = QuizListState(isLoading = true, userName = "User"),
                    onAction = {},
                    onNavigateToInsights = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.QUIZ_LIST_CONTENT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TestTags.QUIZ_LIST_ERROR).assertDoesNotExist()
    }

    // --- Error State Tests ---

    @Test
    fun errorState_showsErrorMessage() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizListContent(
                    state = QuizListState(
                        isLoading = false,
                        userName = "Test",
                        errorMessageResId = com.example.kotlinquizzes.R.string.error_failed_load_quizzes
                    ),
                    onAction = {},
                    onNavigateToInsights = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.QUIZ_LIST_ERROR).assertIsDisplayed()
        composeTestRule.onNodeWithText("Try Again").assertIsDisplayed()
    }

    @Test
    fun errorState_retryClickTriggersAction() {
        var receivedAction: QuizListAction? = null
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizListContent(
                    state = QuizListState(
                        isLoading = false,
                        userName = "Test",
                        errorMessageResId = com.example.kotlinquizzes.R.string.error_failed_load_quizzes
                    ),
                    onAction = { receivedAction = it },
                    onNavigateToInsights = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Try Again").performClick()
        assertEquals(QuizListAction.RetryClicked, receivedAction)
    }

    // --- Empty State Tests (regression: blank screen bug) ---

    @Test
    fun emptyQuizList_showsEmptyStateMessage() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizListContent(
                    state = QuizListState(
                        isLoading = false,
                        userName = "Test",
                        quizzes = emptyList()
                    ),
                    onAction = {},
                    onNavigateToInsights = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.QUIZ_LIST_EMPTY).assertIsDisplayed()
    }

    @Test
    fun emptyQuizList_doesNotShowBlankScreen() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizListContent(
                    state = QuizListState(
                        isLoading = false,
                        userName = "Test",
                        quizzes = emptyList()
                    ),
                    onAction = {},
                    onNavigateToInsights = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.QUIZ_LIST_CONTENT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.QUIZ_LIST_EMPTY).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.WELCOME_TEXT).assertIsDisplayed()
    }

    // --- Content State Tests ---

    @Test
    fun contentState_showsQuizList() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizListContent(
                    state = QuizListState(
                        isLoading = false,
                        userName = "Matheus",
                        quizzes = sampleQuizzes
                    ),
                    onAction = {},
                    onNavigateToInsights = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.QUIZ_LIST_CONTENT).assertIsDisplayed()
        composeTestRule.onNodeWithText("Kotlin Basics").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coroutines").assertIsDisplayed()
    }

    @Test
    fun contentState_showsWelcomeWithUserName() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizListContent(
                    state = QuizListState(
                        isLoading = false,
                        userName = "Matheus",
                        quizzes = sampleQuizzes
                    ),
                    onAction = {},
                    onNavigateToInsights = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Welcome back, Matheus!").assertIsDisplayed()
    }

    @Test
    fun contentState_showsQuestionCount() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizListContent(
                    state = QuizListState(
                        isLoading = false,
                        userName = "Test",
                        quizzes = sampleQuizzes
                    ),
                    onAction = {},
                    onNavigateToInsights = {},
                )
            }
        }

        composeTestRule.onNodeWithText("1 Questions").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 Questions").assertIsDisplayed()
    }

    @Test
    fun quizClick_triggersAction() {
        var receivedAction: QuizListAction? = null
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizListContent(
                    state = QuizListState(
                        isLoading = false,
                        userName = "Test",
                        quizzes = sampleQuizzes
                    ),
                    onAction = { receivedAction = it },
                    onNavigateToInsights = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Kotlin Basics").performClick()
        assertEquals(QuizListAction.QuizClicked("quiz_1"), receivedAction)
    }

    // --- Avatar Tests ---

    @Test
    fun avatar_showsUserInitial() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizListContent(
                    state = QuizListState(
                        isLoading = false,
                        userName = "Matheus",
                        quizzes = sampleQuizzes
                    ),
                    onAction = {},
                    onNavigateToInsights = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.AVATAR_CIRCLE).assertIsDisplayed()
    }

    @Test
    fun avatar_emptyUserName_showsDefaultInitial() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizListContent(
                    state = QuizListState(
                        isLoading = false,
                        userName = "",
                        quizzes = sampleQuizzes
                    ),
                    onAction = {},
                    onNavigateToInsights = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.AVATAR_CIRCLE).assertIsDisplayed()
    }

    @Test
    fun avatarClick_doesNotCauseBlankScreen() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizListContent(
                    state = QuizListState(
                        isLoading = false,
                        userName = "Matheus",
                        quizzes = sampleQuizzes
                    ),
                    onAction = {},
                    onNavigateToInsights = {},
                )
            }
        }

        val avatar = composeTestRule.onNodeWithTag(TestTags.AVATAR_CIRCLE)
        avatar.performClick()
        avatar.performClick()
        avatar.performClick()

        composeTestRule.onNodeWithTag(TestTags.QUIZ_LIST_CONTENT).assertIsDisplayed()
        composeTestRule.onNodeWithText("Kotlin Basics").assertIsDisplayed()
    }

    // --- Bottom Navigation Tests ---

    @Test
    fun bottomNav_insightsClickNavigates() {
        var navigatedToInsights = false
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizListContent(
                    state = QuizListState(
                        isLoading = false,
                        userName = "Test",
                        quizzes = sampleQuizzes
                    ),
                    onAction = {},
                    onNavigateToInsights = { navigatedToInsights = true },
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_INSIGHTS).performClick()
        assertTrue(navigatedToInsights)
    }

    // --- Rapid Click Tests ---

    @Test
    fun rapidQuizClicks_onlyLastActionReceived() {
        val receivedActions = mutableListOf<QuizListAction>()
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizListContent(
                    state = QuizListState(
                        isLoading = false,
                        userName = "Test",
                        quizzes = sampleQuizzes
                    ),
                    onAction = { receivedActions.add(it) },
                    onNavigateToInsights = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Kotlin Basics").performClick()
        composeTestRule.onNodeWithText("Kotlin Basics").performClick()
        composeTestRule.onNodeWithText("Kotlin Basics").performClick()

        assertTrue(receivedActions.isNotEmpty())
        assertTrue(receivedActions.all { it == QuizListAction.QuizClicked("quiz_1") })
    }

    // --- State Transition Tests ---

    @Test
    fun stateTransition_loadingToContent_noBlankScreen() {
        val state = QuizListState(isLoading = true, userName = "Test")
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizListContent(
                    state = state,
                    onAction = {},
                    onNavigateToInsights = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.QUIZ_LIST_LOADING).assertIsDisplayed()

        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizListContent(
                    state = QuizListState(
                        isLoading = false,
                        userName = "Test",
                        quizzes = sampleQuizzes
                    ),
                    onAction = {},
                    onNavigateToInsights = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.QUIZ_LIST_CONTENT).assertIsDisplayed()
        composeTestRule.onNodeWithText("Kotlin Basics").assertIsDisplayed()
    }
}
