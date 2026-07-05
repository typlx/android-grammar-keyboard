package com.typlx.keyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class OneHandedModeTest {

    @Test
    fun defaultValueIsOff() {
        assertEquals(OneHandedMode.OFF, OneHandedMode.OFF)
    }

    @Test
    fun enumHasThreeValues() {
        assertEquals(3, OneHandedMode.entries.size)
    }

    @Test
    fun entriesAreOffLeftRight() {
        val entries = OneHandedMode.entries
        assertEquals(OneHandedMode.OFF, entries[0])
        assertEquals(OneHandedMode.LEFT, entries[1])
        assertEquals(OneHandedMode.RIGHT, entries[2])
    }

    @Test
    fun valueOfOff() {
        assertEquals(OneHandedMode.OFF, OneHandedMode.valueOf("OFF"))
    }

    @Test
    fun valueOfLeft() {
        assertEquals(OneHandedMode.LEFT, OneHandedMode.valueOf("LEFT"))
    }

    @Test
    fun valueOfRight() {
        assertEquals(OneHandedMode.RIGHT, OneHandedMode.valueOf("RIGHT"))
    }

    @Test
    fun flipFromLeftReturnsRight() {
        val current = OneHandedMode.LEFT
        val flipped = if (current == OneHandedMode.LEFT) OneHandedMode.RIGHT else OneHandedMode.LEFT
        assertEquals(OneHandedMode.RIGHT, flipped)
    }

    @Test
    fun flipFromRightReturnsLeft() {
        val current = OneHandedMode.RIGHT
        val flipped = if (current == OneHandedMode.RIGHT) OneHandedMode.LEFT else OneHandedMode.RIGHT
        assertEquals(OneHandedMode.LEFT, flipped)
    }

    @Test
    fun isActiveWhenNotOff() {
        val isActive: (OneHandedMode) -> Boolean = { it != OneHandedMode.OFF }
        assertEquals(false, isActive(OneHandedMode.OFF))
        assertEquals(true, isActive(OneHandedMode.LEFT))
        assertEquals(true, isActive(OneHandedMode.RIGHT))
    }
}
