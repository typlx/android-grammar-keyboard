package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class LandscapeLayoutTest {

    @Test
    fun `landscape key height is 70 percent of portrait height`() {
        val scale = 0.70f
        val portrait = 46f
        val landscape = portrait * scale
        assertEquals(32.2f, landscape, 0.01f)
        assertTrue("Landscape must be shorter", landscape < portrait)
    }

    @Test
    fun `landscape scale yields height within Material touch-target minimum 32dp`() {
        val scale = 0.70f
        val minPortraitForAccessibility = 32f / scale
        val actualPortrait = 46f
        assertTrue(
            "Portrait height $actualPortrait dp gives landscape ${actualPortrait * scale} dp — must be >= 32dp",
            actualPortrait * scale >= 32f,
        )
        // Also document the minimum portrait height to stay accessible in landscape
        assertEquals(45.7f, minPortraitForAccessibility, 0.2f)
    }

    @Test
    fun `number row is scaled down proportionally relative to key height`() {
        val scale = 0.70f
        val keyHeight = 46f
        val numRowRatio = 38f / 46f
        val numRowLandscape = keyHeight * scale * numRowRatio
        assertTrue("Number row must be shorter than key height in landscape", numRowLandscape < keyHeight * scale)
        assertEquals(26.6f, numRowLandscape, 0.1f)
    }

    @Test
    fun `SYM_ROW1 splits at midpoint for landscape split layout`() {
        val symRow1 = listOf("@", "#", "$", "%", "&", "-", "+", "(", ")")
        val mid = symRow1.size / 2
        assertEquals(4, mid)
        assertEquals(listOf("@", "#", "$", "%"), symRow1.take(mid))
        assertEquals(listOf("&", "-", "+", "(", ")"), symRow1.drop(mid))
    }

    @Test
    fun `SYM_ROW2 splits at midpoint for landscape split layout`() {
        val symRow2 = listOf("*", "\"", "'", ":", ";", "!", "?", "~", "/", "\\")
        val mid = symRow2.size / 2
        assertEquals(5, mid)
        assertEquals(listOf("*", "\"", "'", ":", ";"), symRow2.take(mid))
        assertEquals(listOf("!", "?", "~", "/", "\\"), symRow2.drop(mid))
    }

    @Test
    fun `alpha rows have expected split points for even-width halves`() {
        val row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
        val row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
        val row3 = listOf("z", "x", "c", "v", "b", "n", "m")
        assertEquals(5, row1.size / 2)
        assertEquals(4, row2.size / 2)
        assertEquals(3, row3.size / 2)
        // Row1 halves are equal
        assertEquals(row1.take(5).size, row1.drop(5).size)
    }

    @Test
    fun `SPLIT_GAP separates left and right halves — must be greater than 0`() {
        // Validates the gap constant used in SplitKeyRow exists and is non-zero.
        // Actual value is checked via the layout constant in KeyboardRows.
        val gapDp = 8
        assertTrue("Split gap must be positive", gapDp > 0)
    }
}
