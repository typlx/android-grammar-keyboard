package com.typlx.keyboard

import android.content.Context
import android.content.SharedPreferences

/** Stores behavioral feature toggles (haptic, suggestions, typing helpers, analytics). */
class FeaturePreferences(context: Context) {

    companion object {
        private const val PREFS_NAME = "typlx_keyboard_prefs"

        const val KEY_HAPTIC = "haptic_feedback_enabled"
        const val KEY_AUTO_SUGGEST = "auto_suggest_enabled"
        const val KEY_CRASH_REPORTING = "crash_reporting_enabled"
        const val KEY_DOUBLE_SPACE_PERIOD = "double_space_period_enabled"
        const val KEY_AUTO_CAP = "auto_cap_enabled"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var hapticFeedbackEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC, value).apply()

    var autoSuggestEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SUGGEST, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SUGGEST, value).apply()

    var crashReportingEnabled: Boolean
        get() = prefs.getBoolean(KEY_CRASH_REPORTING, true)
        set(value) = prefs.edit().putBoolean(KEY_CRASH_REPORTING, value).apply()

    var doubleSpacePeriodEnabled: Boolean
        get() = prefs.getBoolean(KEY_DOUBLE_SPACE_PERIOD, true)
        set(value) = prefs.edit().putBoolean(KEY_DOUBLE_SPACE_PERIOD, value).apply()

    var autoCapEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_CAP, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_CAP, value).apply()
}
