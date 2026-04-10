package com.example.kotlinquizzes.insights

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.kotlinquizzes.R
import com.example.kotlinquizzes.core.theme.KotlinQuizzesTheme
import com.example.kotlinquizzes.core.utils.TestTags
import com.example.kotlinquizzes.feature.quiz.domain.model.CategoryMastery
import com.example.kotlinquizzes.feature.quiz.domain.model.LearningInsights
import com.example.kotlinquizzes.feature.quiz.domain.model.WeakTopic
import com.example.kotlinquizzes.feature.quiz.presentation.insights.LearningInsightsContent
import com.example.kotlinquizzes.feature.quiz.presentation.insights.LearningInsightsContract.LearningInsightsAction
import com.example.kotlinquizzes.feature.quiz.presentation.insights.LearningInsightsContract.LearningInsightsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LearningInsightsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleInsights = LearningInsights(
        totalQuizzesCompleted = 5,
        totalCorrect = 40,
        totalIncorrect = 10,
        accuracyPercent = 80,
        masteryByCategory = listOf(
            CategoryMastery(tag = "kotlin_basics", masteryPercent = 90),
            CategoryMastery(tag = "coroutines", masteryPercent = 70),
        ),
        topicsToImprove = listOf(
            WeakTopic(tag = "generics", errorRatePercent = 45),
        ),
    )

    // --- Loading State Tests ---

    @Test
    fun loadingState_showsLoadingIndicator() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LearningInsightsContent(
                    state = LearningInsightsState(isLoading = true),
                    onAction = {},
                    onNavigateToHome = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.INSIGHTS_LOADING).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.INSIGHTS_CONTENT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TestTags.INSIGHTS_ERROR).assertDoesNotExist()
    }

    // --- Error State Tests ---

    @Test
    fun errorState_showsErrorAndRetryButton() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LearningInsightsContent(
                    state = LearningInsightsState(
                        isLoading = false,
                        errorMessageResId = R.string.error_failed_load_insights
                    ),
                    onAction = {},
                    onNavigateToHome = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.INSIGHTS_ERROR).assertIsDisplayed()
        composeTestRule.onNodeWithText("Try Again").assertIsDisplayed()
    }

    @Test
    fun errorState_retryClickTriggersAction() {
        var receivedAction: LearningInsightsAction? = null
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LearningInsightsContent(
                    state = LearningInsightsState(
                        isLoading = false,
                        errorMessageResId = R.string.error_failed_load_insights
                    ),
                    onAction = { receivedAction = it },
                    onNavigateToHome = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Try Again").performClick()
        assertEquals(LearningInsightsAction.RetryClicked, receivedAction)
    }

    // --- Blank Screen Regression Test (CRITICAL) ---

    @Test
    fun blankState_doesNotShowBlankScreen() {
        // This tests the bug fix: when isLoading=false, insights=null, errorMessageResId=null
        // the screen should show a fallback loading indicator, NOT a blank screen.
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LearningInsightsContent(
                    state = LearningInsightsState(
                        isLoading = false,
                        insights = null,
                        errorMessageResId = null
                    ),
                    onAction = {},
                    onNavigateToHome = {},
                )
            }
        }

        // The screen should NOT be blank — the else branch shows a loading indicator
        // It should not show the content or error tags
        composeTestRule.onNodeWithTag(TestTags.INSIGHTS_CONTENT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TestTags.INSIGHTS_ERROR).assertDoesNotExist()
    }

    // --- Content State Tests ---

    @Test
    fun contentState_showsInsightsData() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LearningInsightsContent(
                    state = LearningInsightsState(
                        isLoading = false,
                        insights = sampleInsights
                    ),
                    onAction = {},
                    onNavigateToHome = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.INSIGHTS_CONTENT).assertIsDisplayed()
    }

    @Test
    fun contentState_showsAccuracyPercentage() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LearningInsightsContent(
                    state = LearningInsightsState(
                        isLoading = false,
                        insights = sampleInsights
                    ),
                    onAction = {},
                    onNavigateToHome = {},
                )
            }
        }

        composeTestRule.onNodeWithText("80%").assertIsDisplayed()
    }

    @Test
    fun contentState_showsTotalQuizzesCompleted() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LearningInsightsContent(
                    state = LearningInsightsState(
                        isLoading = false,
                        insights = sampleInsights
                    ),
                    onAction = {},
                    onNavigateToHome = {},
                )
            }
        }

        composeTestRule.onNodeWithText("5").assertIsDisplayed()
    }

    @Test
    fun contentState_showsCorrectAndIncorrectCounts() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LearningInsightsContent(
                    state = LearningInsightsState(
                        isLoading = false,
                        insights = sampleInsights
                    ),
                    onAction = {},
                    onNavigateToHome = {},
                )
            }
        }

        composeTestRule.onNodeWithText("40").assertIsDisplayed()
        composeTestRule.onNodeWithText("10").assertIsDisplayed()
    }

    @Test
    fun contentState_showsCategoryMastery() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LearningInsightsContent(
                    state = LearningInsightsState(
                        isLoading = false,
                        insights = sampleInsights
                    ),
                    onAction = {},
                    onNavigateToHome = {},
                )
            }
        }

        composeTestRule.onNodeWithText("KOTLIN BASICS").assertIsDisplayed()
        composeTestRule.onNodeWithText("COROUTINES").assertIsDisplayed()
        composeTestRule.onNodeWithText("90%").assertIsDisplayed()
        composeTestRule.onNodeWithText("70%").assertIsDisplayed()
    }

    @Test
    fun contentState_showsWeakTopics() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LearningInsightsContent(
                    state = LearningInsightsState(
                        isLoading = false,
                        insights = sampleInsights
                    ),
                    onAction = {},
                    onNavigateToHome = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Generics").assertIsDisplayed()
        composeTestRule.onNodeWithText("45% error rate").assertIsDisplayed()
    }

    // --- Empty Data Tests ---

    @Test
    fun contentState_emptyMastery_showsPlaceholder() {
        val emptyInsights = LearningInsights(
            totalQuizzesCompleted = 0,
            totalCorrect = 0,
            totalIncorrect = 0,
            accuracyPercent = 0,
            masteryByCategory = emptyList(),
            topicsToImprove = emptyList(),
        )
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LearningInsightsContent(
                    state = LearningInsightsState(
                        isLoading = false,
                        insights = emptyInsights
                    ),
                    onAction = {},
                    onNavigateToHome = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.INSIGHTS_CONTENT).assertIsDisplayed()
        composeTestRule.onNodeWithText("Complete quizzes to see your mastery.").assertIsDisplayed()
        composeTestRule.onNodeWithText("No weak topics yet — keep going!").assertIsDisplayed()
    }

    @Test
    fun contentState_zeroAccuracy_displaysCorrectly() {
        val zeroInsights = LearningInsights(
            totalQuizzesCompleted = 0,
            totalCorrect = 0,
            totalIncorrect = 0,
            accuracyPercent = 0,
            masteryByCategory = emptyList(),
            topicsToImprove = emptyList(),
        )
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LearningInsightsContent(
                    state = LearningInsightsState(
                        isLoading = false,
                        insights = zeroInsights
                    ),
                    onAction = {},
                    onNavigateToHome = {},
                )
            }
        }

        composeTestRule.onNodeWithText("0%").assertIsDisplayed()
    }

    // --- Navigation Tests ---

    @Test
    fun bottomNav_homeClickNavigatesHome() {
        var navigatedToHome = false
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LearningInsightsContent(
                    state = LearningInsightsState(
                        isLoading = false,
                        insights = sampleInsights
                    ),
                    onAction = {},
                    onNavigateToHome = { navigatedToHome = true },
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_HOME).performClick()
        assertTrue(navigatedToHome)
    }

    @Test
    fun backToHomeButton_triggersAction() {
        var receivedAction: LearningInsightsAction? = null
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LearningInsightsContent(
                    state = LearningInsightsState(
                        isLoading = false,
                        insights = sampleInsights
                    ),
                    onAction = { receivedAction = it },
                    onNavigateToHome = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Back to Home").performClick()
        assertEquals(LearningInsightsAction.BackToHomeClicked, receivedAction)
    }

    // --- State Transition Tests ---

    @Test
    fun stateTransition_loadingToContent_noBlankScreen() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LearningInsightsContent(
                    state = LearningInsightsState(isLoading = true),
                    onAction = {},
                    onNavigateToHome = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.INSIGHTS_LOADING).assertIsDisplayed()

        // Transition to content
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LearningInsightsContent(
                    state = LearningInsightsState(
                        isLoading = false,
                        insights = sampleInsights
                    ),
                    onAction = {},
                    onNavigateToHome = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.INSIGHTS_CONTENT).assertIsDisplayed()
    }

    @Test
    fun stateTransition_loadingToError_showsErrorState() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LearningInsightsContent(
                    state = LearningInsightsState(isLoading = true),
                    onAction = {},
                    onNavigateToHome = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.INSIGHTS_LOADING).assertIsDisplayed()

        // Transition to error
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LearningInsightsContent(
                    state = LearningInsightsState(
                        isLoading = false,
                        errorMessageResId = R.string.error_failed_load_insights
                    ),
                    onAction = {},
                    onNavigateToHome = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.INSIGHTS_ERROR).assertIsDisplayed()
    }
}
