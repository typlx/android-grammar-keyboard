package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EmojiSuggestionHelperTest {

    private lateinit var helper: EmojiSuggestionHelper

    @Before
    fun setUp() {
        helper = EmojiSuggestionHelper()
    }

    @Test
    fun `known keyword returns relevant emojis`() {
        val result = helper.suggest("happy")
        assertTrue(result.isNotEmpty())
        assertTrue(result.contains("😊"))
    }

    @Test
    fun `keyword matching is case-insensitive`() {
        val lower = helper.suggest("happy")
        val upper = helper.suggest("HAPPY")
        val mixed = helper.suggest("Happy")
        assertEquals(lower, upper)
        assertEquals(lower, mixed)
    }

    @Test
    fun `unknown word returns empty list`() {
        val result = helper.suggest("xyzzyx")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `blank string returns empty list`() {
        assertTrue(helper.suggest("").isEmpty())
        assertTrue(helper.suggest("   ").isEmpty())
    }

    @Test
    fun `maxResults limits output to 3 by default`() {
        val result = helper.suggest("happy")
        assertTrue(result.size <= 3)
    }

    @Test
    fun `maxResults parameter is respected`() {
        val result = helper.suggest("happy", maxResults = 1)
        assertEquals(1, result.size)
    }

    @Test
    fun `trailing punctuation is stripped before lookup`() {
        val withPeriod = helper.suggest("happy.")
        val plain = helper.suggest("happy")
        assertEquals(plain, withPeriod)
    }

    @Test
    fun `love keyword returns heart emoji`() {
        val result = helper.suggest("love")
        assertTrue(result.contains("❤️"))
    }

    @Test
    fun `birthday keyword returns cake or party emoji`() {
        val result = helper.suggest("birthday")
        assertTrue(result.any { it == "🎂" || it == "🎉" || it == "🎁" })
    }

    @Test
    fun `weather keyword sun returns sun emoji`() {
        val result = helper.suggest("sun")
        assertTrue(result.any { it == "☀️" || it == "🌞" })
    }

    @Test
    fun `food keyword pizza returns pizza emoji`() {
        val result = helper.suggest("pizza")
        assertTrue(result.contains("🍕"))
    }

    @Test
    fun `fire keyword returns fire emoji`() {
        val result = helper.suggest("fire")
        assertTrue(result.contains("🔥"))
    }

    @Test
    fun `sad keyword returns sad emoji`() {
        val result = helper.suggest("sad")
        assertTrue(result.any { it == "😢" || it == "😔" })
    }

    @Test
    fun `all map entries have at least one emoji`() {
        EmojiSuggestionHelper.EMOJI_MAP.forEach { (key, emojis) ->
            assertTrue("Key '$key' has empty emoji list", emojis.isNotEmpty())
        }
    }

    @Test
    fun `result contains only strings from the emoji map`() {
        val key = "happy"
        val expected = EmojiSuggestionHelper.EMOJI_MAP[key]!!.take(3)
        val result = helper.suggest(key)
        assertEquals(expected, result)
    }
}
