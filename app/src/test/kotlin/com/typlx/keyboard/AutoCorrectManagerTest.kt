package com.typlx.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AutoCorrectManagerTest {

    private lateinit var manager: AutoCorrectManager

    @Before
    fun setUp() {
        manager = AutoCorrectManager()
    }

    @Test
    fun `known typo returns correction`() {
        assertEquals("the", manager.correct("teh"))
    }

    @Test
    fun `unknown word returns null`() {
        assertNull(manager.correct("hello"))
    }

    @Test
    fun `empty string returns null`() {
        assertNull(manager.correct(""))
    }

    @Test
    fun `correction preserves lowercase`() {
        assertEquals("the", manager.correct("teh"))
    }

    @Test
    fun `correction preserves first-letter capitalisation`() {
        assertEquals("The", manager.correct("Teh"))
    }

    @Test
    fun `correction preserves all-caps`() {
        assertEquals("THE", manager.correct("TEH"))
    }

    @Test
    fun `another typo - adn to and`() {
        assertEquals("and", manager.correct("adn"))
    }

    @Test
    fun `capitalised adn to And`() {
        assertEquals("And", manager.correct("Adn"))
    }

    @Test
    fun `recieve corrected to receive`() {
        assertEquals("receive", manager.correct("recieve"))
    }

    @Test
    fun `Recieve capitalised preserved`() {
        assertEquals("Receive", manager.correct("Recieve"))
    }

    @Test
    fun `seperate corrected to separate`() {
        assertEquals("separate", manager.correct("seperate"))
    }

    @Test
    fun `definately corrected to definitely`() {
        assertEquals("definitely", manager.correct("definately"))
    }

    @Test
    fun `tommorow corrected to tomorrow`() {
        assertEquals("tomorrow", manager.correct("tommorow"))
    }

    @Test
    fun `word already correct returns null`() {
        assertNull(manager.correct("the"))
    }

    @Test
    fun `single character returns null`() {
        assertNull(manager.correct("a"))
    }

    @Test
    fun `typo in uppercase returns all-caps correction`() {
        assertEquals("SEPARATE", manager.correct("SEPERATE"))
    }

    @Test
    fun `dont corrected to don't`() {
        assertEquals("don't", manager.correct("dont"))
    }

    @Test
    fun `teh typo with mixed case - mixed treated as lowercase`() {
        // Only first-char capitalisation or all-caps are detected; mixed → lowercase correction
        assertEquals("the", manager.correct("tEh"))
    }
}
