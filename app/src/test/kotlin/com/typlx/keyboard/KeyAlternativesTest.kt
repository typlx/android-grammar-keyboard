package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class KeyAlternativesTest {

    @Test
    fun `e returns four accent variants lowercase`() {
        assertEquals(listOf("è", "é", "ê", "ë"), getAlternatives("e", isCaps = false))
    }

    @Test
    fun `e returns uppercase accents when isCaps true`() {
        assertEquals(listOf("È", "É", "Ê", "Ë"), getAlternatives("E", isCaps = true))
    }

    @Test
    fun `uppercase key string lowercased before lookup`() {
        // "A" should hit the same "a" entry and return lowercase alts when isCaps=false
        val alts = getAlternatives("A", isCaps = false)
        assertEquals(listOf("à", "á", "â", "ã", "ä", "å", "æ"), alts)
    }

    @Test
    fun `a has seven variants`() {
        assertEquals(7, getAlternatives("a", isCaps = false)!!.size)
    }

    @Test
    fun `n returns n-tilde`() {
        assertEquals(listOf("ñ"), getAlternatives("n", isCaps = false))
    }

    @Test
    fun `n returns uppercase N-tilde when caps`() {
        assertEquals(listOf("Ñ"), getAlternatives("N", isCaps = true))
    }

    @Test
    fun `c returns cedilla`() {
        assertTrue(getAlternatives("c", isCaps = false)!!.contains("ç"))
    }

    @Test
    fun `s returns sharp-s`() {
        assertTrue(getAlternatives("s", isCaps = false)!!.contains("ß"))
    }

    @Test
    fun `keys without alternatives return null`() {
        assertNull(getAlternatives("b", isCaps = false))
        assertNull(getAlternatives("q", isCaps = false))
        assertNull(getAlternatives("m", isCaps = false))
        assertNull(getAlternatives("x", isCaps = false))
    }

    @Test
    fun `all mapped keys have non-empty alternatives`() {
        KEY_ALTERNATIVES.forEach { (key, alts) ->
            assertTrue("$key must have at least one alternative", alts.isNotEmpty())
        }
    }
}
