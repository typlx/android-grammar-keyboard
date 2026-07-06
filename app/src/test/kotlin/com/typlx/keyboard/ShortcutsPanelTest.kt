package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/** Tests for the shortcuts panel data layer used by ShortcutsPanel composable. */
class ShortcutsPanelTest {

    private lateinit var manager: TextShortcutsManager

    @Before
    fun setUp() {
        manager = TextShortcutsManager()
    }

    @Test
    fun `panel empty state when no shortcuts`() {
        assertTrue(manager.getAll().isEmpty())
    }

    @Test
    fun `panel shows all shortcuts after adding`() {
        manager.add("ty", "Thank you!")
        manager.add("np", "No problem!")
        manager.add("hbu", "How about you?")
        assertEquals(3, manager.getAll().size)
    }

    @Test
    fun `panel shortcuts are sorted alphabetically`() {
        manager.add("zzz", "Last")
        manager.add("aaa", "First")
        manager.add("mmm", "Middle")
        val keys = manager.getAll().map { it.shortcut }
        assertEquals(listOf("aaa", "mmm", "zzz"), keys)
    }

    @Test
    fun `expansion preview truncates at 60 chars`() {
        val longExpansion = "A".repeat(80)
        manager.add("long", longExpansion)
        val shortcut = manager.getAll().first()
        val preview = if (shortcut.expansion.length > 60) shortcut.expansion.take(60) + "…" else shortcut.expansion
        assertEquals(61, preview.length) // 60 chars + ellipsis
        assertTrue(preview.endsWith("…"))
    }

    @Test
    fun `expansion preview does not truncate short text`() {
        manager.add("hi", "Hello!")
        val shortcut = manager.getAll().first()
        val preview = if (shortcut.expansion.length > 60) shortcut.expansion.take(60) + "…" else shortcut.expansion
        assertEquals("Hello!", preview)
    }

    @Test
    fun `expansion exactly 60 chars is not truncated`() {
        val exactly60 = "X".repeat(60)
        manager.add("x60", exactly60)
        val shortcut = manager.getAll().first()
        val preview = if (shortcut.expansion.length > 60) shortcut.expansion.take(60) + "…" else shortcut.expansion
        assertEquals(exactly60, preview)
        assertFalse(preview.endsWith("…"))
    }

    @Test
    fun `insertion returns correct expansion for shortcut`() {
        manager.add("brb", "Be right back!")
        val inserted = mutableListOf<String>()
        val onInsert: (String) -> Unit = { inserted.add(it) }

        val shortcut = manager.getAll().first { it.shortcut == "brb" }
        onInsert(shortcut.expansion)

        assertEquals(1, inserted.size)
        assertEquals("Be right back!", inserted[0])
    }

    @Test
    fun `reload clears and re-populates from json`() {
        manager.add("omw", "On my way!")
        val json = manager.toJson()
        val fresh = TextShortcutsManager()
        fresh.loadFromJson(json)
        assertEquals(1, fresh.getAll().size)
        assertEquals("omw", fresh.getAll()[0].shortcut)
    }

    @Test
    fun `defaults loaded when no saved shortcuts`() {
        val defaults = TextShortcutsManager.defaults()
        defaults.forEach { manager.add(it.shortcut, it.expansion) }
        val all = manager.getAll()
        assertTrue(all.isNotEmpty())
        assertTrue(all.any { it.shortcut == "brb" })
        assertTrue(all.any { it.shortcut == "omw" })
    }
}
