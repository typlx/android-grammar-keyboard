package com.typlx.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NumberRowPreferenceTest {

    @Test
    fun `KEY_SHOW_NUMBER_ROW has stable persistence key`() {
        // If this key ever changes, existing user preferences are silently reset to the
        // default. That's a breaking change that needs a migration — fail loudly here.
        assertEquals("show_number_row", PreferencesManager.KEY_SHOW_NUMBER_ROW)
    }

    @Test
    fun `KEY_HAPTIC has stable persistence key`() {
        assertEquals("haptic_feedback_enabled", PreferencesManager.KEY_HAPTIC)
    }

    @Test
    fun `KEY_AUTO_SUGGEST has stable persistence key`() {
        assertEquals("auto_suggest_enabled", PreferencesManager.KEY_AUTO_SUGGEST)
    }

    @Test
    fun `KEY_SHOW_NUMBER_ROW is distinct from all other preference keys`() {
        val allKeys = listOf(
            PreferencesManager.KEY_HAPTIC,
            PreferencesManager.KEY_AUTO_SUGGEST,
            PreferencesManager.KEY_THEME_PRESET,
            PreferencesManager.KEY_CORNER_RADIUS_DP,
            PreferencesManager.KEY_KEY_ALPHA_PERCENT,
        )
        assertTrue(
            "KEY_SHOW_NUMBER_ROW must not collide with another preference key",
            PreferencesManager.KEY_SHOW_NUMBER_ROW !in allKeys,
        )
    }
}
