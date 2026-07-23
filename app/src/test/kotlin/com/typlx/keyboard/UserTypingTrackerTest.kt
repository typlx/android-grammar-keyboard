package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UserTypingTrackerTest {

    private lateinit var tracker: UserTypingTracker

    @Before
    fun setUp() {
        tracker = UserTypingTracker(maxTracked = 20)
    }

    // --- recordWord ---

    @Test
    fun `recordWord normalises word to lowercase`() {
        tracker.recordWord("Hello")
        assertEquals(1, tracker.getCount("hello"))
        assertEquals(1, tracker.getCount("Hello"))
    }

    @Test
    fun `recordWord increments count on repeated commits`() {
        repeat(5) { tracker.recordWord("kotlin") }
        assertEquals(5, tracker.getCount("kotlin"))
    }

    @Test
    fun `recordWord ignores single-char words`() {
        tracker.recordWord("a")
        assertEquals(0, tracker.size)
    }

    @Test
    fun `recordWord ignores digit-only strings`() {
        tracker.recordWord("42")
        tracker.recordWord("2024")
        assertEquals(0, tracker.size)
    }

    @Test
    fun `recordWord ignores blank or empty input`() {
        tracker.recordWord("")
        tracker.recordWord("  ")
        assertEquals(0, tracker.size)
    }

    @Test
    fun `recordWord tracks multiple distinct words independently`() {
        tracker.recordWord("kotlin")
        tracker.recordWord("kotlin")
        tracker.recordWord("compose")
        assertEquals(2, tracker.getCount("kotlin"))
        assertEquals(1, tracker.getCount("compose"))
    }

    @Test
    fun `recordWord respects maxTracked limit for new words`() {
        val small = UserTypingTracker(maxTracked = 3)
        small.recordWord("alpha")
        small.recordWord("beta")
        small.recordWord("gamma")
        small.recordWord("delta") // should be silently dropped
        assertEquals(3, small.size)
        assertEquals(0, small.getCount("delta"))
    }

    @Test
    fun `recordWord still increments existing word when at maxTracked`() {
        val small = UserTypingTracker(maxTracked = 2)
        small.recordWord("alpha")
        small.recordWord("beta")
        small.recordWord("alpha") // existing word — should still increment
        assertEquals(2, small.getCount("alpha"))
    }

    // --- getLearnedWords ---

    @Test
    fun `getLearnedWords excludes words below minCount threshold`() {
        tracker.recordWord("rare")
        tracker.recordWord("rare")
        assertTrue(tracker.getLearnedWords(minCount = 3).isEmpty())
    }

    @Test
    fun `getLearnedWords includes words at or above threshold`() {
        repeat(3) { tracker.recordWord("kotlin") }
        val learned = tracker.getLearnedWords(minCount = 3)
        assertEquals(listOf("kotlin"), learned)
    }

    @Test
    fun `getLearnedWords sorts by descending frequency`() {
        repeat(5) { tracker.recordWord("kotlin") }
        repeat(3) { tracker.recordWord("compose") }
        repeat(7) { tracker.recordWord("android") }
        val learned = tracker.getLearnedWords(minCount = 3)
        assertEquals(listOf("android", "kotlin", "compose"), learned)
    }

    @Test
    fun `getLearnedWords caps result at maxWords`() {
        listOf("alpha", "beta", "gamma", "delta", "epsilon").forEach { word ->
            repeat(4) { tracker.recordWord(word) }
        }
        val learned = tracker.getLearnedWords(minCount = 3, maxWords = 3)
        assertEquals(3, learned.size)
    }

    @Test
    fun `getLearnedWords returns empty list when no words tracked`() {
        assertTrue(tracker.getLearnedWords().isEmpty())
    }

    // --- getCount ---

    @Test
    fun `getCount returns zero for untracked word`() {
        assertEquals(0, tracker.getCount("unknown"))
    }

    @Test
    fun `getCount is case-insensitive`() {
        tracker.recordWord("Kotlin")
        assertEquals(1, tracker.getCount("kotlin"))
        assertEquals(1, tracker.getCount("KOTLIN"))
    }

    // --- clear ---

    @Test
    fun `clear resets all counts and size`() {
        repeat(3) { tracker.recordWord("kotlin") }
        tracker.clear()
        assertEquals(0, tracker.size)
        assertEquals(0, tracker.getCount("kotlin"))
        assertTrue(tracker.getLearnedWords().isEmpty())
    }

    // --- toJson / loadFromJson ---

    @Test
    fun `toJson and loadFromJson round-trip preserves counts`() {
        repeat(5) { tracker.recordWord("kotlin") }
        repeat(2) { tracker.recordWord("compose") }
        val json = tracker.toJson()
        val restored = UserTypingTracker()
        restored.loadFromJson(json)
        assertEquals(5, restored.getCount("kotlin"))
        assertEquals(2, restored.getCount("compose"))
    }

    @Test
    fun `toJson produces empty object for empty tracker`() {
        assertEquals("{}", tracker.toJson())
    }

    @Test
    fun `loadFromJson handles malformed input gracefully`() {
        tracker.recordWord("existing")
        tracker.loadFromJson("not valid json")
        assertEquals(0, tracker.size)
    }

    @Test
    fun `loadFromJson handles empty object`() {
        tracker.recordWord("existing")
        tracker.loadFromJson("{}")
        assertEquals(0, tracker.size)
    }

    @Test
    fun `loadFromJson handles escaped characters in word keys`() {
        val withQuote = UserTypingTracker()
        withQuote.loadFromJson("""{"it's":3}""")
        // Pattern matches literal escaped quote in JSON
        val json = """{"it\"s":3}"""
        val tracker2 = UserTypingTracker()
        tracker2.loadFromJson(json)
        assertEquals(3, tracker2.getCount("it\"s"))
    }

    @Test
    fun `loadFromJson respects maxTracked limit`() {
        val small = UserTypingTracker(maxTracked = 2)
        // JSON with 3 words — only 2 should be loaded
        small.loadFromJson("""{"alpha":1,"beta":2,"gamma":3}""")
        assertEquals(2, small.size)
    }
}
