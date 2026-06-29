package com.typlx.keyboard

import android.text.InputType
import org.junit.Assert.*
import org.junit.Test

class NumPadKeyTypeTest {

    @Test
    fun `phone field triggers numpad in phone mode`() {
        val cfg = numPadConfig(InputType.TYPE_CLASS_PHONE)
        assertTrue(cfg.isNumPad)
        assertTrue(cfg.isPhoneMode)
        assertFalse(cfg.isDecimalAllowed)
        assertFalse(cfg.isSignedAllowed)
    }

    @Test
    fun `plain number field triggers numpad without phone mode`() {
        val cfg = numPadConfig(InputType.TYPE_CLASS_NUMBER)
        assertTrue(cfg.isNumPad)
        assertFalse(cfg.isPhoneMode)
        assertFalse(cfg.isDecimalAllowed)
        assertFalse(cfg.isSignedAllowed)
    }

    @Test
    fun `decimal number field enables decimal key`() {
        val inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        val cfg = numPadConfig(inputType)
        assertTrue(cfg.isNumPad)
        assertFalse(cfg.isPhoneMode)
        assertTrue(cfg.isDecimalAllowed)
        assertFalse(cfg.isSignedAllowed)
    }

    @Test
    fun `signed number field enables signed key`() {
        val inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        val cfg = numPadConfig(inputType)
        assertTrue(cfg.isNumPad)
        assertFalse(cfg.isPhoneMode)
        assertFalse(cfg.isDecimalAllowed)
        assertTrue(cfg.isSignedAllowed)
    }

    @Test
    fun `signed decimal number field enables both extra keys`() {
        val inputType = InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL or
                InputType.TYPE_NUMBER_FLAG_SIGNED
        val cfg = numPadConfig(inputType)
        assertTrue(cfg.isNumPad)
        assertTrue(cfg.isDecimalAllowed)
        assertTrue(cfg.isSignedAllowed)
    }

    @Test
    fun `text field does not trigger numpad`() {
        val cfg = numPadConfig(InputType.TYPE_CLASS_TEXT)
        assertFalse(cfg.isNumPad)
        assertFalse(cfg.isPhoneMode)
    }

    @Test
    fun `email field does not trigger numpad`() {
        val inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        val cfg = numPadConfig(inputType)
        assertFalse(cfg.isNumPad)
    }

    @Test
    fun `password field does not trigger numpad`() {
        val inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        val cfg = numPadConfig(inputType)
        assertFalse(cfg.isNumPad)
    }

    @Test
    fun `numeric password field triggers numpad`() {
        val inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        val cfg = numPadConfig(inputType)
        assertTrue(cfg.isNumPad)
        assertFalse(cfg.isPhoneMode)
    }

    @Test
    fun `zero inputType does not trigger numpad`() {
        val cfg = numPadConfig(0)
        assertFalse(cfg.isNumPad)
        assertFalse(cfg.isPhoneMode)
        assertFalse(cfg.isDecimalAllowed)
        assertFalse(cfg.isSignedAllowed)
    }
}
