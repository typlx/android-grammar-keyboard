package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class KeySizePresetTest {

    @Test
    fun compactScaleFactor() {
        assertEquals(0.85f, KeySizePreset.COMPACT.scaleFactor, 0.001f)
    }

    @Test
    fun normalScaleFactor() {
        assertEquals(1.00f, KeySizePreset.NORMAL.scaleFactor, 0.001f)
    }

    @Test
    fun largeScaleFactor() {
        assertEquals(1.15f, KeySizePreset.LARGE.scaleFactor, 0.001f)
    }

    @Test
    fun compactKeyHeightLessThanNormal() {
        assertTrue(KeySizePreset.COMPACT.scaleFactor < KeySizePreset.NORMAL.scaleFactor)
    }

    @Test
    fun largeKeyHeightGreaterThanNormal() {
        assertTrue(KeySizePreset.LARGE.scaleFactor > KeySizePreset.NORMAL.scaleFactor)
    }

    @Test
    fun normalScaleProducesBaselineHeight() {
        val baselineDp = 46f
        val actual = baselineDp * KeySizePreset.NORMAL.scaleFactor
        assertEquals(46f, actual, 0.001f)
    }

    @Test
    fun compactScaleProducesCorrectHeight() {
        val baselineDp = 46f
        val actual = baselineDp * KeySizePreset.COMPACT.scaleFactor
        assertEquals(39.1f, actual, 0.1f)
    }

    @Test
    fun largeScaleProducesCorrectHeight() {
        val baselineDp = 46f
        val actual = baselineDp * KeySizePreset.LARGE.scaleFactor
        assertEquals(52.9f, actual, 0.1f)
    }

    @Test
    fun threePresetsExist() {
        assertEquals(3, KeySizePreset.entries.size)
    }

    @Test
    fun enumValuesAreOrdered() {
        val entries = KeySizePreset.entries
        assertEquals(KeySizePreset.COMPACT, entries[0])
        assertEquals(KeySizePreset.NORMAL, entries[1])
        assertEquals(KeySizePreset.LARGE, entries[2])
    }
}
