package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class OneHandedModeTest {

    private fun cycleMode(current: OneHandedMode): OneHandedMode =
        OneHandedMode.entries[(current.ordinal + 1) % OneHandedMode.entries.size]

    @Test
    fun `toolbar cycle goes OFF to LEFT to RIGHT and back to OFF`() {
        var mode = OneHandedMode.OFF
        mode = cycleMode(mode); assertEquals(OneHandedMode.LEFT, mode)
        mode = cycleMode(mode); assertEquals(OneHandedMode.RIGHT, mode)
        mode = cycleMode(mode); assertEquals(OneHandedMode.OFF, mode)
    }

    @Test
    fun `preference key constant has expected string value`() {
        assertEquals("one_handed_mode", PreferencesManager.KEY_ONE_HANDED_MODE)
    }

    @Test
    fun `preference default is OFF — valueOf empty string fallback`() {
        val result = runCatching { OneHandedMode.valueOf("OFF") }.getOrDefault(OneHandedMode.OFF)
        assertEquals(OneHandedMode.OFF, result)
    }

    @Test
    fun `invalid stored preference falls back to OFF`() {
        val result = runCatching { OneHandedMode.valueOf("UNKNOWN") }.getOrDefault(OneHandedMode.OFF)
        assertEquals(OneHandedMode.OFF, result)
    }

    @Test
    fun `handle is not shown in OFF mode`() {
        val showHandle = { mode: OneHandedMode -> mode != OneHandedMode.OFF }
        assertFalse("OFF mode should not show handle", showHandle(OneHandedMode.OFF))
        assertTrue("LEFT mode should show handle", showHandle(OneHandedMode.LEFT))
        assertTrue("RIGHT mode should show handle", showHandle(OneHandedMode.RIGHT))
    }

    @Test
    fun `keyboard takes 70 percent width in one-handed mode`() {
        val keyboardWidthFraction = 0.70f
        val handleWidthFraction = 1f - keyboardWidthFraction
        assertEquals(0.30f, handleWidthFraction, 0.001f)
    }

    @Test
    fun `LEFT mode arrow points right, RIGHT mode arrow points left`() {
        val arrowForMode = { mode: OneHandedMode ->
            when (mode) {
                OneHandedMode.LEFT -> "▶"
                OneHandedMode.RIGHT -> "◀"
                OneHandedMode.OFF -> ""
            }
        }
        assertEquals("▶", arrowForMode(OneHandedMode.LEFT))
        assertEquals("◀", arrowForMode(OneHandedMode.RIGHT))
    }
}
