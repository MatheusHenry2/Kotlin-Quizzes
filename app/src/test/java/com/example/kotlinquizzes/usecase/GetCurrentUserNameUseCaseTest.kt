package com.example.kotlinquizzes.usecase

import com.example.kotlinquizzes.feature.auth.domain.usecase.GetCurrentUserNameUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GetCurrentUserNameUseCaseTest {

    @Mock
    private lateinit var firebaseAuth: FirebaseAuth

    @Mock
    private lateinit var firebaseUser: FirebaseUser

    private lateinit var getCurrentUserNameUseCase: GetCurrentUserNameUseCase
    private lateinit var closeable: AutoCloseable

    @Before
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
        getCurrentUserNameUseCase = GetCurrentUserNameUseCase(firebaseAuth)
    }

    @After
    fun tearDown() {
        closeable.close()
    }

    @Test
    fun testInvoke_WhenCurrentUserIsNull_ReturnsFallbackUser() {
        whenever(firebaseAuth.currentUser).thenReturn(null)

        assertEquals("User", getCurrentUserNameUseCase())
    }

    @Test
    fun testInvoke_WhenDisplayNameIsNull_ReturnsFallbackUser() {
        whenever(firebaseAuth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.displayName).thenReturn(null)

        assertEquals("User", getCurrentUserNameUseCase())
    }

    @Test
    fun testInvoke_WhenDisplayNameIsBlank_ReturnsFallbackUser() {
        whenever(firebaseAuth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.displayName).thenReturn("   ")

        assertEquals("User", getCurrentUserNameUseCase())
    }

    @Test
    fun testInvoke_WhenDisplayNameHasWhitespace_ReturnsTrimmedName() {
        whenever(firebaseAuth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.displayName).thenReturn("  Matheus  ")

        assertEquals("Matheus", getCurrentUserNameUseCase())
    }

    @Test
    fun testInvoke_WhenDisplayNameIsValid_ReturnsIt() {
        whenever(firebaseAuth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.displayName).thenReturn("Matheus")

        assertEquals("Matheus", getCurrentUserNameUseCase())
    }
}
