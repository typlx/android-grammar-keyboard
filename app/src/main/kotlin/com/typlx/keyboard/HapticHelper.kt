package com.typlx.keyboard

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Wraps haptic feedback logic for keyboard key taps.
 *
 * Primary path: [View.performHapticFeedback] with [HapticFeedbackConstants.KEYBOARD_TAP].
 * Fallback: [Vibrator] with [VibrationEffect.createOneShot] if the view haptic returns false.
 *
 * All vibration calls are guarded with try/catch so a missing VIBRATE permission never crashes.
 *
 * @param isEnabled supplier checked on every tap; returns false when the user disabled haptics
 * @param vibrate fallback action called with duration ms when view haptic is unavailable
 * @param performViewHaptic optional override injected in tests; production code uses [attachView]
 */
class HapticHelper(
    private val isEnabled: () -> Boolean,
    private val vibrate: (durationMs: Long) -> Unit,
    private var performViewHaptic: (() -> Boolean)? = null,
) {

    companion object {
        fun create(context: Context, prefs: PreferencesManager): HapticHelper {
            val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(VibratorManager::class.java)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Vibrator::class.java)
            }
            return HapticHelper(
                isEnabled = { prefs.hapticFeedbackEnabled },
                vibrate = { durationMs ->
                    try {
                        vibrator?.vibrate(
                            VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                        )
                    } catch (_: Exception) {}
                },
            )
        }
    }

    fun attachView(view: View) {
        performViewHaptic = { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
    }

    fun tapRegularKey() = tap(10L)

    fun tapSpecialKey() = tap(20L)

    private fun tap(durationMs: Long) {
        if (!isEnabled()) return
        if (performViewHaptic?.invoke() == true) return
        vibrate(durationMs)
    }
}
