package com.typlx.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SwipeTypingDecoderTest {

    private lateinit var decoder: SwipeTypingDecoder
    private val wordList = listOf(
        "the", "they", "there", "then", "these",
        "hello", "help", "held",
        "good", "god",
        "query", "quite",
        "world", "word", "work",
        "typing", "type",
        "swipe", "swift",
    )

    @Before
    fun setUp() {
        decoder = SwipeTypingDecoder()
    }

    @Test
    fun `empty path returns no results`() {
        val result = decoder.decode(emptyList(), wordList)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `single-key path returns no results`() {
        val result = decoder.decode(listOf("t"), wordList)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `exact path for the returns the as top result`() {
        val path = listOf("t", "h", "e")
        val result = decoder.decode(path, wordList)
        assertEquals("the", result.first())
    }

    @Test
    fun `path with duplicate consecutive keys is deduped`() {
        // Swiping slowly over "t" then "h" then "e" may produce repeated keys
        val path = listOf("t", "t", "h", "h", "e", "e")
        val result = decoder.decode(path, wordList)
        assertEquals("the", result.first())
    }

    @Test
    fun `path with extra keys still finds word as subsequence`() {
        // User's finger drifted through g between t and h
        val path = listOf("t", "g", "h", "e")
        val result = decoder.decode(path, wordList)
        assertTrue("the" in result)
    }

    @Test
    fun `returns up to maxResults candidates`() {
        val path = listOf("t", "h", "e")
        val result = decoder.decode(path, wordList, maxResults = 2)
        assertTrue(result.size <= 2)
    }

    @Test
    fun `path for hello returns hello`() {
        val path = listOf("h", "e", "l", "l", "o")
        val result = decoder.decode(path, wordList)
        assertEquals("hello", result.first())
    }

    @Test
    fun `word with more letters than path is not returned`() {
        // Path is only 2 keys long; "typing" (6 letters) shouldn't appear
        val path = listOf("t", "y")
        val result = decoder.decode(path, wordList)
        assertTrue(result.none { it.length > 2 + SwipeTypingDecoder.MAX_EXTRA })
    }

    @Test
    fun `longer word preferred when it matches path length more closely`() {
        // "the" and "they" are both subsequences of "they"; "they" should score better (0 extra)
        val path = listOf("t", "h", "e", "y")
        val result = decoder.decode(path, wordList)
        assertEquals("they", result.first())
    }

    @Test
    fun `case insensitive path matching`() {
        val path = listOf("T", "H", "E")
        val result = decoder.decode(path, wordList)
        assertEquals("the", result.first())
    }

    @Test
    fun `proper noun from word list is returned with original casing`() {
        val result = decoder.decode(
            listOf("l", "o", "n", "d", "o", "n"),
            listOf("London"),
        )
        assertEquals("London", result.firstOrNull())
    }

    @Test
    fun `camel-cased brand name from word list preserves casing`() {
        val result = decoder.decode(
            listOf("i", "p", "h", "o", "n", "e"),
            listOf("iPhone"),
        )
        assertEquals("iPhone", result.firstOrNull())
    }

    @Test
    fun `empty word list returns no results`() {
        val result = decoder.decode(listOf("t", "h", "e"), emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `path with too many extra keys is rejected`() {
        // MAX_EXTRA = 6; a path with 13 keys for a 3-letter word has 10 extras — over limit
        val path = "abcdefghijklm".map { it.toString() }
        val result = decoder.decode(path, listOf("abc"))
        // "abc" is a subsequence but the extra keys exceed MAX_EXTRA, so it should be absent
        assertTrue(result.isEmpty())
    }
}
