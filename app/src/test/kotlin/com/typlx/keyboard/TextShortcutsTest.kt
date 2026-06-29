package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TextShortcutsTest {

    private lateinit var manager: TextShortcutsManager

    @Before
    fun setUp() {
        manager = TextShortcutsManager()
    }

    @Test
    fun `expand returns null when no shortcuts are defined`() {
        assertNull(manager.expand("omw"))
    }

    @Test
    fun `add and expand basic shortcut`() {
        manager.add("omw", "On my way!")
        assertEquals("On my way!", manager.expand("omw"))
    }

    @Test
    fun `expand is case-insensitive`() {
        manager.add("omw", "On my way!")
        assertEquals("On my way!", manager.expand("OMW"))
        assertEquals("On my way!", manager.expand("Omw"))
    }

    @Test
    fun `add lowercases shortcut key`() {
        manager.add("THX", "Thanks!")
        assertEquals("Thanks!", manager.expand("thx"))
        assertEquals(1, manager.size)
    }

    @Test
    fun `add returns false for duplicate shortcut`() {
        assertTrue(manager.add("omw", "On my way!"))
        assertFalse(manager.add("omw", "Other expansion"))
        assertEquals(1, manager.size)
    }

    @Test
    fun `add returns false for blank shortcut`() {
        assertFalse(manager.add("", "expansion"))
        assertFalse(manager.add("   ", "expansion"))
    }

    @Test
    fun `add returns false for blank expansion`() {
        assertFalse(manager.add("omw", ""))
        assertFalse(manager.add("omw", "   "))
    }

    @Test
    fun `remove deletes a shortcut`() {
        manager.add("omw", "On my way!")
        manager.remove("omw")
        assertNull(manager.expand("omw"))
        assertEquals(0, manager.size)
    }

    @Test
    fun `remove is case-insensitive`() {
        manager.add("omw", "On my way!")
        manager.remove("OMW")
        assertNull(manager.expand("omw"))
    }

    @Test
    fun `getAll returns shortcuts sorted by key`() {
        manager.add("zzz", "Zzz")
        manager.add("aaa", "Aaa")
        manager.add("mmm", "Mmm")
        val keys = manager.getAll().map { it.shortcut }
        assertEquals(listOf("aaa", "mmm", "zzz"), keys)
    }

    @Test
    fun `json round-trip preserves all shortcuts`() {
        manager.add("omw", "On my way!")
        manager.add("ttyl", "Talk to you later!")
        manager.add("brb", "Be right back!")
        val json = manager.toJson()
        val restored = TextShortcutsManager()
        restored.loadFromJson(json)
        assertEquals(3, restored.size)
        assertEquals("On my way!", restored.expand("omw"))
        assertEquals("Talk to you later!", restored.expand("ttyl"))
        assertEquals("Be right back!", restored.expand("brb"))
    }

    @Test
    fun `json round-trip with special characters`() {
        manager.add("q1", "Say \"hello\" & goodbye")
        val json = manager.toJson()
        val restored = TextShortcutsManager()
        restored.loadFromJson(json)
        assertEquals("Say \"hello\" & goodbye", restored.expand("q1"))
    }

    @Test
    fun `loadFromJson empty array clears shortcuts`() {
        manager.add("omw", "On my way!")
        manager.loadFromJson("[]")
        assertEquals(0, manager.size)
    }

    @Test
    fun `defaults contains expected shortcuts`() {
        val defaults = TextShortcutsManager.defaults()
        assertTrue(defaults.any { it.shortcut == "omw" })
        assertTrue(defaults.any { it.shortcut == "brb" })
        assertTrue(defaults.any { it.shortcut == "thx" })
    }

    @Test
    fun `maxSize prevents adding beyond capacity`() {
        val small = TextShortcutsManager(maxSize = 2)
        assertTrue(small.add("a", "Alpha"))
        assertTrue(small.add("b", "Beta"))
        assertFalse(small.add("c", "Gamma"))
        assertEquals(2, small.size)
    }
}
