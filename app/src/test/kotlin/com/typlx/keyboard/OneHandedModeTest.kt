package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class OneHandedModeTest {

    @Test
    fun defaultIsOff() {
        assertEquals(OneHandedMode.OFF, OneHandedMode.valueOf("OFF"))
    }

    @Test
    fun leftValueRoundTrips() {
        val mode = OneHandedMode.LEFT
        assertEquals(OneHandedMode.LEFT, OneHandedMode.valueOf(mode.name))
    }

    @Test
    fun rightValueRoundTrips() {
        val mode = OneHandedMode.RIGHT
        assertEquals(OneHandedMode.RIGHT, OneHandedMode.valueOf(mode.name))
    }

    @Test
    fun threeModesExist() {
        assertEquals(3, OneHandedMode.entries.size)
    }

    @Test
    fun cycleOffToLeftToRightToOff() {
        val cycle = listOf(OneHandedMode.OFF, OneHandedMode.LEFT, OneHandedMode.RIGHT)
        var mode = OneHandedMode.OFF
        cycle.forEachIndexed { index, expected ->
            assertEquals("cycle step $index", expected, mode)
            mode = OneHandedMode.entries[(mode.ordinal + 1) % OneHandedMode.entries.size]
        }
        // after three steps, back to OFF
        assertEquals(OneHandedMode.OFF, mode)
    }

    @Test
    fun invalidNameFallbackToOff() {
        val result = try {
            OneHandedMode.valueOf("INVALID_MODE")
        } catch (_: IllegalArgumentException) {
            OneHandedMode.OFF
        }
        assertEquals(OneHandedMode.OFF, result)
    }

    @Test
    fun leftIsNotRight() {
        assertNotEquals(OneHandedMode.LEFT, OneHandedMode.RIGHT)
    }

    @Test
    fun offIsNotLeft() {
        assertNotEquals(OneHandedMode.OFF, OneHandedMode.LEFT)
    }
}
