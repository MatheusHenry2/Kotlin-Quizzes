package com.example.kotlinquizzes.quiz

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.kotlinquizzes.R
import com.example.kotlinquizzes.core.theme.KotlinQuizzesTheme
import com.example.kotlinquizzes.core.utils.TestTags
import com.example.kotlinquizzes.feature.quiz.domain.model.Question
import com.example.kotlinquizzes.feature.quiz.presentation.quiz.QuizContent
import com.example.kotlinquizzes.feature.quiz.presentation.quiz.QuizContract.QuizAction
import com.example.kotlinquizzes.feature.quiz.presentation.quiz.QuizContract.QuizState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class QuizScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleQuestions = listOf(
        Question(
            id = "q1",
            text = "What keyword declares an immutable variable in Kotlin?",
            options = listOf("var", "val", "const", "let"),
            correctIndex = 1,
            tags = listOf("basics")
        ),
        Question(
            id = "q2",
            text = "Which scope function uses 'it' as the context object?",
            options = listOf("apply", "run", "let", "with"),
            correctIndex = 2,
            tags = listOf("scope_functions")
        ),
    )

    // --- Loading State ---

    @Test
    fun loadingState_showsLoadingIndicator() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizContent(
                    state = QuizState(isLoading = true),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.QUIZ_LOADING).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.QUIZ_CONTENT).assertDoesNotExist()
    }

    // --- Error State ---

    @Test
    fun errorState_showsErrorMessage() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizContent(
                    state = QuizState(
                        isLoading = false,
                        errorMessageResId = R.string.error_quiz_not_found
                    ),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.QUIZ_ERROR).assertIsDisplayed()
        composeTestRule.onNodeWithText("Quiz not found").assertIsDisplayed()
    }

    // --- Content State ---

    @Test
    fun contentState_showsQuestionText() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizContent(
                    state = QuizState(
                        isLoading = false,
                        questions = sampleQuestions,
                        currentQuestionIndex = 0
                    ),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.QUIZ_CONTENT).assertIsDisplayed()
        composeTestRule
            .onNodeWithText("What keyword declares an immutable variable in Kotlin?")
            .assertIsDisplayed()
    }

    @Test
    fun contentState_showsAllOptions() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizContent(
                    state = QuizState(
                        isLoading = false,
                        questions = sampleQuestions,
                        currentQuestionIndex = 0
                    ),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithText("var").assertIsDisplayed()
        composeTestRule.onNodeWithText("val").assertIsDisplayed()
        composeTestRule.onNodeWithText("const").assertIsDisplayed()
        composeTestRule.onNodeWithText("let").assertIsDisplayed()
    }

    @Test
    fun contentState_showsProgressCounter() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizContent(
                    state = QuizState(
                        isLoading = false,
                        questions = sampleQuestions,
                        currentQuestionIndex = 0
                    ),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithText("1 of 2").assertIsDisplayed()
    }

    @Test
    fun optionSelection_triggersAction() {
        var receivedAction: QuizAction? = null
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizContent(
                    state = QuizState(
                        isLoading = false,
                        questions = sampleQuestions,
                        currentQuestionIndex = 0
                    ),
                    onAction = { receivedAction = it },
                )
            }
        }

        composeTestRule.onNodeWithText("val").performClick()
        assertEquals(QuizAction.OptionSelected(1), receivedAction)
    }

    @Test
    fun nextButton_disabledWhenNoOptionSelected() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizContent(
                    state = QuizState(
                        isLoading = false,
                        questions = sampleQuestions,
                        currentQuestionIndex = 0,
                        selectedOptionIndex = null
                    ),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Next").assertIsNotEnabled()
    }

    @Test
    fun nextButton_enabledWhenOptionSelected() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizContent(
                    state = QuizState(
                        isLoading = false,
                        questions = sampleQuestions,
                        currentQuestionIndex = 0,
                        selectedOptionIndex = 1
                    ),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Next").assertIsEnabled()
    }

    @Test
    fun nextButton_clickTriggersAction() {
        var receivedAction: QuizAction? = null
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizContent(
                    state = QuizState(
                        isLoading = false,
                        questions = sampleQuestions,
                        currentQuestionIndex = 0,
                        selectedOptionIndex = 1
                    ),
                    onAction = { receivedAction = it },
                )
            }
        }

        composeTestRule.onNodeWithText("Next").performClick()
        assertEquals(QuizAction.NextClicked, receivedAction)
    }

    @Test
    fun lastQuestion_showsFinishButton() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizContent(
                    state = QuizState(
                        isLoading = false,
                        questions = sampleQuestions,
                        currentQuestionIndex = 1,
                        selectedOptionIndex = 0
                    ),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Finish").assertIsDisplayed()
    }

    @Test
    fun secondQuestion_showsCorrectProgress() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizContent(
                    state = QuizState(
                        isLoading = false,
                        questions = sampleQuestions,
                        currentQuestionIndex = 1
                    ),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithText("2 of 2").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Which scope function uses 'it' as the context object?")
            .assertIsDisplayed()
    }

    // --- Answer Checking State ---

    @Test
    fun checkingAnswer_disablesNextButton() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizContent(
                    state = QuizState(
                        isLoading = false,
                        questions = sampleQuestions,
                        currentQuestionIndex = 0,
                        selectedOptionIndex = 1,
                        isCheckingAnswer = true,
                        selectedOptionIsCorrect = true
                    ),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Next").assertIsNotEnabled()
    }

    // --- Close Quiz ---

    @Test
    fun closeButton_triggersCloseAction() {
        var receivedAction: QuizAction? = null
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizContent(
                    state = QuizState(
                        isLoading = false,
                        questions = sampleQuestions,
                        currentQuestionIndex = 0
                    ),
                    onAction = { receivedAction = it },
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Close quiz")
            .performClick()
        assertEquals(QuizAction.CloseClicked, receivedAction)
    }

    // --- Empty Questions Edge Case ---

    @Test
    fun emptyQuestionsList_showsNoContent() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizContent(
                    state = QuizState(
                        isLoading = false,
                        questions = emptyList()
                    ),
                    onAction = {},
                )
            }
        }

        // With empty questions and currentQuestion = null, none of the when branches match
        // → no content, loading, or error shown. This verifies the state is safe.
        composeTestRule.onNodeWithTag(TestTags.QUIZ_CONTENT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TestTags.QUIZ_LOADING).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TestTags.QUIZ_ERROR).assertDoesNotExist()
    }
}
