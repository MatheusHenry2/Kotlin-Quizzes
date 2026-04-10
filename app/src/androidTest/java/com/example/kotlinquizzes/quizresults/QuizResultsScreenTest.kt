package com.example.kotlinquizzes.quizresults

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.kotlinquizzes.core.theme.KotlinQuizzesTheme
import com.example.kotlinquizzes.core.utils.TestTags
import com.example.kotlinquizzes.feature.quiz.presentation.quizresults.QuizResultsContent
import com.example.kotlinquizzes.feature.quiz.presentation.quizresults.QuizResultsContract.QuizResultsAction
import com.example.kotlinquizzes.feature.quiz.presentation.quizresults.QuizResultsContract.QuizResultsState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class QuizResultsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- Content Rendering ---

    @Test
    fun resultsScreen_showsCongratulationsMessage() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizResultsContent(
                    state = QuizResultsState(totalQuestions = 10, correctAnswers = 8),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.RESULTS_CONTENT).assertIsDisplayed()
        composeTestRule.onNodeWithText("Congratulations!").assertIsDisplayed()
        composeTestRule.onNodeWithText("You have completed the quiz.").assertIsDisplayed()
    }

    @Test
    fun resultsScreen_showsCorrectScorePercentage() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizResultsContent(
                    state = QuizResultsState(totalQuestions = 10, correctAnswers = 8),
                    onAction = {},
                )
            }
        }

        // 80% score
        composeTestRule.onNodeWithText("80%").assertIsDisplayed()
    }

    @Test
    fun resultsScreen_showsCorrectAndIncorrectCounts() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizResultsContent(
                    state = QuizResultsState(totalQuestions = 10, correctAnswers = 7),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithText("7").assertIsDisplayed()
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun resultsScreen_perfectScore() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizResultsContent(
                    state = QuizResultsState(totalQuestions = 5, correctAnswers = 5),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithText("100%").assertIsDisplayed()
        composeTestRule.onNodeWithText("0").assertIsDisplayed() // 0 incorrect
    }

    @Test
    fun resultsScreen_zeroScore() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizResultsContent(
                    state = QuizResultsState(totalQuestions = 5, correctAnswers = 0),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithText("0%").assertIsDisplayed()
    }

    // --- Navigation ---

    @Test
    fun backToHomeButton_triggersAction() {
        var receivedAction: QuizResultsAction? = null
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizResultsContent(
                    state = QuizResultsState(totalQuestions = 10, correctAnswers = 8),
                    onAction = { receivedAction = it },
                )
            }
        }

        composeTestRule.onNodeWithText("Back to Home").performClick()
        assertEquals(QuizResultsAction.BackToHomeClicked, receivedAction)
    }

    @Test
    fun closeButton_triggersBackToHomeAction() {
        var receivedAction: QuizResultsAction? = null
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizResultsContent(
                    state = QuizResultsState(totalQuestions = 10, correctAnswers = 8),
                    onAction = { receivedAction = it },
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Close results")
            .performClick()
        assertEquals(QuizResultsAction.BackToHomeClicked, receivedAction)
    }

    // --- Score Calculation Edge Cases ---

    @Test
    fun resultsScreen_singleQuestion_correctAnswer() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizResultsContent(
                    state = QuizResultsState(totalQuestions = 1, correctAnswers = 1),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithText("100%").assertIsDisplayed()
    }

    @Test
    fun resultsScreen_singleQuestion_incorrectAnswer() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizResultsContent(
                    state = QuizResultsState(totalQuestions = 1, correctAnswers = 0),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithText("0%").assertIsDisplayed()
    }

    // --- Rapid Click Tests ---

    @Test
    fun rapidBackToHomeClicks_allFireActions() {
        val receivedActions = mutableListOf<QuizResultsAction>()
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizResultsContent(
                    state = QuizResultsState(totalQuestions = 10, correctAnswers = 8),
                    onAction = { receivedActions.add(it) },
                )
            }
        }

        val button = composeTestRule.onNodeWithText("Back to Home")
        button.performClick()
        button.performClick()
        button.performClick()

        // All clicks should be registered (ViewModel handles deduplication)
        assertEquals(3, receivedActions.size)
        receivedActions.forEach {
            assertEquals(QuizResultsAction.BackToHomeClicked, it)
        }
    }
}
