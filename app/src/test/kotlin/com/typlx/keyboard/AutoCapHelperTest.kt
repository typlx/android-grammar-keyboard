package com.typlx.keyboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoCapHelperTest {

    // --- shouldShiftAfterSpace ---

    @Test
    fun `shouldShiftAfterSpace - period space triggers shift`() =
        assertTrue(shouldShiftAfterSpace("hello. "))

    @Test
    fun `shouldShiftAfterSpace - exclamation space triggers shift`() =
        assertTrue(shouldShiftAfterSpace("wow! "))

    @Test
    fun `shouldShiftAfterSpace - question space triggers shift`() =
        assertTrue(shouldShiftAfterSpace("really? "))

    @Test
    fun `shouldShiftAfterSpace - only the last two chars matter`() =
        assertTrue(shouldShiftAfterSpace("a! b? c. "))

    @Test
    fun `shouldShiftAfterSpace - comma space does not trigger shift`() =
        assertFalse(shouldShiftAfterSpace("hello, "))

    @Test
    fun `shouldShiftAfterSpace - plain word space does not trigger shift`() =
        assertFalse(shouldShiftAfterSpace("hello "))

    @Test
    fun `shouldShiftAfterSpace - empty string does not trigger shift`() =
        assertFalse(shouldShiftAfterSpace(""))

    @Test
    fun `shouldShiftAfterSpace - period without trailing space does not trigger`() =
        assertFalse(shouldShiftAfterSpace("hello."))

    @Test
    fun `shouldShiftAfterSpace - double space after period does not trigger`() =
        assertFalse(shouldShiftAfterSpace("hello.  "))

    @Test
    fun `shouldShiftAfterSpace - single space only does not trigger`() =
        assertFalse(shouldShiftAfterSpace(" "))
}
