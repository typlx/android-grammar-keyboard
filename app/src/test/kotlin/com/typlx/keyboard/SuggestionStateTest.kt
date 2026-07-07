package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class SuggestionStateTest {

    @Test
    fun `Idle is distinct from Loading`() {
        assertNotEquals(SuggestionState.Idle, SuggestionState.Loading)
    }

    @Test
    fun `Available carries original and corrected text`() {
        val state = SuggestionState.Available("i went there", "I went there.")
        assertEquals("i went there", state.original)
        assertEquals("I went there.", state.corrected)
    }

    @Test
    fun `Available equality is value-based`() {
        val a = SuggestionState.Available("foo", "Foo.")
        val b = SuggestionState.Available("foo", "Foo.")
        assertEquals(a, b)
    }

    @Test
    fun `Available inequality when corrected differs`() {
        val a = SuggestionState.Available("foo", "Foo.")
        val b = SuggestionState.Available("foo", "Foo!")
        assertNotEquals(a, b)
    }

    @Test
    fun `Idle is not Available`() {
        val state: SuggestionState = SuggestionState.Idle
        assertFalse(state is SuggestionState.Available)
    }

    @Test
    fun `Loading is not Available`() {
        val state: SuggestionState = SuggestionState.Loading
        assertFalse(state is SuggestionState.Available)
    }

    @Test
    fun `cast to Available succeeds when state is Available`() {
        val state: SuggestionState = SuggestionState.Available("x", "X.")
        val cast = state as? SuggestionState.Available
        assertNotNull(cast)
        assertEquals("X.", cast!!.corrected)
    }

    @Test
    fun `cast to Available returns null when state is Idle`() {
        val state: SuggestionState = SuggestionState.Idle
        assertNull(state as? SuggestionState.Available)
    }

    @Test
    fun `cast to Available returns null when state is Loading`() {
        val state: SuggestionState = SuggestionState.Loading
        assertNull(state as? SuggestionState.Available)
    }

    @Test
    fun `Available toString contains original and corrected`() {
        val state = SuggestionState.Available("orig", "fixed")
        val str = state.toString()
        assertTrue(str.contains("orig"))
        assertTrue(str.contains("fixed"))
    }

    @Test
    fun `AutoCorrected carries original and corrected word`() {
        val state = SuggestionState.AutoCorrected("teh", "the")
        assertEquals("teh", state.original)
        assertEquals("the", state.corrected)
    }

    @Test
    fun `AutoCorrected equality is value-based`() {
        val a = SuggestionState.AutoCorrected("teh", "the")
        val b = SuggestionState.AutoCorrected("teh", "the")
        assertEquals(a, b)
    }

    @Test
    fun `AutoCorrected inequality when original differs`() {
        val a = SuggestionState.AutoCorrected("teh", "the")
        val b = SuggestionState.AutoCorrected("taht", "that")
        assertNotEquals(a, b)
    }

    @Test
    fun `Idle is not AutoCorrected`() {
        val state: SuggestionState = SuggestionState.Idle
        assertFalse(state is SuggestionState.AutoCorrected)
    }

    @Test
    fun `cast to AutoCorrected succeeds when state is AutoCorrected`() {
        val state: SuggestionState = SuggestionState.AutoCorrected("dont", "don't")
        val cast = state as? SuggestionState.AutoCorrected
        assertNotNull(cast)
        assertEquals("don't", cast!!.corrected)
    }

    @Test
    fun `cast to AutoCorrected returns null when state is Idle`() {
        val state: SuggestionState = SuggestionState.Idle
        assertNull(state as? SuggestionState.AutoCorrected)
    }
}
