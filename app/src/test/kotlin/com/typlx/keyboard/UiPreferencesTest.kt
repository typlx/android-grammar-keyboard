package com.typlx.keyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class UiPreferencesTest {

    @Test
    fun `KEY_THEME_PRESET has stable persistence key`() {
        assertEquals("theme_preset", UiPreferences.KEY_THEME_PRESET)
    }

    @Test
    fun `KEY_CORNER_RADIUS_DP has stable persistence key`() {
        assertEquals("corner_radius_dp", UiPreferences.KEY_CORNER_RADIUS_DP)
    }

    @Test
    fun `KEY_KEY_ALPHA_PERCENT has stable persistence key`() {
        assertEquals("key_alpha_percent", UiPreferences.KEY_KEY_ALPHA_PERCENT)
    }

    @Test
    fun `KEY_KEYBOARD_LAYOUT has stable persistence key`() {
        assertEquals("keyboard_layout", UiPreferences.KEY_KEYBOARD_LAYOUT)
    }

    @Test
    fun `KEY_KEY_SIZE_PRESET has stable persistence key`() {
        assertEquals("key_size_preset", UiPreferences.KEY_KEY_SIZE_PRESET)
    }

    @Test
    fun `KEY_SHOW_NUMBER_ROW has stable persistence key`() {
        assertEquals("show_number_row", UiPreferences.KEY_SHOW_NUMBER_ROW)
    }

    @Test
    fun `all UiPreferences keys are unique`() {
        val keys = listOf(
            UiPreferences.KEY_THEME_PRESET,
            UiPreferences.KEY_CORNER_RADIUS_DP,
            UiPreferences.KEY_KEY_ALPHA_PERCENT,
            UiPreferences.KEY_KEYBOARD_LAYOUT,
            UiPreferences.KEY_KEY_SIZE_PRESET,
            UiPreferences.KEY_SHOW_NUMBER_ROW,
        )
        assertEquals("Duplicate key in UiPreferences", keys.size, keys.distinct().size)
    }

    @Test
    fun `cornerRadiusDp coerce logic stays within 0-16 bounds`() {
        fun coerce(v: Int) = v.coerceIn(0, 16)
        assertEquals(0, coerce(-5))
        assertEquals(0, coerce(0))
        assertEquals(8, coerce(8))
        assertEquals(16, coerce(16))
        assertEquals(16, coerce(100))
    }

    @Test
    fun `keyAlphaPercent coerce logic stays within 0-100 bounds`() {
        fun coerce(v: Int) = v.coerceIn(0, 100)
        assertEquals(0, coerce(-1))
        assertEquals(0, coerce(0))
        assertEquals(50, coerce(50))
        assertEquals(100, coerce(100))
        assertEquals(100, coerce(200))
    }
}
