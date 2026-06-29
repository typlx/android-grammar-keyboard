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
    fun `add word returns true and stores it lowercased`() {
        assertTrue(list.add("Kotlin"))
        assertEquals(1, list.size)
        assertTrue(list.contains("kotlin"))
        assertTrue(list.contains("Kotlin"))
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
}
