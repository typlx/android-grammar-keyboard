package com.typlx.keyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class FeaturePreferencesTest {

    @Test
    fun `KEY_HAPTIC has stable persistence key`() {
        assertEquals("haptic_feedback_enabled", FeaturePreferences.KEY_HAPTIC)
    }

    @Test
    fun `KEY_AUTO_SUGGEST has stable persistence key`() {
        assertEquals("auto_suggest_enabled", FeaturePreferences.KEY_AUTO_SUGGEST)
    }

    @Test
    fun `KEY_CRASH_REPORTING has stable persistence key`() {
        assertEquals("crash_reporting_enabled", FeaturePreferences.KEY_CRASH_REPORTING)
    }

    @Test
    fun `KEY_DOUBLE_SPACE_PERIOD has stable persistence key`() {
        assertEquals("double_space_period_enabled", FeaturePreferences.KEY_DOUBLE_SPACE_PERIOD)
    }

    @Test
    fun `KEY_AUTO_CAP has stable persistence key`() {
        assertEquals("auto_cap_enabled", FeaturePreferences.KEY_AUTO_CAP)
    }

    @Test
    fun `all FeaturePreferences keys are unique`() {
        val keys = listOf(
            FeaturePreferences.KEY_HAPTIC,
            FeaturePreferences.KEY_AUTO_SUGGEST,
            FeaturePreferences.KEY_CRASH_REPORTING,
            FeaturePreferences.KEY_DOUBLE_SPACE_PERIOD,
            FeaturePreferences.KEY_AUTO_CAP,
        )
        assertEquals("Duplicate key in FeaturePreferences", keys.size, keys.distinct().size)
    }

    @Test
    fun `FeaturePreferences keys are disjoint from UiPreferences keys`() {
        val featureKeys = setOf(
            FeaturePreferences.KEY_HAPTIC,
            FeaturePreferences.KEY_AUTO_SUGGEST,
            FeaturePreferences.KEY_CRASH_REPORTING,
            FeaturePreferences.KEY_DOUBLE_SPACE_PERIOD,
            FeaturePreferences.KEY_AUTO_CAP,
        )
        val uiKeys = setOf(
            UiPreferences.KEY_THEME_PRESET,
            UiPreferences.KEY_CORNER_RADIUS_DP,
            UiPreferences.KEY_KEY_ALPHA_PERCENT,
            UiPreferences.KEY_KEYBOARD_LAYOUT,
            UiPreferences.KEY_KEY_SIZE_PRESET,
            UiPreferences.KEY_SHOW_NUMBER_ROW,
        )
        val collision = featureKeys.intersect(uiKeys)
        assertEquals("FeaturePreferences and UiPreferences share a key: $collision", 0, collision.size)
    }
}
