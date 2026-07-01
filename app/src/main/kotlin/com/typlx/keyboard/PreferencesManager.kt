package com.typlx.keyboard

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.typlx.keyboard.ui.theme.ThemePreset

/**
 * Manages application preferences with encrypted storage for sensitive data (API token)
 * and regular SharedPreferences for non-sensitive settings (API URL, model name).
 */
class PreferencesManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "typlx_keyboard_prefs"
        private const val ENCRYPTED_PREFS_NAME = "typlx_keyboard_secure_prefs"

        private const val KEY_API_URL = "api_url"
        private const val KEY_MODEL = "model"
        private const val KEY_API_TOKEN = "api_token"
        const val KEY_HAPTIC = "haptic_feedback_enabled"
        const val KEY_AUTO_SUGGEST = "auto_suggest_enabled"
        const val KEY_THEME_PRESET = "theme_preset"
        const val KEY_CORNER_RADIUS_DP = "corner_radius_dp"
        const val KEY_KEY_ALPHA_PERCENT = "key_alpha_percent"
        const val KEY_SHOW_NUMBER_ROW = "show_number_row"

        private const val DEFAULT_API_URL = "https://api.openai.com"
        private const val DEFAULT_MODEL = "gpt-4o-mini"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    var apiUrl: String
        get() = prefs.getString(KEY_API_URL, DEFAULT_API_URL) ?: DEFAULT_API_URL
        set(value) = prefs.edit().putString(KEY_API_URL, value).apply()

    var model: String
        get() = prefs.getString(KEY_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        set(value) = prefs.edit().putString(KEY_MODEL, value).apply()

    var apiToken: String
        get() = encryptedPrefs.getString(KEY_API_TOKEN, "") ?: ""
        set(value) = encryptedPrefs.edit().putString(KEY_API_TOKEN, value).apply()

    var hapticFeedbackEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC, value).apply()

    var autoSuggestEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SUGGEST, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SUGGEST, value).apply()

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

    var showNumberRow: Boolean
        get() = prefs.getBoolean(KEY_SHOW_NUMBER_ROW, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_NUMBER_ROW, value).apply()

    val isConfigured: Boolean
        get() = apiUrl.isNotBlank() && model.isNotBlank() && apiToken.isNotBlank()
}
