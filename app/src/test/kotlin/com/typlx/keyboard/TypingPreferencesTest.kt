package com.typlx.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TypingPreferencesTest {

    @Test
    fun `KEY_DOUBLE_SPACE_PERIOD has expected string value`() {
        assertEquals("double_space_period_enabled", PreferencesManager.KEY_DOUBLE_SPACE_PERIOD)
    }

    @Test
    fun `KEY_AUTO_CAP has expected string value`() {
        assertEquals("auto_cap_enabled", PreferencesManager.KEY_AUTO_CAP)
    }

    @Test
    fun `typing pref keys are distinct from each other`() {
        assertNotEquals(PreferencesManager.KEY_DOUBLE_SPACE_PERIOD, PreferencesManager.KEY_AUTO_CAP)
    }

    @Test
    fun `all pref keys are unique — no accidental duplicate SharedPreferences keys`() {
        val keys = listOf(
            PreferencesManager.KEY_HAPTIC,
            PreferencesManager.KEY_AUTO_SUGGEST,
            PreferencesManager.KEY_THEME_PRESET,
            PreferencesManager.KEY_CORNER_RADIUS_DP,
            PreferencesManager.KEY_KEY_ALPHA_PERCENT,
            PreferencesManager.KEY_DOUBLE_SPACE_PERIOD,
            PreferencesManager.KEY_AUTO_CAP,
        )
        assertEquals("Duplicate preference key found", keys.size, keys.distinct().size)
    }

    @Test
    fun `auto-cap guard — shouldShiftAfterSpace still fires when autoCap pref is true`() {
        // Validate that the underlying sentence-end detection remains correct.
        // The guard in GrammarKeyboardService is: if (prefs.autoCapEnabled) { ... }
        // so we verify the pure function it calls still returns true for a sentence end.
        assert(shouldShiftAfterSpace("Hello. ")) { "Should shift after '. '" }
        assert(shouldShiftAfterSpace("Wait! ")) { "Should shift after '! '" }
        assert(shouldShiftAfterSpace("Really? ")) { "Should shift after '? '" }
    }

    @Test
    fun `auto-cap guard — shouldShiftAfterSpace returns false for non-sentence-end`() {
        assert(!shouldShiftAfterSpace("hello ")) { "Should not shift after plain word" }
        assert(!shouldShiftAfterSpace("")) { "Should not shift for empty string" }
        assert(!shouldShiftAfterSpace("hi, ")) { "Should not shift after comma" }
    }
}
