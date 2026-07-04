package com.typlx.keyboard

import android.media.AudioManager
import android.view.HapticFeedbackConstants
import android.view.View
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class HapticHelperTest {

    private fun helper(
        haptic: Boolean = false,
        sound: Boolean = false,
        audioManager: AudioManager? = null,
    ) = HapticHelper(
        audioManager = audioManager,
        isHapticEnabled = { haptic },
        isSoundEnabled = { sound },
    )

    @Test
    fun `tap does nothing when disabled`() {
        val h = helper(haptic = false)
        val view = mock(View::class.java)
        h.tap(view)
        verify(view, never()).performHapticFeedback(anyInt())
    }

    @Test
    fun `tap performs KEYBOARD_TAP when enabled and view not null`() {
        val h = helper(haptic = true)
        val view = mock(View::class.java)
        h.tap(view)
        verify(view).performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    @Test
    fun `tap does not throw when view is null`() {
        val h = helper(haptic = true)
        h.tap(null)
    }

    @Test
    fun `tap does not throw when disabled and view is null`() {
        val h = helper(haptic = false)
        h.tap(null)
    }

    @Test
    fun `enabled state is re-read on each tap call`() {
        var haptic = false
        val h = HapticHelper(
            audioManager = null,
            isHapticEnabled = { haptic },
            isSoundEnabled = { false },
        )
        val view = mock(View::class.java)

        h.tap(view)
        haptic = true
        h.tap(view)

        verify(view, times(1)).performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    @Test
    fun `multiple taps when enabled all fire feedback`() {
        val h = helper(haptic = true)
        val view = mock(View::class.java)
        repeat(3) { h.tap(view) }
        verify(view, times(3)).performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    @Test
    fun `sound plays when sound enabled and audioManager not null`() {
        val audioManager = mock(AudioManager::class.java)
        val h = helper(sound = true, audioManager = audioManager)
        h.tap(null)
        verify(audioManager).playSoundEffect(AudioManager.FX_KEY_CLICK)
    }

    @Test
    fun `sound does not play when sound disabled`() {
        val audioManager = mock(AudioManager::class.java)
        val h = helper(sound = false, audioManager = audioManager)
        h.tap(null)
        verify(audioManager, never()).playSoundEffect(anyInt())
    }

    @Test
    fun `sound does not play when audioManager is null`() {
        val h = helper(sound = true, audioManager = null)
        h.tap(null) // should not throw
    }
}
