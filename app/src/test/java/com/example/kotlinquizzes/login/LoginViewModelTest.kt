package com.example.kotlinquizzes.login

import com.example.kotlinquizzes.core.ui.event.UiEventManager
import com.example.kotlinquizzes.feature.auth.data.model.SignInResult
import com.example.kotlinquizzes.feature.auth.data.model.UserData
import com.example.kotlinquizzes.feature.auth.domain.usecase.SignInWithGoogleUseCase
import com.example.kotlinquizzes.feature.auth.presentation.login.LoginContract
import com.example.kotlinquizzes.feature.auth.presentation.login.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class LoginViewModelTest {

    @Mock
    private lateinit var signInWithGoogle: SignInWithGoogleUseCase

    @Mock
    private lateinit var uiEvenetManager: UiEventManager
    private lateinit var loginViewmodel: LoginViewModel
    private lateinit var closeable: AutoCloseable
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        closeable = MockitoAnnotations.openMocks(this)
        loginViewmodel = LoginViewModel(signInWithGoogle, uiEvenetManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        closeable.close()
    }

    @Test
    fun testInitialState_WhenViewModelCreated_IsNotLoading() {
        val state = loginViewmodel.state.value

        assertFalse(state.isLoading)
    }

    @Test
    fun testHandleGoogleSignIn_WhenFinished_IsNotLoading() = runTest {
        whenever(signInWithGoogle()).thenReturn(SignInResult.Cancelled)

        loginViewmodel.onAction(LoginContract.LoginAction.GoogleSignInClicked)

        advanceUntilIdle()
        assertFalse(loginViewmodel.state.value.isLoading)
    }

    @Test
    fun testGoogleSignIn_Clicked_InvokesSignInUseCase() = runTest {
        whenever(signInWithGoogle()).thenReturn(SignInResult.Cancelled)

        loginViewmodel.onAction(LoginContract.LoginAction.GoogleSignInClicked)

        advanceUntilIdle()
        verify(signInWithGoogle).invoke()
    }

    @Test
    fun testGoogleSignIn_WhenSuccess_EmitsNavigateToHomeEffect() = runTest {
        whenever(signInWithGoogle()).thenReturn(
            SignInResult.Success(
                user = UserData(
                    "uid",
                    "name",
                    "a@b.com",
                    null
                )
            )
        )
        val effects = mutableListOf<LoginContract.LoginEffect>()
        val job = launch { loginViewmodel.effect.collect { effects.add(it) } }
        loginViewmodel.onAction(LoginContract.LoginAction.GoogleSignInClicked)

        advanceUntilIdle()
        assertTrue(effects.contains(LoginContract.LoginEffect.NavigateToHome))
        job.cancel()
    }

    @Test
    fun testGoogleSignIn_WhenCancelled_DoesNotEmitNavigateToHome() = runTest {
        whenever(signInWithGoogle()).thenReturn(SignInResult.Cancelled)
        val effects = mutableListOf<LoginContract.LoginEffect>()
        val job = launch { loginViewmodel.effect.collect { effects.add(it) } }
        loginViewmodel.onAction(LoginContract.LoginAction.GoogleSignInClicked)

        advanceUntilIdle()
        assertFalse(effects.contains(LoginContract.LoginEffect.NavigateToHome))
        job.cancel()
    }

    @Test
    fun testGoogleSignIn_WhenFailure_DoesNotEmitNavigateToHome() = runTest {
        whenever(signInWithGoogle()).thenReturn(
            SignInResult.Failure(
                RuntimeException("boom")
            )
        )
        val effects = mutableListOf<LoginContract.LoginEffect>()
        val job = launch { loginViewmodel.effect.collect { effects.add(it) } }
        loginViewmodel.onAction(LoginContract.LoginAction.GoogleSignInClicked)

        advanceUntilIdle()
        assertFalse(effects.contains(LoginContract.LoginEffect.NavigateToHome))
        job.cancel()
    }

    @Test
    fun testBackClicked_EmitsNavigateBackEffect() = runTest {
        val effects = mutableListOf<LoginContract.LoginEffect>()
        val job = launch { loginViewmodel.effect.collect { effects.add(it) } }

        loginViewmodel.onAction(LoginContract.LoginAction.BackClicked)
        advanceUntilIdle()

        assertTrue(effects.contains(LoginContract.LoginEffect.NavigateBack))
        job.cancel()
    }
}
