package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class WordPredictorTest {

    private lateinit var predictor: WordPredictor

    @Before
    fun setUp() {
        predictor = WordPredictor()
    }

    @Test
    fun `returns empty list for prefix shorter than 2 chars`() {
        assertTrue(predictor.predict("").isEmpty())
        assertTrue(predictor.predict("a").isEmpty())
    }

    @Test
    fun `returns matching words for lowercase prefix`() {
        val results = predictor.predict("th")
        assertTrue("Expected matches for 'th'", results.isNotEmpty())
        results.forEach { word ->
            assertTrue("$word does not start with 'th'", word.startsWith("th"))
        }
    }

    @Test
    fun `returns at most maxResults words`() {
        val results = predictor.predict("th", maxResults = 3)
        assertTrue(results.size <= 3)
    }

    @Test
    fun `does not include exact match when prefix matches word exactly`() {
        val results = predictor.predict("the")
        // "the" itself should not be returned — only completions beyond the typed prefix
        assertFalse("Should not suggest the exact typed word", results.contains("the"))
    }

    @Test
    fun `capitalizes results when prefix starts with uppercase`() {
        val results = predictor.predict("Th")
        results.forEach { word ->
            assertTrue("$word should start with uppercase", word[0].isUpperCase())
        }
    }

    @Test
    fun `uppercases all results when prefix is all-caps`() {
        val results = predictor.predict("TH")
        results.forEach { word ->
            assertTrue("$word should be all uppercase", word == word.uppercase())
        }
    }

    @Test
    fun `personal words take priority over common words`() {
        val personal = listOf("typescript", "thyme", "throwable")
        val results = predictor.predict("th", personal, maxResults = 3)
        // Personal words starting with "th" should appear first
        val personalInResults = results.filter { w ->
            personal.any { it.equals(w, ignoreCase = true) }
        }
        assertTrue("Personal words should appear in results", personalInResults.isNotEmpty())
        // First result should be a personal word (since we have 3 personal "th" words)
        val firstWord = results.firstOrNull()
        assertNotNull(firstWord)
        assertTrue("First result should be a personal word",
            personal.any { it.equals(firstWord, ignoreCase = true) })
    }

    @Test
    fun `personal words do not duplicate common word entries`() {
        val personal = listOf("there")
        val results = predictor.predict("th", personal, maxResults = 5)
        val thereCount = results.count { it.equals("there", ignoreCase = true) }
        assertEquals("'there' should appear only once", 1, thereCount)
    }

    @Test
    fun `returns empty list when no words match prefix`() {
        // "zz" is highly unlikely to match anything in the common word list
        val results = predictor.predict("zz")
        assertTrue(results.isEmpty())
    }

    @Test
    fun `handles prefix with mixed case correctly`() {
        // Prefix starting with uppercase should capitalize results
        val results = predictor.predict("He")
        assertTrue("Expected at least one result for 'He'", results.isNotEmpty())
        results.forEach { word ->
            assertTrue("$word should start with uppercase H", word.startsWith("H"))
        }
    }

    @Test
    fun `WordSuggestions state carries words`() {
        val state = SuggestionState.WordSuggestions(listOf("hello", "help", "her"))
        assertEquals(3, state.words.size)
        assertEquals("hello", state.words[0])
    }

    @Test
    fun `WordSuggestions is distinct from Idle and Available`() {
        val state: SuggestionState = SuggestionState.WordSuggestions(listOf("hi"))
        assertFalse(state == SuggestionState.Idle)
        assertFalse(state is SuggestionState.Available)
        assertTrue(state is SuggestionState.WordSuggestions)
    }
}
