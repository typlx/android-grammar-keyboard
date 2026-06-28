package com.typlx.keyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class WordDeleteCountTest {

    @Test
    fun `empty string returns 0`() {
        assertEquals(0, wordDeleteCount(""))
    }

    @Test
    fun `single character returns 1`() {
        assertEquals(1, wordDeleteCount("a"))
    }

    @Test
    fun `single word returns word length`() {
        assertEquals(5, wordDeleteCount("hello"))
    }

    @Test
    fun `word after space deletes only the word`() {
        // "hello world" → deletes "world" (5 chars), leaves "hello "
        assertEquals(5, wordDeleteCount("hello world"))
    }

    @Test
    fun `trailing space then word deletes both`() {
        // "hello world " → skips " " (1) + deletes "world" (5) = 6
        assertEquals(6, wordDeleteCount("hello world "))
    }

    @Test
    fun `only whitespace deletes the whitespace`() {
        assertEquals(2, wordDeleteCount("  "))
    }

    @Test
    fun `trailing spaces after word deletes all`() {
        // "hello  " → skips "  " (2) + deletes "hello" (5) = 7
        assertEquals(7, wordDeleteCount("hello  "))
    }

    @Test
    fun `single space returns at least 1`() {
        assertEquals(1, wordDeleteCount(" "))
    }

    @Test
    fun `punctuation boundary respected`() {
        // "end." → no whitespace boundary → delete entire "end." (4)
        assertEquals(4, wordDeleteCount("end."))
    }

    @Test
    fun `newline counts as whitespace boundary`() {
        // "line1\nline2" → skip nothing, delete "line2" (5)
        assertEquals(5, wordDeleteCount("line1\nline2"))
    }
}
