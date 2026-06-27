package com.typlx.keyboard

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class StubAuthRepositoryTest {

    private val repo = StubAuthRepository()

    @Test
    fun `currentUser returns null`() = runTest {
        assertNull(repo.currentUser())
    }

    @Test
    fun `isSignedIn returns false`() = runTest {
        assertFalse(repo.isSignedIn())
    }

    @Test
    fun `signIn returns Error result`() = runTest {
        val result = repo.signIn("user@example.com", "password")
        assertTrue("signIn should return Error, got $result", result is AuthResult.Error)
    }

    @Test
    fun `signIn error message is non-empty`() = runTest {
        val result = repo.signIn("user@example.com", "password") as AuthResult.Error
        assertTrue("error message should not be blank", result.message.isNotBlank())
    }

    @Test
    fun `signUp returns Error result`() = runTest {
        val result = repo.signUp("user@example.com", "password")
        assertTrue("signUp should return Error, got $result", result is AuthResult.Error)
    }

    @Test
    fun `signUp error message is non-empty`() = runTest {
        val result = repo.signUp("user@example.com", "password") as AuthResult.Error
        assertTrue("error message should not be blank", result.message.isNotBlank())
    }

    @Test
    fun `signOut completes without throwing`() = runTest {
        repo.signOut()
    }
}
