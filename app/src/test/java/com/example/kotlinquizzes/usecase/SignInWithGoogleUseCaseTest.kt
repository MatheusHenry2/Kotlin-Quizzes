package com.example.kotlinquizzes.usecase

import android.content.Context
import com.example.kotlinquizzes.R
import com.example.kotlinquizzes.feature.auth.data.client.GoogleAuthClient
import com.example.kotlinquizzes.feature.auth.data.model.SignInResult
import com.example.kotlinquizzes.feature.auth.data.model.UserData
import com.example.kotlinquizzes.feature.auth.domain.usecase.SignInWithGoogleUseCase
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SignInWithGoogleUseCaseTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var googleAuthClient: GoogleAuthClient

    private lateinit var signInWithGoogleUseCase: SignInWithGoogleUseCase
    private lateinit var closeable: AutoCloseable
    private val testDispatcher = StandardTestDispatcher()
    private val webClientId = "fake-web-client-id"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        closeable = MockitoAnnotations.openMocks(this)
        whenever(context.getString(R.string.web_client_id)).thenReturn(webClientId)
        signInWithGoogleUseCase = SignInWithGoogleUseCase(context, googleAuthClient)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        closeable.close()
    }

    @Test
    fun testInvoke_DelegatesToClientWithResolvedWebClientId() = runTest {
        whenever(googleAuthClient.signIn(webClientId)).thenReturn(SignInResult.Cancelled)

        signInWithGoogleUseCase()

        verify(context).getString(R.string.web_client_id)
        verify(googleAuthClient).signIn(webClientId)
    }

    @Test
    fun testInvoke_WhenClientReturnsSuccess_ReturnsSameSuccess() = runTest {
        val expected = SignInResult.Success(UserData("uid", "name", "a@b.com", null))
        whenever(googleAuthClient.signIn(webClientId)).thenReturn(expected)

        val result = signInWithGoogleUseCase()

        assertEquals(expected, result)
    }

    @Test
    fun testInvoke_WhenClientReturnsCancelled_ReturnsCancelled() = runTest {
        whenever(googleAuthClient.signIn(webClientId)).thenReturn(SignInResult.Cancelled)

        val result = signInWithGoogleUseCase()

        assertEquals(SignInResult.Cancelled, result)
    }

    @Test
    fun testInvoke_WhenClientReturnsFailure_ReturnsFailure() = runTest {
        val error = RuntimeException("boom")
        whenever(googleAuthClient.signIn(webClientId)).thenReturn(SignInResult.Failure(error))

        val result = signInWithGoogleUseCase()

        assertTrue(result is SignInResult.Failure)
        assertEquals(error, (result as SignInResult.Failure).error)
    }
}
