package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class HapticHelperTest {

    // Creates a HapticHelper with full control over enabled state, view haptic result, and vibrate.
    private fun makeHelper(
        enabled: Boolean,
        viewHapticResult: Boolean = false,
        onVibrate: (Long) -> Unit = {},
    ) = HapticHelper(
        isEnabled = { enabled },
        vibrate = onVibrate,
        performViewHaptic = { viewHapticResult },
    )

    @Test
    fun `disabled - view haptic not called`() {
        var viewHapticCalled = false
        val helper = HapticHelper(
            isEnabled = { false },
            vibrate = {},
            performViewHaptic = { viewHapticCalled = true; true },
        )
        helper.tapRegularKey()
        assertFalse(viewHapticCalled)
    }

    @Test
    fun `disabled - vibrator not called`() {
        var vibrated = false
        val helper = makeHelper(enabled = false, onVibrate = { vibrated = true })
        helper.tapSpecialKey()
        assertFalse(vibrated)
    }

    @Test
    fun `enabled view haptic succeeds - no vibrator fallback`() {
        var vibrated = false
        val helper = makeHelper(enabled = true, viewHapticResult = true, onVibrate = { vibrated = true })
        helper.tapRegularKey()
        assertFalse(vibrated)
    }

    @Test
    fun `enabled view haptic fails - vibrator fallback used for regular key`() {
        var vibratedMs = -1L
        val helper = makeHelper(enabled = true, viewHapticResult = false, onVibrate = { vibratedMs = it })
        helper.tapRegularKey()
        assertEquals(10L, vibratedMs)
    }

    @Test
    fun `enabled view haptic fails - vibrator fallback uses longer duration for special key`() {
        var vibratedMs = -1L
        val helper = makeHelper(enabled = true, viewHapticResult = false, onVibrate = { vibratedMs = it })
        helper.tapSpecialKey()
        assertEquals(20L, vibratedMs)
    }

    @Test
    fun `enabled no view haptic performer - vibrator fallback used`() {
        var vibrated = false
        val helper = HapticHelper(
            isEnabled = { true },
            vibrate = { vibrated = true },
            performViewHaptic = null,
        )
        helper.tapRegularKey()
        assertTrue(vibrated)
    }
}
