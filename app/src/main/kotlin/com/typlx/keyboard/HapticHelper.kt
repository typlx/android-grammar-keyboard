package com.typlx.keyboard

import android.view.HapticFeedbackConstants
import android.view.View

class HapticHelper(private val isEnabled: () -> Boolean) {

    fun tap(view: View?) {
        if (!isEnabled() || view == null) return
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
}
