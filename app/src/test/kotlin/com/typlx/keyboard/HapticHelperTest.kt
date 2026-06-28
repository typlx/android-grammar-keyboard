package com.typlx.keyboard

import android.view.HapticFeedbackConstants
import android.view.View
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class HapticHelperTest {

    @Test
    fun `tap does nothing when disabled`() {
        val helper = HapticHelper(isEnabled = { false })
        val view = mock(View::class.java)
        helper.tap(view)
        verify(view, never()).performHapticFeedback(anyInt())
    }

    @Test
    fun `tap performs KEYBOARD_TAP when enabled and view not null`() {
        val helper = HapticHelper(isEnabled = { true })
        val view = mock(View::class.java)
        helper.tap(view)
        verify(view).performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    @Test
    fun `tap does not throw when view is null`() {
        val helper = HapticHelper(isEnabled = { true })
        helper.tap(null)
    }

    @Test
    fun `tap does not throw when disabled and view is null`() {
        val helper = HapticHelper(isEnabled = { false })
        helper.tap(null)
    }

    @Test
    fun `enabled state is re-read on each tap call`() {
        var enabled = false
        val helper = HapticHelper(isEnabled = { enabled })
        val view = mock(View::class.java)

        helper.tap(view)
        enabled = true
        helper.tap(view)

        verify(view, times(1)).performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    @Test
    fun `multiple taps when enabled all fire feedback`() {
        val helper = HapticHelper(isEnabled = { true })
        val view = mock(View::class.java)
        repeat(3) { helper.tap(view) }
        verify(view, times(3)).performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
}
