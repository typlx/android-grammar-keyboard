package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PersonalWordListTest {

    private lateinit var list: PersonalWordList

    @Before
    fun setUp() {
        list = PersonalWordList(maxSize = 10)
    }

    @Test
    fun `initial state is empty`() {
        assertEquals(0, list.size)
        assertTrue(list.getAll().isEmpty())
    }

    @Test
    fun `add word returns true and is case-insensitively accessible`() {
        assertTrue(list.add("Kotlin"))
        assertEquals(1, list.size)
        assertTrue(list.contains("kotlin"))
        assertTrue(list.contains("Kotlin"))
    }

    @Test
    fun `getAll returns words with original casing`() {
        list.add("London")
        list.add("iPhone")
        list.add("coroutines")
        val all = list.getAll()
        assertTrue("London" in all)
        assertTrue("iPhone" in all)
        assertTrue("coroutines" in all)
    }

    @Test
    fun `add duplicate returns false`() {
        list.add("kotlin")
        assertFalse(list.add("kotlin"))
        assertFalse(list.add("Kotlin"))
        assertEquals(1, list.size)
    }

    @Test
    fun `add empty or blank returns false`() {
        assertFalse(list.add(""))
        assertFalse(list.add("   "))
        assertEquals(0, list.size)
    }

    @Test
    fun `remove deletes word case-insensitively`() {
        list.add("GPT-4")
        list.remove("gpt-4")
        assertFalse(list.contains("gpt-4"))
        assertEquals(0, list.size)
    }

    @Test
    fun `remove is no-op for non-existent word`() {
        list.add("coroutines")
        list.remove("nonexistent")
        assertEquals(1, list.size)
    }

    @Test
    fun `max size is enforced`() {
        val small = PersonalWordList(maxSize = 3)
        assertTrue(small.add("a"))
        assertTrue(small.add("b"))
        assertTrue(small.add("c"))
        assertFalse(small.add("d"))
        assertEquals(3, small.size)
    }

    @Test
    fun `getAll returns words sorted alphabetically`() {
        list.add("zeta")
        list.add("alpha")
        list.add("middle")
        assertEquals(listOf("alpha", "middle", "zeta"), list.getAll())
    }

    @Test
    fun `shouldSuppressCorrection when texts are identical`() {
        assertTrue(list.shouldSuppressCorrection("hello world", "hello world"))
    }

    @Test
    fun `shouldSuppressCorrection returns false with empty word list and different texts`() {
        assertFalse(list.shouldSuppressCorrection("teh cat", "the cat"))
    }

    @Test
    fun `shouldSuppressCorrection suppresses when only personal word differs`() {
        list.add("coroutines")
        // Grammar API "corrected" coroutines → routines (wrongly)
        assertTrue(list.shouldSuppressCorrection(
            "Kotlin coroutines are great",
            "Kotlin routines are great",
        ))
    }

    @Test
    fun `shouldSuppressCorrection does not suppress real grammar fix`() {
        list.add("Kotlin")
        // Real fix: "teh cat" → "the cat" — "cat"/"teh"/"the" not in word list
        assertFalse(list.shouldSuppressCorrection("teh cat sat", "the cat sat"))
    }

    @Test
    fun `shouldSuppressCorrection suppresses case-only difference in personal word`() {
        list.add("iphone")
        // API capitalised iPhone → Iphone
        assertTrue(list.shouldSuppressCorrection(
            "I bought an iPhone yesterday",
            "I bought an Iphone yesterday",
        ))
    }

    @Test
    fun `toJson and loadFromJson round-trip preserves words`() {
        list.add("Kotlin")
        list.add("GPT-4")
        list.add("coroutines")
        val json = list.toJson()
        val restored = PersonalWordList(maxSize = 10)
        restored.loadFromJson(json)
        assertEquals(list.getAll(), restored.getAll())
    }

    @Test
    fun `loadFromJson ignores malformed input`() {
        list.add("existing")
        list.loadFromJson("not valid json at all")
        assertEquals(0, list.size)
    }

    @Test
    fun `loadFromJson empty array clears words`() {
        list.add("word")
        list.loadFromJson("[]")
        assertEquals(0, list.size)
    }

    // --- export / import ---

    @Test
    fun `toExportText returns one word per line sorted alphabetically`() {
        list.add("zeta")
        list.add("alpha")
        list.add("middle")
        assertEquals("alpha\nmiddle\nzeta\n", list.toExportText())
    }

    @Test
    fun `toExportText returns empty string when list is empty`() {
        assertEquals("", list.toExportText())
    }

    @Test
    fun `importFromText adds words and returns count`() {
        val count = list.importFromText("kotlin\ncompose\nandroid\n")
        assertEquals(3, count)
        assertEquals(listOf("android", "compose", "kotlin"), list.getAll())
    }

    @Test
    fun `importFromText skips blank and whitespace-only lines`() {
        val count = list.importFromText("kotlin\n\n   \ncompose")
        assertEquals(2, count)
    }

    @Test
    fun `importFromText skips duplicate words`() {
        list.add("kotlin")
        val count = list.importFromText("kotlin\ncompose")
        assertEquals(1, count)
        assertEquals(2, list.size)
    }

    @Test
    fun `importFromText on empty string adds nothing`() {
        val count = list.importFromText("")
        assertEquals(0, count)
        assertEquals(0, list.size)
    }

    @Test
    fun `importFromText trims surrounding whitespace from each line`() {
        val count = list.importFromText("  kotlin  \n  compose  ")
        assertEquals(2, count)
        assertTrue(list.contains("kotlin"))
        assertTrue(list.contains("compose"))
    }

    @Test
    fun `toExportText and importFromText round-trip preserves words`() {
        list.add("Kotlin")
        list.add("GPT-4")
        list.add("compose")
        val exported = list.toExportText()

        val restored = PersonalWordList(maxSize = 10)
        restored.importFromText(exported)
        assertEquals(list.getAll(), restored.getAll())
    }

    // --- addChangedTokens ---

    @Test
    fun `addChangedTokens adds only the differing original tokens`() {
        val added = list.addChangedTokens("I wrk at home", "I work at home")
        assertTrue(added)
        assertTrue(list.contains("wrk"))
        assertFalse(list.contains("i"))
        assertFalse(list.contains("at"))
        assertFalse(list.contains("home"))
    }

    @Test
    fun `addChangedTokens returns false when token counts differ`() {
        val added = list.addChangedTokens("short", "much longer text here")
        assertFalse(added)
        assertEquals(0, list.size)
    }

    @Test
    fun `addChangedTokens returns false when texts are identical`() {
        val added = list.addChangedTokens("same text", "same text")
        assertFalse(added)
        assertEquals(0, list.size)
    }

    @Test
    fun `addChangedTokens after adding suppresses that correction`() {
        list.addChangedTokens("I wrk at Google", "I work at Google")
        assertTrue(list.shouldSuppressCorrection("I wrk at Google", "I work at Google"))
    }

    @Test
    fun `addChangedTokens strips trailing punctuation before adding`() {
        list.addChangedTokens("I like teh.", "I like the.")
        assertTrue(list.contains("teh"))
    }
}
