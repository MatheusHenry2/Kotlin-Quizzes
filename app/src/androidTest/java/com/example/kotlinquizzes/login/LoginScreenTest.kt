package com.example.kotlinquizzes.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.kotlinquizzes.core.theme.KotlinQuizzesTheme
import com.example.kotlinquizzes.core.utils.TestTags
import com.example.kotlinquizzes.feature.auth.presentation.login.LoginContent
import com.example.kotlinquizzes.feature.auth.presentation.login.LoginContract.LoginAction
import com.example.kotlinquizzes.feature.auth.presentation.login.LoginContract.LoginState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- Rendering ---

    @Test
    fun loginScreen_showsWelcomeText() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LoginContent(
                    state = LoginState(),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.LOGIN_CONTENT).assertIsDisplayed()
        composeTestRule.onNodeWithText("Welcome back!").assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsSubtitle() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LoginContent(
                    state = LoginState(),
                    onAction = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText("Continue your Kotlin learning journey with our interactive quizzes.")
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsGoogleButton() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LoginContent(
                    state = LoginState(),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.LOGIN_GOOGLE_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithText("Login with Google").assertIsDisplayed()
    }

    // --- Loading State ---

    @Test
    fun loadingState_disablesGoogleButton() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LoginContent(
                    state = LoginState(isLoading = true),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.LOGIN_LOADING).assertIsNotEnabled()
    }

    @Test
    fun normalState_enablesGoogleButton() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LoginContent(
                    state = LoginState(isLoading = false),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.LOGIN_GOOGLE_BUTTON).assertIsEnabled()
    }

    // --- Actions ---

    @Test
    fun googleButtonClick_triggersSignInAction() {
        var receivedAction: LoginAction? = null
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LoginContent(
                    state = LoginState(),
                    onAction = { receivedAction = it },
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.LOGIN_GOOGLE_BUTTON).performClick()
        assertEquals(LoginAction.GoogleSignInClicked, receivedAction)
    }

    @Test
    fun backButtonClick_triggersBackAction() {
        var receivedAction: LoginAction? = null
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LoginContent(
                    state = LoginState(),
                    onAction = { receivedAction = it },
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Back")
            .performClick()
        assertEquals(LoginAction.BackClicked, receivedAction)
    }

    // --- Rapid Click Tests ---

    @Test
    fun rapidGoogleButtonClicks_firesMultipleActions() {
        val actions = mutableListOf<LoginAction>()
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LoginContent(
                    state = LoginState(),
                    onAction = { actions.add(it) },
                )
            }
        }

        val button = composeTestRule.onNodeWithTag(TestTags.LOGIN_GOOGLE_BUTTON)
        button.performClick()
        button.performClick()
        button.performClick()

        // All clicks are registered at the UI level
        assertEquals(3, actions.size)
        actions.forEach { assertEquals(LoginAction.GoogleSignInClicked, it) }
    }

    // --- State Transition ---

    @Test
    fun stateTransition_normalToLoading() {
        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LoginContent(
                    state = LoginState(isLoading = false),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.LOGIN_GOOGLE_BUTTON).assertIsEnabled()

        composeTestRule.setContent {
            KotlinQuizzesTheme {
                LoginContent(
                    state = LoginState(isLoading = true),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.LOGIN_LOADING).assertIsNotEnabled()
    }
}
