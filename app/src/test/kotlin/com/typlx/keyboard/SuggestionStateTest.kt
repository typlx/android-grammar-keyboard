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
        assertFalse(SuggestionState.Idle is SuggestionState.Available)
    }

    @Test
    fun `Loading is not Available`() {
        assertFalse(SuggestionState.Loading is SuggestionState.Available)
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
}
