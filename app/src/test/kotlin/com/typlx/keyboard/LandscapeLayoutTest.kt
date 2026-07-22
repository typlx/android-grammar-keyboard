package com.typlx.keyboard

import com.typlx.keyboard.ui.LANDSCAPE_HEIGHT_SCALE
import org.junit.Assert.*
import org.junit.Test

class LandscapeLayoutTest {

    @Test
    fun `LANDSCAPE_HEIGHT_SCALE constant is 0_70 — compact in landscape`() {
        // Tests the actual production constant used in KeyboardScreen.effectiveKeyHeight.
        // If the scale changes, this catches it before users see broken layouts.
        assertEquals(0.70f, LANDSCAPE_HEIGHT_SCALE, 0.001f)
    }

    @Test
    fun `landscape key height stays at or above 32dp Material touch-target minimum`() {
        val portraitDefault = 46f
        val landscapeHeight = portraitDefault * LANDSCAPE_HEIGHT_SCALE
        assertTrue(
            "Default portrait ${portraitDefault}dp → landscape ${landscapeHeight}dp must be >= 32dp",
            landscapeHeight >= 32f,
        )
        assertTrue("Landscape height must be shorter than portrait", landscapeHeight < portraitDefault)
    }

    @Test
    fun `QWERTY row1 splits into equal halves for landscape split layout`() {
        val row = LAYOUT_QWERTY.row1
        val splitAt = row.size / 2
        val left = row.take(splitAt)
        val right = row.drop(splitAt)
        assertEquals("Left half must have 5 keys", 5, left.size)
        assertEquals("Right half must have 5 keys", 5, right.size)
        assertEquals("Left half starts with q", "q", left.first())
        assertEquals("Right half ends with p", "p", right.last())
    }

    @Test
    fun `QWERTY row2 split gives 4 keys left and 5 keys right`() {
        val row = LAYOUT_QWERTY.row2
        val splitAt = row.size / 2
        assertEquals("Left half of 9-key row2 has 4 keys", 4, row.take(splitAt).size)
        assertEquals("Right half of 9-key row2 has 5 keys", 5, row.drop(splitAt).size)
    }

    @Test
    fun `QWERTY row3 split gives 3 keys left and 4 keys right`() {
        val row = LAYOUT_QWERTY.row3
        val splitAt = row.size / 2
        assertEquals("Left half of 7-key row3 has 3 keys", 3, row.take(splitAt).size)
        assertEquals("Right half of 7-key row3 has 4 keys", 4, row.drop(splitAt).size)
    }

    @Test
    fun `minimum portrait height that stays accessible in landscape is below 46dp default`() {
        // Minimum portrait height so landscape >= 32dp: 32 / 0.70 ≈ 45.7dp.
        // The default 46dp clears this floor, keeping keys accessible in landscape.
        val minPortrait = 32f / LANDSCAPE_HEIGHT_SCALE
        assertTrue(
            "Default key height 46dp must exceed minimum portrait floor ${minPortrait}dp",
            46f >= minPortrait,
        )
    }
}
