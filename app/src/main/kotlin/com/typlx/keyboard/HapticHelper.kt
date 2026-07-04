package com.typlx.keyboard

import android.media.AudioManager
import android.view.HapticFeedbackConstants
import android.view.View

class HapticHelper(
    private val audioManager: AudioManager?,
    private val isHapticEnabled: () -> Boolean,
    private val isSoundEnabled: () -> Boolean = { false },
) {

    fun tap(view: View?) {
        if (view != null && isHapticEnabled()) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        if (isSoundEnabled()) {
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK)
        }
    }
}
