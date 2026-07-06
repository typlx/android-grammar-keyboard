package com.typlx.keyboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Uses raw AOSP InputType constants (verified against android.text.InputType source):
 *   TYPE_NULL                              = 0x00000000
 *   TYPE_CLASS_TEXT                        = 0x00000001
 *   TYPE_CLASS_NUMBER                      = 0x00000002
 *   TYPE_CLASS_PHONE                       = 0x00000003
 *   TYPE_CLASS_DATETIME                    = 0x00000004
 *   TYPE_MASK_VARIATION                    = 0x00000ff0
 *   TYPE_TEXT_VARIATION_URI                = 0x00000010
 *   TYPE_TEXT_VARIATION_EMAIL_ADDRESS      = 0x00000020
 *   TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS  = 0x000000d0
 *   TYPE_TEXT_VARIATION_PASSWORD           = 0x00000080
 *   TYPE_TEXT_VARIATION_VISIBLE_PASSWORD   = 0x00000090
 *   TYPE_TEXT_VARIATION_WEB_PASSWORD       = 0x000000e0
 *   TYPE_TEXT_VARIATION_LONG_MESSAGE       = 0x00000050
 */
class PrivateFieldCheckTest {

    private fun textClass(variation: Int) = 0x00000001 or variation

    // --- password / URI (existing behaviour) ---

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

    // --- email address fields (PII — must not be sent to grammar API) ---

    @Test
    fun `email address variation is private`() {
        assertTrue(isPrivateInputType(textClass(0x00000020)))
    }

    @Test
    fun `web email address variation is private`() {
        assertTrue(isPrivateInputType(textClass(0x000000d0)))
    }

    // --- non-text input classes (phone / number / datetime) ---

    @Test
    fun `number class is private`() {
        assertTrue(isPrivateInputType(0x00000002))
    }

    @Test
    fun `phone class is private`() {
        assertTrue(isPrivateInputType(0x00000003))
    }

    @Test
    fun `datetime class is private`() {
        assertTrue(isPrivateInputType(0x00000004))
    }

    // --- text fields that should continue to receive grammar correction ---

    @Test
    fun `normal text field is not private`() {
        assertFalse(isPrivateInputType(0x00000001))
    }

    @Test
    fun `long message variation is not private`() {
        assertFalse(isPrivateInputType(textClass(0x00000050)))
    }

    @Test
    fun `TYPE_NULL is not private`() {
        assertFalse(isPrivateInputType(0))
    }
}
