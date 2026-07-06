package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class KeyHeightPreferenceTest {

    @Test
    fun `KEY_KEY_HEIGHT_DP has stable persistence key`() {
        assertEquals("key_height_dp", PreferencesManager.KEY_KEY_HEIGHT_DP)
    }

    @Test
    fun `KEY_HEIGHT_DP_DEFAULT is 46`() {
        assertEquals(46, PreferencesManager.KEY_HEIGHT_DP_DEFAULT)
    }

    @Test
    fun `KEY_HEIGHT_DP_MIN is 36`() {
        assertEquals(36, PreferencesManager.KEY_HEIGHT_DP_MIN)
    }

    @Test
    fun `KEY_HEIGHT_DP_MAX is 64`() {
        assertEquals(64, PreferencesManager.KEY_HEIGHT_DP_MAX)
    }

    @Test
    fun `default height is within valid range`() {
        assertTrue(
            "Default must be within [MIN, MAX]",
            PreferencesManager.KEY_HEIGHT_DP_DEFAULT in
                PreferencesManager.KEY_HEIGHT_DP_MIN..PreferencesManager.KEY_HEIGHT_DP_MAX
        )
    }

    @Test
    fun `clamping logic clamps below min to min`() {
        val raw = 10
        val clamped = raw.coerceIn(PreferencesManager.KEY_HEIGHT_DP_MIN, PreferencesManager.KEY_HEIGHT_DP_MAX)
        assertEquals(PreferencesManager.KEY_HEIGHT_DP_MIN, clamped)
    }

    @Test
    fun `clamping logic clamps above max to max`() {
        val raw = 100
        val clamped = raw.coerceIn(PreferencesManager.KEY_HEIGHT_DP_MIN, PreferencesManager.KEY_HEIGHT_DP_MAX)
        assertEquals(PreferencesManager.KEY_HEIGHT_DP_MAX, clamped)
    }

    @Test
    fun `clamping logic preserves in-range value`() {
        val raw = 50
        val clamped = raw.coerceIn(PreferencesManager.KEY_HEIGHT_DP_MIN, PreferencesManager.KEY_HEIGHT_DP_MAX)
        assertEquals(50, clamped)
    }

    @Test
    fun `KEY_KEY_HEIGHT_DP is distinct from other preference keys`() {
        val others = listOf(
            PreferencesManager.KEY_KEY_SIZE_PRESET,
            PreferencesManager.KEY_HAPTIC,
            PreferencesManager.KEY_AUTO_SUGGEST,
            PreferencesManager.KEY_CORNER_RADIUS_DP,
            PreferencesManager.KEY_KEY_ALPHA_PERCENT,
            PreferencesManager.KEY_SHOW_NUMBER_ROW,
        )
        assertTrue(
            "KEY_KEY_HEIGHT_DP must not collide with another preference key",
            PreferencesManager.KEY_KEY_HEIGHT_DP !in others,
        )
    }

    @Test
    fun `compact preset height 39 is within slider range`() {
        assertTrue(39 in PreferencesManager.KEY_HEIGHT_DP_MIN..PreferencesManager.KEY_HEIGHT_DP_MAX)
    }

    @Test
    fun `large preset height 53 is within slider range`() {
        assertTrue(53 in PreferencesManager.KEY_HEIGHT_DP_MIN..PreferencesManager.KEY_HEIGHT_DP_MAX)
    }

    @Test
    fun `slider step count equals range size minus one`() {
        val expectedSteps = PreferencesManager.KEY_HEIGHT_DP_MAX - PreferencesManager.KEY_HEIGHT_DP_MIN - 1
        assertEquals(27, expectedSteps)
    }
}
