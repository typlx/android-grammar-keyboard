package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class EmojiRecentsTest {

    @Test
    fun `initial recents is empty`() {
        val recents = EmojiRecents()
        assertTrue(recents.recents.isEmpty())
    }

    @Test
    fun `add single emoji appears at front`() {
        val recents = EmojiRecents()
        recents.add("😀")
        assertEquals(listOf("😀"), recents.recents)
    }

    @Test
    fun `add multiple emojis most recent is first`() {
        val recents = EmojiRecents()
        recents.add("😀")
        recents.add("❤️")
        recents.add("👍")
        assertEquals(listOf("👍", "❤️", "😀"), recents.recents)
    }

    @Test
    fun `adding duplicate moves it to front`() {
        val recents = EmojiRecents()
        recents.add("😀")
        recents.add("❤️")
        recents.add("😀")
        assertEquals(listOf("😀", "❤️"), recents.recents)
    }

    @Test
    fun `exceeding maxSize drops oldest`() {
        val recents = EmojiRecents(maxSize = 3)
        recents.add("😀")
        recents.add("❤️")
        recents.add("👍")
        recents.add("🎉")
        assertEquals(listOf("🎉", "👍", "❤️"), recents.recents)
        assertEquals(3, recents.recents.size)
    }

    @Test
    fun `toJson produces valid JSON array`() {
        val recents = EmojiRecents()
        recents.add("😀")
        recents.add("❤️")
        val json = recents.toJson()
        assertTrue(json.startsWith("["))
        assertTrue(json.endsWith("]"))
        assertTrue(json.contains("❤️"))
        assertTrue(json.contains("😀"))
    }

    @Test
    fun `loadFromJson restores recents`() {
        val original = EmojiRecents()
        original.add("😀")
        original.add("❤️")
        original.add("👍")
        val json = original.toJson()

        val restored = EmojiRecents()
        restored.loadFromJson(json)
        assertEquals(original.recents, restored.recents)
    }

    @Test
    fun `loadFromJson with malformed JSON starts fresh`() {
        val recents = EmojiRecents()
        recents.add("😀")
        recents.loadFromJson("not-valid-json{{")
        assertTrue(recents.recents.isEmpty())
    }

    @Test
    fun `loadFromJson respects maxSize`() {
        val big = EmojiRecents(maxSize = 100)
        repeat(10) { big.add("emoji$it") }
        val json = big.toJson()

        val small = EmojiRecents(maxSize = 3)
        small.loadFromJson(json)
        assertEquals(3, small.recents.size)
    }

    @Test
    fun `add after loadFromJson prepends to restored list`() {
        val recents = EmojiRecents()
        recents.loadFromJson("""["😀","❤️"]""")
        recents.add("🎉")
        assertEquals(listOf("🎉", "😀", "❤️"), recents.recents)
    }

    @Test
    fun `empty json string starts fresh`() {
        val recents = EmojiRecents()
        recents.loadFromJson("")
        assertTrue(recents.recents.isEmpty())
    }
}
