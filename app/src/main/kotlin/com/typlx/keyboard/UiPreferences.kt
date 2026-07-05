package com.typlx.keyboard

import android.content.Context
import android.content.SharedPreferences
import com.typlx.keyboard.ui.theme.ThemePreset

/** Stores visual and layout preferences for the keyboard UI. */
class UiPreferences(context: Context) {

    companion object {
        private const val PREFS_NAME = "typlx_keyboard_prefs"

        const val KEY_THEME_PRESET = "theme_preset"
        const val KEY_CORNER_RADIUS_DP = "corner_radius_dp"
        const val KEY_KEY_ALPHA_PERCENT = "key_alpha_percent"
        const val KEY_KEYBOARD_LAYOUT = "keyboard_layout"
        const val KEY_KEY_SIZE_PRESET = "key_size_preset"
        const val KEY_SHOW_NUMBER_ROW = "show_number_row"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var themePreset: ThemePreset
        get() = try {
            ThemePreset.valueOf(prefs.getString(KEY_THEME_PRESET, "SYSTEM") ?: "SYSTEM")
        } catch (_: IllegalArgumentException) {
            ThemePreset.SYSTEM
        }
        set(value) = prefs.edit().putString(KEY_THEME_PRESET, value.name).apply()

    var cornerRadiusDp: Int
        get() = prefs.getInt(KEY_CORNER_RADIUS_DP, 6).coerceIn(0, 16)
        set(value) = prefs.edit().putInt(KEY_CORNER_RADIUS_DP, value.coerceIn(0, 16)).apply()

    var keyAlphaPercent: Int
        get() = prefs.getInt(KEY_KEY_ALPHA_PERCENT, 100).coerceIn(0, 100)
        set(value) = prefs.edit().putInt(KEY_KEY_ALPHA_PERCENT, value.coerceIn(0, 100)).apply()

    var keyboardLayoutId: LayoutId
        get() = try {
            LayoutId.valueOf(prefs.getString(KEY_KEYBOARD_LAYOUT, "QWERTY") ?: "QWERTY")
        } catch (_: IllegalArgumentException) {
            LayoutId.QWERTY
        }
        set(value) = prefs.edit().putString(KEY_KEYBOARD_LAYOUT, value.name).apply()

    var keySizePreset: KeySizePreset
        get() = try {
            KeySizePreset.valueOf(prefs.getString(KEY_KEY_SIZE_PRESET, "NORMAL") ?: "NORMAL")
        } catch (_: IllegalArgumentException) {
            KeySizePreset.NORMAL
        }
        set(value) = prefs.edit().putString(KEY_KEY_SIZE_PRESET, value.name).apply()

    var showNumberRow: Boolean
        get() = prefs.getBoolean(KEY_SHOW_NUMBER_ROW, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_NUMBER_ROW, value).apply()
}
