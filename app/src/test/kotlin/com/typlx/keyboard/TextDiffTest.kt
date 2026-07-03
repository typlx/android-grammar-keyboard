package com.typlx.keyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class TextDiffTest {

    private fun segments(original: String, corrected: String) = diffWords(original, corrected)

    private fun reconstructCorrected(segments: List<DiffSegment>) =
        segments.filter { it.kind != DiffKind.REMOVED }.joinToString("") { it.text }

    private fun reconstructOriginal(segments: List<DiffSegment>) =
        segments.filter { it.kind != DiffKind.ADDED }.joinToString("") { it.text }

    @Test
    fun `identical texts produce single unchanged segment`() {
        val result = segments("hello world", "hello world")
        assertEquals(1, result.size)
        assertEquals(DiffKind.UNCHANGED, result[0].kind)
        assertEquals("hello world", result[0].text)
    }

    @Test
    fun `single word substitution`() {
        val result = segments("I has gone", "I have gone")
        val removed = result.filter { it.kind == DiffKind.REMOVED }.joinToString("") { it.text }
        val added = result.filter { it.kind == DiffKind.ADDED }.joinToString("") { it.text }
        assertEquals("has", removed.trim())
        assertEquals("have", added.trim())
    }

    @Test
    fun `added word at end`() {
        val result = segments("hello", "hello world")
        val addedText = result.filter { it.kind == DiffKind.ADDED }.joinToString("") { it.text }
        assertEquals(" world", addedText)
    }

    @Test
    fun `removed word at start`() {
        val result = segments("very long text", "long text")
        val removedText = result.filter { it.kind == DiffKind.REMOVED }.joinToString("") { it.text }
        assertEquals("very ", removedText)
    }

    @Test
    fun `reconstructed corrected text matches corrected input`() {
        val original = "He go to the store yesterday"
        val corrected = "He went to the store yesterday"
        val result = segments(original, corrected)
        assertEquals(corrected, reconstructCorrected(result))
    }

    @Test
    fun `reconstructed original text matches original input`() {
        val original = "She are very happy today"
        val corrected = "She is very happy today"
        val result = segments(original, corrected)
        assertEquals(original, reconstructOriginal(result))
    }

    @Test
    fun `empty original produces single added segment`() {
        val result = segments("", "hello")
        assertEquals(1, result.size)
        assertEquals(DiffKind.ADDED, result[0].kind)
        assertEquals("hello", result[0].text)
    }

    @Test
    fun `empty corrected produces single removed segment`() {
        val result = segments("hello", "")
        assertEquals(1, result.size)
        assertEquals(DiffKind.REMOVED, result[0].kind)
        assertEquals("hello", result[0].text)
    }

    @Test
    fun `multiple changes across sentence`() {
        val original = "I has went to there yesterday"
        val corrected = "I had gone there yesterday"
        val result = segments(original, corrected)
        // Corrected text can be fully reconstructed from non-REMOVED segments
        assertEquals(corrected, reconstructCorrected(result))
        // Original can be fully reconstructed from non-ADDED segments
        assertEquals(original, reconstructOriginal(result))
        // There are some changes
        val hasAdded = result.any { it.kind == DiffKind.ADDED }
        val hasRemoved = result.any { it.kind == DiffKind.REMOVED }
        assert(hasAdded)
        assert(hasRemoved)
    }

    @Test
    fun `punctuation differences detected`() {
        val original = "Hello world"
        val corrected = "Hello, world!"
        val result = segments(original, corrected)
        assertEquals(corrected, reconstructCorrected(result))
    }

    @Test
    fun `all unchanged words are tagged UNCHANGED`() {
        val original = "The quick brown fox"
        val corrected = "The quick brown fox jumps"
        val result = segments(original, corrected)
        val unchanged = result.filter { it.kind == DiffKind.UNCHANGED }.joinToString("") { it.text }
        assertEquals("The quick brown fox", unchanged)
    }
}
