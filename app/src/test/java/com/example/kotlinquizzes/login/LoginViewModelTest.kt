package com.example.kotlinquizzes.login

import android.app.Application
import com.example.kotlinquizzes.R

import com.example.kotlinquizzes.feature.auth.data.client.GoogleAuthClient
import com.example.kotlinquizzes.feature.auth.data.model.SignInResult
import com.example.kotlinquizzes.feature.auth.data.model.UserData
import com.example.kotlinquizzes.feature.presentation.login.LoginContract
import com.example.kotlinquizzes.feature.presentation.login.LoginViewModel
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
    private lateinit var googleAuthClient: GoogleAuthClient

    @Mock
    private lateinit var application: Application
    private lateinit var loginViewmodel: LoginViewModel
    private lateinit var closeable: AutoCloseable
    private val testDispatcher = StandardTestDispatcher()
    private val fakeWebClientId = "fake-web-client-id"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        closeable = MockitoAnnotations.openMocks(this)
        whenever(application.getString(R.string.web_client_id)).thenReturn(fakeWebClientId)
        loginViewmodel = LoginViewModel(googleAuthClient, application)
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
        whenever(googleAuthClient.signIn(fakeWebClientId)).thenReturn(SignInResult.Cancelled)

        loginViewmodel.onAction(LoginContract.LoginAction.GoogleSignInClicked)

        advanceUntilIdle()
        assertFalse(loginViewmodel.state.value.isLoading)
    }

    @Test
    fun testGoogleSignIn_Clicked_CallsSignInWithWebClientId() = runTest {
        whenever(googleAuthClient.signIn(fakeWebClientId)).thenReturn(SignInResult.Cancelled)

        loginViewmodel.onAction(LoginContract.LoginAction.GoogleSignInClicked)

        advanceUntilIdle()
        verify(googleAuthClient).signIn(fakeWebClientId)
    }

    @Test
    fun testGoogleSignIn_WhenSuccess_EmitsNavigateToHomeEffect() = runTest {
        whenever(googleAuthClient.signIn(fakeWebClientId)).thenReturn(
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
        whenever(googleAuthClient.signIn(fakeWebClientId)).thenReturn(SignInResult.Cancelled)
        val effects = mutableListOf<LoginContract.LoginEffect>()
        val job = launch { loginViewmodel.effect.collect { effects.add(it) } }
        loginViewmodel.onAction(LoginContract.LoginAction.GoogleSignInClicked)

        advanceUntilIdle()
        assertFalse(effects.contains(LoginContract.LoginEffect.NavigateToHome))
        job.cancel()
    }

    @Test
    fun testGoogleSignIn_WhenFailure_DoesNotEmitNavigateToHome() = runTest {
        whenever(googleAuthClient.signIn(fakeWebClientId)).thenReturn(
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