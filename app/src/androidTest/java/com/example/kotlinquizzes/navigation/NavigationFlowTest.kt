package com.example.kotlinquizzes.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.kotlinquizzes.core.navigation.BottomNavBar
import com.example.kotlinquizzes.core.navigation.BottomNavDestination
import com.example.kotlinquizzes.core.theme.KotlinQuizzesTheme
import com.example.kotlinquizzes.core.utils.TestTags
import com.example.kotlinquizzes.feature.quiz.domain.model.LearningInsights
import com.example.kotlinquizzes.feature.quiz.domain.model.Question
import com.example.kotlinquizzes.feature.quiz.domain.model.Quiz
import com.example.kotlinquizzes.feature.quiz.presentation.insights.LearningInsightsContent
import com.example.kotlinquizzes.feature.quiz.presentation.insights.LearningInsightsContract.LearningInsightsState
import com.example.kotlinquizzes.feature.quiz.presentation.quizlist.QuizListContent
import com.example.kotlinquizzes.feature.quiz.presentation.quizlist.QuizListContract.QuizListState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NavigationFlowTest {

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
        )
    )

    // --- Bottom Navigation Tests ---

    @Test
    fun bottomNavBar_homeTabSelected_showsHomeSelected() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                BottomNavBar(
                    current = BottomNavDestination.HOME,
                    onNavigate = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_HOME).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_INSIGHTS).assertIsDisplayed()
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("Insights").assertIsDisplayed()
    }

    @Test
    fun bottomNavBar_insightsTabSelected_showsInsightsSelected() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                BottomNavBar(
                    current = BottomNavDestination.INSIGHTS,
                    onNavigate = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_HOME).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_INSIGHTS).assertIsDisplayed()
    }

    @Test
    fun bottomNavBar_clickingInsightsFromHome_triggersNavigation() {
        var navigatedTo: BottomNavDestination? = null
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                BottomNavBar(
                    current = BottomNavDestination.HOME,
                    onNavigate = { navigatedTo = it },
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_INSIGHTS).performClick()
        assertEquals(BottomNavDestination.INSIGHTS, navigatedTo)
    }

    @Test
    fun bottomNavBar_clickingHomeFromInsights_triggersNavigation() {
        var navigatedTo: BottomNavDestination? = null
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                BottomNavBar(
                    current = BottomNavDestination.INSIGHTS,
                    onNavigate = { navigatedTo = it },
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_HOME).performClick()
        assertEquals(BottomNavDestination.HOME, navigatedTo)
    }

    @Test
    fun bottomNavBar_clickingSameTab_doesNotNavigate() {
        var navigated = false
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                BottomNavBar(
                    current = BottomNavDestination.HOME,
                    onNavigate = { navigated = true },
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_HOME).performClick()
        assertFalse("Should not navigate when clicking the same tab", navigated)
    }

    @Test
    fun bottomNavBar_rapidSwitching_doesNotCrash() {
        var lastNavigation: BottomNavDestination? = null
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                BottomNavBar(
                    current = BottomNavDestination.HOME,
                    onNavigate = { lastNavigation = it },
                )
            }
        }

        // Rapidly click between tabs
        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_INSIGHTS).performClick()
        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_HOME).performClick()
        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_INSIGHTS).performClick()
        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_HOME).performClick()

        // The nav bar should still render correctly after rapid clicks
        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_HOME).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_INSIGHTS).assertIsDisplayed()
    }

    // --- Cross-screen Flow Simulation ---

    @Test
    fun quizListToInsights_insightsShowsContent() {
        // Simulate: user is on QuizList and navigates to Insights
        var onInsightsScreen = false

        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizListContent(
                    state = QuizListState(
                        isLoading = false,
                        userName = "Test",
                        quizzes = sampleQuizzes
                    ),
                    onAction = {},
                    onNavigateToInsights = { onInsightsScreen = true },
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_INSIGHTS).performClick()
        assertTrue(onInsightsScreen)
    }

    @Test
    fun insightsToHome_homeShowsContent() {
        // Simulate: user is on Insights and navigates to Home
        var onHomeScreen = false

        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LearningInsightsContent(
                    state = LearningInsightsState(
                        isLoading = false,
                        insights = LearningInsights(
                            totalQuizzesCompleted = 1,
                            totalCorrect = 5,
                            totalIncorrect = 2,
                            accuracyPercent = 71,
                            masteryByCategory = emptyList(),
                            topicsToImprove = emptyList(),
                        )
                    ),
                    onAction = {},
                    onNavigateToHome = { onHomeScreen = true },
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_HOME).performClick()
        assertTrue(onHomeScreen)
    }

    // --- Avatar Blank Screen Regression Tests ---

    @Test
    fun avatarClick_doesNotNavigateAnywhere() {
        var navigatedToInsights = false
        var receivedAction: Any? = null

        composeTestRule.setContent {
            KotlinQuizzesTheme {
                QuizListContent(
                    state = QuizListState(
                        isLoading = false,
                        userName = "Matheus",
                        quizzes = sampleQuizzes
                    ),
                    onAction = { receivedAction = it },
                    onNavigateToInsights = { navigatedToInsights = true },
                )
            }
        }

        // Click the avatar
        composeTestRule.onNodeWithTag(TestTags.AVATAR_CIRCLE).performClick()

        // Should NOT trigger navigation or any action
        assertFalse("Avatar click should not navigate to insights", navigatedToInsights)
        // The quiz list should still be visible
        composeTestRule.onNodeWithTag(TestTags.QUIZ_LIST_CONTENT).assertIsDisplayed()
    }

    @Test
    fun rapidAvatarClicks_screenRemainsStable() {
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
        // Rapidly click the avatar 10 times
        repeat(10) {
            avatar.performClick()
        }

        // Screen should remain stable
        composeTestRule.onNodeWithTag(TestTags.QUIZ_LIST_CONTENT).assertIsDisplayed()
        composeTestRule.onNodeWithText("Kotlin Basics").assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.WELCOME_TEXT).assertIsDisplayed()
    }

    // --- Screen Rendering Stability Tests ---

    @Test
    fun quizListScreen_allElementsRendered() {
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

        // Verify all critical elements are rendered
        composeTestRule.onNodeWithTag(TestTags.AVATAR_CIRCLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.WELCOME_TEXT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.QUIZ_LIST_CONTENT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_HOME).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_INSIGHTS).assertIsDisplayed()
        composeTestRule.onNodeWithText("Kotlin Quizzes").assertIsDisplayed()
    }

    @Test
    fun insightsScreen_allElementsRendered() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LearningInsightsContent(
                    state = LearningInsightsState(
                        isLoading = false,
                        insights = LearningInsights(
                            totalQuizzesCompleted = 3,
                            totalCorrect = 25,
                            totalIncorrect = 5,
                            accuracyPercent = 83,
                            masteryByCategory = emptyList(),
                            topicsToImprove = emptyList(),
                        )
                    ),
                    onAction = {},
                    onNavigateToHome = {},
                )
            }
        }

        // Verify all critical elements are rendered
        composeTestRule.onNodeWithTag(TestTags.INSIGHTS_CONTENT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_HOME).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_INSIGHTS).assertIsDisplayed()
        composeTestRule.onNodeWithText("83%").assertIsDisplayed()
    }
}
