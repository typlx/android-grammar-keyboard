package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GrammarUndoStateTest {

    private lateinit var state: GrammarUndoState

    @Before
    fun setUp() {
        state = GrammarUndoState()
    }

    @Test
    fun `initially canUndo is false`() {
        assertFalse(state.canUndo)
    }

    @Test
    fun `recordFix sets canUndo true`() {
        state.recordFix("original text", "fixed text")
        assertTrue(state.canUndo)
    }

    @Test
    fun `consume returns original and fixed texts`() {
        state.recordFix("hello world", "Hello, world.")
        val result = state.consume()
        assertNotNull(result)
        assertEquals("hello world", result!!.first)
        assertEquals("Hello, world.", result.second)
    }

    @Test
    fun `consume clears canUndo`() {
        state.recordFix("a", "b")
        state.consume()
        assertFalse(state.canUndo)
    }

    @Test
    fun `consume returns null when no undo available`() {
        assertNull(state.consume())
    }

    @Test
    fun `consume returns null after already consumed`() {
        state.recordFix("a", "b")
        state.consume()
        assertNull(state.consume())
    }

    @Test
    fun `clear sets canUndo false`() {
        state.recordFix("original", "fixed")
        state.clear()
        assertFalse(state.canUndo)
    }

    @Test
    fun `clear makes consume return null`() {
        state.recordFix("original", "fixed")
        state.clear()
        assertNull(state.consume())
    }

    @Test
    fun `clear is no-op when already clear`() {
        state.clear()
        assertFalse(state.canUndo)
    }

    @Test
    fun `recordFix overwrites previous undo state`() {
        state.recordFix("first original", "first fixed")
        state.recordFix("second original", "second fixed")
        assertTrue(state.canUndo)
        val result = state.consume()
        assertEquals("second original", result!!.first)
        assertEquals("second fixed", result.second)
    }
}
