package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class LandscapeLayoutTest {

    private fun landscapeHeight(portraitDp: Float, scale: Float = 0.70f) = portraitDp * scale

    @Test
    fun landscapeHeightIsSeventyPercentOfPortrait() {
        val result = landscapeHeight(46f)
        assertEquals(32.2f, result, 0.01f)
    }

    @Test
    fun landscapeHeightForSmallKey() {
        val result = landscapeHeight(36f)
        assertEquals(25.2f, result, 0.01f)
    }

    @Test
    fun landscapeHeightForLargeKey() {
        val result = landscapeHeight(64f)
        assertEquals(44.8f, result, 0.01f)
    }

    @Test
    fun landscapeHeightIsLessThanPortrait() {
        val portrait = 46f
        val landscape = landscapeHeight(portrait)
        assertTrue("Landscape key height should be less than portrait", landscape < portrait)
    }

    @Test
    fun numberRowScalesRelativeToKeyHeight() {
        val keyHeight = 46f
        val numRowScale = 38f / 46f
        val numRowPortrait = keyHeight * numRowScale
        val numRowLandscape = landscapeHeight(keyHeight) * numRowScale
        assertTrue("Landscape number row should be shorter", numRowLandscape < numRowPortrait)
        assertEquals(26.6f, numRowLandscape, 0.1f)
    }

    @Test
    fun splitRowMidpointForRow1() {
        val row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
        val mid = row1.size / 2
        assertEquals(5, mid)
        assertEquals(listOf("q", "w", "e", "r", "t"), row1.take(mid))
        assertEquals(listOf("y", "u", "i", "o", "p"), row1.drop(mid))
    }

    @Test
    fun splitRowMidpointForRow2() {
        val row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
        val mid = row2.size / 2
        assertEquals(4, mid)
        assertEquals(listOf("a", "s", "d", "f"), row2.take(mid))
        assertEquals(listOf("g", "h", "j", "k", "l"), row2.drop(mid))
    }

    @Test
    fun splitRowMidpointForRow3() {
        val row3 = listOf("z", "x", "c", "v", "b", "n", "m")
        val mid = row3.size / 2
        assertEquals(3, mid)
        assertEquals(listOf("z", "x", "c"), row3.take(mid))
        assertEquals(listOf("v", "b", "n", "m"), row3.drop(mid))
    }
}
