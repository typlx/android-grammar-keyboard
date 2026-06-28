package com.typlx.keyboard

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

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

    val isConfigured: Boolean
        get() = apiUrl.isNotBlank() && model.isNotBlank() && apiToken.isNotBlank()
}
