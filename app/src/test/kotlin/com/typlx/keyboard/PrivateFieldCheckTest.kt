package com.typlx.keyboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Uses raw AOSP InputType constants (verified against android.text.InputType source):
 *   TYPE_CLASS_TEXT                   = 0x00000001
 *   TYPE_MASK_VARIATION               = 0x00000ff0
 *   TYPE_TEXT_VARIATION_URI           = 0x00000010
 *   TYPE_TEXT_VARIATION_PASSWORD      = 0x00000080
 *   TYPE_TEXT_VARIATION_VISIBLE_PASSWORD = 0x00000090
 *   TYPE_TEXT_VARIATION_WEB_PASSWORD  = 0x000000e0
 *   TYPE_TEXT_VARIATION_EMAIL_ADDRESS = 0x00000020
 *   TYPE_CLASS_NUMBER                 = 0x00000002
 */
class PrivateFieldCheckTest {

    private fun textClass(variation: Int) = 0x00000001 or variation

    @Test
    fun `password variation is private`() {
        assertTrue(isPrivateInputType(textClass(0x00000080)))
    }

    @Test
    fun `visible password variation is private`() {
        assertTrue(isPrivateInputType(textClass(0x00000090)))
    }

    @Test
    fun `web password variation is private`() {
        assertTrue(isPrivateInputType(textClass(0x000000e0)))
    }

    @Test
    fun `URI variation is private`() {
        assertTrue(isPrivateInputType(textClass(0x00000010)))
    }

    @Test
    fun `normal text field is not private`() {
        assertFalse(isPrivateInputType(0x00000001))
    }

    @Test
    fun `email address variation is not private`() {
        assertFalse(isPrivateInputType(textClass(0x00000020)))
    }

    @Test
    fun `long message variation is not private`() {
        assertFalse(isPrivateInputType(textClass(0x00000050)))
    }

    @Test
    fun `zero inputType is not private`() {
        assertFalse(isPrivateInputType(0))
    }

    @Test
    fun `number class is not private`() {
        assertFalse(isPrivateInputType(0x00000002))
    }
}
