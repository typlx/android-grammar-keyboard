package com.typlx.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PredictionSmartComposePrefsTest {

    @Test
    fun `KEY_WORD_PREDICTION has expected string value`() {
        assertEquals("word_prediction_enabled", PreferencesManager.KEY_WORD_PREDICTION)
    }

    @Test
    fun `KEY_SMART_COMPOSE has expected string value`() {
        assertEquals("smart_compose_enabled", PreferencesManager.KEY_SMART_COMPOSE)
    }

    @Test
    fun `word prediction and smart compose keys are distinct`() {
        assertNotEquals(PreferencesManager.KEY_WORD_PREDICTION, PreferencesManager.KEY_SMART_COMPOSE)
    }

    @Test
    fun `new pref keys are distinct from all existing typing pref keys`() {
        val allKeys = listOf(
            PreferencesManager.KEY_HAPTIC,
            PreferencesManager.KEY_AUTO_SUGGEST,
            PreferencesManager.KEY_THEME_PRESET,
            PreferencesManager.KEY_CORNER_RADIUS_DP,
            PreferencesManager.KEY_KEY_ALPHA_PERCENT,
            PreferencesManager.KEY_DOUBLE_SPACE_PERIOD,
            PreferencesManager.KEY_AUTO_CAP,
            PreferencesManager.KEY_GRAMMAR_INSTRUCTION_SUFFIX,
            PreferencesManager.KEY_WORD_PREDICTION,
            PreferencesManager.KEY_SMART_COMPOSE,
        )
        assertEquals("Duplicate preference key found", allKeys.size, allKeys.distinct().size)
    }

    @Test
    fun `KEY_WORD_PREDICTION does not collide with smart compose key`() {
        assertNotEquals(
            PreferencesManager.KEY_WORD_PREDICTION,
            PreferencesManager.KEY_AUTO_SUGGEST,
        )
    }
}
