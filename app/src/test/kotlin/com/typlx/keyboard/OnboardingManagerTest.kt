package com.typlx.keyboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingManagerTest {

    @Test
    fun `defaultImeMatchesPackage returns false for null`() {
        assertFalse(defaultImeMatchesPackage(null, "com.typlx.keyboard"))
    }

    @Test
    fun `defaultImeMatchesPackage returns false for blank`() {
        assertFalse(defaultImeMatchesPackage("", "com.typlx.keyboard"))
        assertFalse(defaultImeMatchesPackage("   ", "com.typlx.keyboard"))
    }

    @Test
    fun `defaultImeMatchesPackage returns true for exact package with service`() {
        assertTrue(
            defaultImeMatchesPackage(
                "com.typlx.keyboard/.GrammarKeyboardService",
                "com.typlx.keyboard",
            )
        )
    }

    @Test
    fun `defaultImeMatchesPackage returns false for different package`() {
        assertFalse(
            defaultImeMatchesPackage(
                "com.gboard.ime/.GboardService",
                "com.typlx.keyboard",
            )
        )
    }

    @Test
    fun `defaultImeMatchesPackage returns false for package that is a prefix but not the same`() {
        // "com.typlx.keyboardpro" should NOT match "com.typlx.keyboard"
        assertFalse(
            defaultImeMatchesPackage(
                "com.typlx.keyboardpro/.Service",
                "com.typlx.keyboard",
            )
        )
    }

    @Test
    fun `defaultImeMatchesPackage handles package without service component`() {
        // Some OEMs may write just the package — but our check requires "/"
        assertFalse(
            defaultImeMatchesPackage(
                "com.typlx.keyboard",
                "com.typlx.keyboard",
            )
        )
    }

    @Test
    fun `defaultImeMatchesPackage is case-sensitive`() {
        assertFalse(
            defaultImeMatchesPackage(
                "COM.TYPLX.KEYBOARD/.GrammarKeyboardService",
                "com.typlx.keyboard",
            )
        )
    }
}
