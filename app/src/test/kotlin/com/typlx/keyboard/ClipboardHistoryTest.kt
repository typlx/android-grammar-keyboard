package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ClipboardHistoryTest {

    private lateinit var history: ClipboardHistory

    @Before
    fun setUp() {
        history = ClipboardHistory(maxSize = 5)
    }

    @Test
    fun `initial state is empty`() {
        assertTrue(history.items.isEmpty())
    }

    @Test
    fun `add single item`() {
        history.add("hello")
        assertEquals(listOf("hello"), history.items)
    }

    @Test
    fun `add item moves to front and deduplicates`() {
        history.add("first")
        history.add("second")
        history.add("first")
        assertEquals(listOf("first", "second"), history.items)
    }

    @Test
    fun `add respects maxSize by evicting oldest`() {
        repeat(6) { i -> history.add("item$i") }
        assertEquals(5, history.items.size)
        assertEquals("item5", history.items.first())
        assertFalse(history.items.contains("item0"))
    }

    @Test
    fun `blank text is not added`() {
        history.add("")
        history.add("   ")
        history.add("\t")
        assertTrue(history.items.isEmpty())
    }

    @Test
    fun `text longer than MAX_ITEM_CHARS is truncated`() {
        val longText = "a".repeat(ClipboardHistory.MAX_ITEM_CHARS + 100)
        history.add(longText)
        assertEquals(ClipboardHistory.MAX_ITEM_CHARS, history.items.first().length)
    }

    @Test
    fun `clear removes all items`() {
        history.add("a")
        history.add("b")
        history.clear()
        assertTrue(history.items.isEmpty())
    }

    @Test
    fun `toJson and loadFromJson round-trip`() {
        history.add("hello world")
        history.add("line1\nline2")
        history.add("""has "quotes" and \backslash""")
        val json = history.toJson()
        val restored = ClipboardHistory(maxSize = 5)
        restored.loadFromJson(json)
        assertEquals(history.items, restored.items)
    }

    @Test
    fun `loadFromJson empty array`() {
        history.add("something")
        history.loadFromJson("[]")
        assertTrue(history.items.isEmpty())
    }

    @Test
    fun `loadFromJson ignores malformed input`() {
        history.loadFromJson("not json at all")
        assertTrue(history.items.isEmpty())
    }
}
