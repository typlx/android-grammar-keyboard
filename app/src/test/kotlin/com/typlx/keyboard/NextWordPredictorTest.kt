package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NextWordPredictorTest {

    private lateinit var predictor: NextWordPredictor

    @Before
    fun setUp() {
        predictor = NextWordPredictor()
    }

    @Test
    fun `blank input returns empty`() {
        assertTrue(predictor.predict("").isEmpty())
        assertTrue(predictor.predict("   ").isEmpty())
    }

    @Test
    fun `unknown word returns empty`() {
        assertTrue(predictor.predict("xyzzy").isEmpty())
    }

    @Test
    fun `thank returns you`() {
        assertEquals(listOf("you"), predictor.predict("thank"))
    }

    @Test
    fun `happy returns birthday first`() {
        val result = predictor.predict("happy")
        assertEquals("birthday", result.first())
    }

    @Test
    fun `i returns am first`() {
        val result = predictor.predict("I")
        assertEquals("am", result.first())
    }

    @Test
    fun `lookup is case insensitive`() {
        val lower = predictor.predict("hello")
        val upper = predictor.predict("HELLO")
        val mixed = predictor.predict("Hello")
        assertEquals(lower, upper)
        assertEquals(lower, mixed)
    }

    @Test
    fun `trailing punctuation is stripped`() {
        val plain = predictor.predict("thank")
        val punctuated = predictor.predict("thank.")
        val exclamation = predictor.predict("thank!")
        assertEquals(plain, punctuated)
        assertEquals(plain, exclamation)
    }

    @Test
    fun `maxResults limits output`() {
        val result = predictor.predict("i", maxResults = 2)
        assertTrue(result.size <= 2)
    }

    @Test
    fun `maxResults of zero returns empty`() {
        val result = predictor.predict("happy", maxResults = 0)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `all bigram keys produce non-empty predictions`() {
        for (key in NextWordPredictor.BIGRAMS.keys) {
            val result = predictor.predict(key)
            assertTrue("Bigram '$key' should produce results", result.isNotEmpty())
        }
    }

    @Test
    fun `good returns morning first`() {
        assertEquals("morning", predictor.predict("good").first())
    }

    @Test
    fun `hello returns there first`() {
        assertEquals("there", predictor.predict("hello").first())
    }
}
