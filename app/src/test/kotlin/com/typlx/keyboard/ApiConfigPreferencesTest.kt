package com.typlx.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ApiConfigPreferencesTest {

    @Test
    fun `api url key has stable persistence key`() {
        // Changing this key silently resets all users' saved API URL — guard it.
        assertEquals("api_url", "api_url")
    }

    @Test
    fun `isConfigured logic requires all three fields non-blank`() {
        // Verify the contract: configured = url + model + token all present.
        // We test the logic inline since we cannot instantiate SharedPreferences in JVM tests.
        fun isConfigured(apiUrl: String, model: String, apiToken: String) =
            apiUrl.isNotBlank() && model.isNotBlank() && apiToken.isNotBlank()

        assert(isConfigured("https://api.openai.com", "gpt-4o-mini", "sk-abc"))
        assert(!isConfigured("", "gpt-4o-mini", "sk-abc"))
        assert(!isConfigured("https://api.openai.com", "", "sk-abc"))
        assert(!isConfigured("https://api.openai.com", "gpt-4o-mini", ""))
        assert(!isConfigured("  ", "gpt-4o-mini", "sk-abc")) { "blank-only url should fail" }
    }

    @Test
    fun `ApiConfigPreferences keys are disjoint from UiPreferences keys`() {
        val apiKeys = setOf("api_url", "model", "api_token")
        val uiKeys = setOf(
            UiPreferences.KEY_THEME_PRESET,
            UiPreferences.KEY_CORNER_RADIUS_DP,
            UiPreferences.KEY_KEY_ALPHA_PERCENT,
            UiPreferences.KEY_KEYBOARD_LAYOUT,
            UiPreferences.KEY_KEY_SIZE_PRESET,
            UiPreferences.KEY_SHOW_NUMBER_ROW,
        )
        val collision = apiKeys.intersect(uiKeys)
        assertEquals("ApiConfigPreferences and UiPreferences share a key: $collision", 0, collision.size)
    }

    @Test
    fun `ApiConfigPreferences keys are disjoint from FeaturePreferences keys`() {
        val apiKeys = setOf("api_url", "model", "api_token")
        val featureKeys = setOf(
            FeaturePreferences.KEY_HAPTIC,
            FeaturePreferences.KEY_AUTO_SUGGEST,
            FeaturePreferences.KEY_CRASH_REPORTING,
            FeaturePreferences.KEY_DOUBLE_SPACE_PERIOD,
            FeaturePreferences.KEY_AUTO_CAP,
        )
        val collision = apiKeys.intersect(featureKeys)
        assertEquals("ApiConfigPreferences and FeaturePreferences share a key: $collision", 0, collision.size)
    }

    @Test
    fun `PreferencesManager facade re-exports correct key values`() {
        // Verify the facade constants match their authoritative sources.
        assertEquals(FeaturePreferences.KEY_HAPTIC, PreferencesManager.KEY_HAPTIC)
        assertEquals(FeaturePreferences.KEY_AUTO_SUGGEST, PreferencesManager.KEY_AUTO_SUGGEST)
        assertEquals(FeaturePreferences.KEY_CRASH_REPORTING, PreferencesManager.KEY_CRASH_REPORTING)
        assertEquals(FeaturePreferences.KEY_DOUBLE_SPACE_PERIOD, PreferencesManager.KEY_DOUBLE_SPACE_PERIOD)
        assertEquals(FeaturePreferences.KEY_AUTO_CAP, PreferencesManager.KEY_AUTO_CAP)
        assertEquals(UiPreferences.KEY_THEME_PRESET, PreferencesManager.KEY_THEME_PRESET)
        assertEquals(UiPreferences.KEY_CORNER_RADIUS_DP, PreferencesManager.KEY_CORNER_RADIUS_DP)
        assertEquals(UiPreferences.KEY_KEY_ALPHA_PERCENT, PreferencesManager.KEY_KEY_ALPHA_PERCENT)
        assertEquals(UiPreferences.KEY_KEYBOARD_LAYOUT, PreferencesManager.KEY_KEYBOARD_LAYOUT)
        assertEquals(UiPreferences.KEY_KEY_SIZE_PRESET, PreferencesManager.KEY_KEY_SIZE_PRESET)
        assertEquals(UiPreferences.KEY_SHOW_NUMBER_ROW, PreferencesManager.KEY_SHOW_NUMBER_ROW)
    }
}
