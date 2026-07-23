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
        const val KEY_KEYBOARD_LAYOUT = "keyboard_layout"
        const val KEY_KEY_SIZE_PRESET = "key_size_preset"
        const val KEY_KEY_HEIGHT_DP = "key_height_dp"
        const val KEY_SHOW_NUMBER_ROW = "show_number_row"
        const val KEY_CRASH_REPORTING = "crash_reporting_enabled"
        const val KEY_DOUBLE_SPACE_PERIOD = "double_space_period_enabled"
        const val KEY_AUTO_CAP = "auto_cap_enabled"
        const val KEY_AUTOCORRECT = "autocorrect_enabled"
        const val KEY_GRAMMAR_INSTRUCTION_SUFFIX = "grammar_instruction_suffix"
        const val KEY_WORD_PREDICTION = "word_prediction_enabled"
        const val KEY_SMART_COMPOSE = "smart_compose_enabled"
        const val KEY_EMOJI_SUGGESTIONS = "emoji_suggestions_enabled"
        const val KEY_KEY_PRESS_PREVIEW = "key_press_preview_enabled"
        const val KEY_SWIPE_TYPING = "swipe_typing_enabled"
        const val KEY_LANDSCAPE_SPLIT = "landscape_split_enabled"
        const val KEY_ONE_HANDED_MODE = "one_handed_mode"
        const val KEY_CUSTOM_KEY_BG = "custom_key_bg_color"
        const val KEY_CUSTOM_KEY_TEXT = "custom_key_text_color"
        const val KEY_CUSTOM_ACCENT = "custom_accent_color"

        private const val DEFAULT_API_URL = "https://api.openai.com"
        private const val DEFAULT_MODEL = "gpt-4o-mini"

        const val KEY_HEIGHT_DP_MIN = 36
        const val KEY_HEIGHT_DP_MAX = 64
        const val KEY_HEIGHT_DP_DEFAULT = 46
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

    var keyHeightDp: Int
        get() = prefs.getInt(KEY_KEY_HEIGHT_DP, KEY_HEIGHT_DP_DEFAULT).coerceIn(KEY_HEIGHT_DP_MIN, KEY_HEIGHT_DP_MAX)
        set(value) = prefs.edit().putInt(KEY_KEY_HEIGHT_DP, value.coerceIn(KEY_HEIGHT_DP_MIN, KEY_HEIGHT_DP_MAX)).apply()

    var showNumberRow: Boolean
        get() = prefs.getBoolean(KEY_SHOW_NUMBER_ROW, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_NUMBER_ROW, value).apply()

    var crashReportingEnabled: Boolean
        get() = prefs.getBoolean(KEY_CRASH_REPORTING, true)
        set(value) = prefs.edit().putBoolean(KEY_CRASH_REPORTING, value).apply()

    var doubleSpacePeriodEnabled: Boolean
        get() = prefs.getBoolean(KEY_DOUBLE_SPACE_PERIOD, true)
        set(value) = prefs.edit().putBoolean(KEY_DOUBLE_SPACE_PERIOD, value).apply()

    var autoCapEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_CAP, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_CAP, value).apply()

    var autocorrectEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTOCORRECT, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTOCORRECT, value).apply()

    var grammarInstructionSuffix: String
        get() = prefs.getString(KEY_GRAMMAR_INSTRUCTION_SUFFIX, "") ?: ""
        set(value) = prefs.edit().putString(KEY_GRAMMAR_INSTRUCTION_SUFFIX, value).apply()

    var wordPredictionEnabled: Boolean
        get() = prefs.getBoolean(KEY_WORD_PREDICTION, true)
        set(value) = prefs.edit().putBoolean(KEY_WORD_PREDICTION, value).apply()

    var smartComposeEnabled: Boolean
        get() = prefs.getBoolean(KEY_SMART_COMPOSE, true)
        set(value) = prefs.edit().putBoolean(KEY_SMART_COMPOSE, value).apply()

    var emojiSuggestionsEnabled: Boolean
        get() = prefs.getBoolean(KEY_EMOJI_SUGGESTIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_EMOJI_SUGGESTIONS, value).apply()

    var keyPressPreviewEnabled: Boolean
        get() = prefs.getBoolean(KEY_KEY_PRESS_PREVIEW, true)
        set(value) = prefs.edit().putBoolean(KEY_KEY_PRESS_PREVIEW, value).apply()

    var swipeTypingEnabled: Boolean
        get() = prefs.getBoolean(KEY_SWIPE_TYPING, true)
        set(value) = prefs.edit().putBoolean(KEY_SWIPE_TYPING, value).apply()

    var landscapeSplitEnabled: Boolean
        get() = prefs.getBoolean(KEY_LANDSCAPE_SPLIT, true)
        set(value) = prefs.edit().putBoolean(KEY_LANDSCAPE_SPLIT, value).apply()

    var oneHandedMode: OneHandedMode
        get() = try {
            OneHandedMode.valueOf(prefs.getString(KEY_ONE_HANDED_MODE, "OFF") ?: "OFF")
        } catch (_: IllegalArgumentException) {
            OneHandedMode.OFF
        }
        set(value) = prefs.edit().putString(KEY_ONE_HANDED_MODE, value.name).apply()

    // Stored as decimal string of the ARGB Int; null means "use preset default".
    var customKeyBgColor: Int?
        get() = prefs.getString(KEY_CUSTOM_KEY_BG, null)?.toIntOrNull()
        set(value) {
            if (value == null) prefs.edit().remove(KEY_CUSTOM_KEY_BG).apply()
            else prefs.edit().putString(KEY_CUSTOM_KEY_BG, value.toString()).apply()
        }

    var customKeyTextColor: Int?
        get() = prefs.getString(KEY_CUSTOM_KEY_TEXT, null)?.toIntOrNull()
        set(value) {
            if (value == null) prefs.edit().remove(KEY_CUSTOM_KEY_TEXT).apply()
            else prefs.edit().putString(KEY_CUSTOM_KEY_TEXT, value.toString()).apply()
        }

    var customAccentColor: Int?
        get() = prefs.getString(KEY_CUSTOM_ACCENT, null)?.toIntOrNull()
        set(value) {
            if (value == null) prefs.edit().remove(KEY_CUSTOM_ACCENT).apply()
            else prefs.edit().putString(KEY_CUSTOM_ACCENT, value.toString()).apply()
        }

    val isConfigured: Boolean
        get() = apiUrl.isNotBlank() && model.isNotBlank() && apiToken.isNotBlank()
}
