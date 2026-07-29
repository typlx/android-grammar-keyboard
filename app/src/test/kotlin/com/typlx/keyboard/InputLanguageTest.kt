package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class InputLanguageTest {

    @Test
    fun `ALL contains exactly six languages`() {
        assertEquals(6, InputLanguage.ALL.size)
    }

    @Test
    fun `ENGLISH is the first entry`() {
        assertEquals(InputLanguage.ENGLISH, InputLanguage.ALL.first())
    }

    @Test
    fun `each language has a unique two-letter code`() {
        val codes = InputLanguage.ALL.map { it.code }
        assertEquals(codes.distinct().size, codes.size)
        codes.forEach { assertEquals("Code must be 2 chars: $it", 2, it.length) }
    }

    @Test
    fun `each language maps to a valid layout id`() {
        InputLanguage.ALL.forEach { lang ->
            assertNotNull(layoutById(lang.defaultLayoutId))
        }
    }

    @Test
    fun `UKRAINIAN uses CYRILLIC layout`() {
        assertEquals(LayoutId.CYRILLIC, InputLanguage.UKRAINIAN.defaultLayoutId)
    }

    @Test
    fun `FRENCH uses AZERTY layout`() {
        assertEquals(LayoutId.AZERTY, InputLanguage.FRENCH.defaultLayoutId)
    }

    @Test
    fun `GERMAN uses QWERTZ layout`() {
        assertEquals(LayoutId.QWERTZ, InputLanguage.GERMAN.defaultLayoutId)
    }

    @Test
    fun `ENGLISH, SPANISH and PORTUGUESE use QWERTY layout`() {
        assertEquals(LayoutId.QWERTY, InputLanguage.ENGLISH.defaultLayoutId)
        assertEquals(LayoutId.QWERTY, InputLanguage.SPANISH.defaultLayoutId)
        assertEquals(LayoutId.QWERTY, InputLanguage.PORTUGUESE.defaultLayoutId)
    }

    @Test
    fun `PORTUGUESE has PT code and Latin script`() {
        assertEquals("PT", InputLanguage.PORTUGUESE.code)
        assertEquals(ScriptFamily.LATIN, InputLanguage.PORTUGUESE.scriptFamily)
    }

    @Test
    fun `UKRAINIAN has Cyrillic script`() {
        assertEquals(ScriptFamily.CYRILLIC, InputLanguage.UKRAINIAN.scriptFamily)
    }

    @Test
    fun `all Latin-script languages have LATIN script family`() {
        val latinLanguages = listOf(
            InputLanguage.ENGLISH, InputLanguage.SPANISH,
            InputLanguage.FRENCH, InputLanguage.GERMAN, InputLanguage.PORTUGUESE
        )
        latinLanguages.forEach { lang ->
            assertEquals("${lang.name} should be LATIN", ScriptFamily.LATIN, lang.scriptFamily)
        }
    }

    @Test
    fun `fromCode finds language by code`() {
        assertEquals(InputLanguage.ENGLISH, InputLanguage.fromCode("EN"))
        assertEquals(InputLanguage.UKRAINIAN, InputLanguage.fromCode("UK"))
        assertEquals(InputLanguage.FRENCH, InputLanguage.fromCode("FR"))
        assertNull(InputLanguage.fromCode("XX"))
    }

    @Test
    fun `fromName finds language by enum name`() {
        assertEquals(InputLanguage.ENGLISH, InputLanguage.fromName("ENGLISH"))
        assertEquals(InputLanguage.GERMAN, InputLanguage.fromName("GERMAN"))
        assertNull(InputLanguage.fromName("NONEXISTENT"))
    }

    @Test
    fun `FREE_TIER_LIMIT is 2`() {
        assertEquals(2, InputLanguage.FREE_TIER_LIMIT)
    }

    @Test
    fun `each language has a non-blank locale tag`() {
        InputLanguage.ALL.forEach { lang ->
            assertTrue("Locale tag blank for ${lang.name}", lang.localeTag.isNotBlank())
        }
    }

    // --- nextLanguage (switchToNextLanguage logic) ---

    @Test
    fun `nextLanguage returns current when only one language enabled`() {
        assertEquals(
            InputLanguage.ENGLISH,
            InputLanguage.nextLanguage(InputLanguage.ENGLISH, listOf(InputLanguage.ENGLISH))
        )
    }

    @Test
    fun `nextLanguage cycles forward through two enabled languages`() {
        val enabled = listOf(InputLanguage.ENGLISH, InputLanguage.UKRAINIAN)
        assertEquals(InputLanguage.UKRAINIAN, InputLanguage.nextLanguage(InputLanguage.ENGLISH, enabled))
        assertEquals(InputLanguage.ENGLISH, InputLanguage.nextLanguage(InputLanguage.UKRAINIAN, enabled))
    }

    @Test
    fun `nextLanguage cycles through all enabled languages`() {
        val all = InputLanguage.ALL
        var current = all[0]
        for (i in 1..all.size) {
            current = InputLanguage.nextLanguage(current, all)
        }
        assertEquals("Full cycle should return to start", all[0], current)
    }

    @Test
    fun `nextLanguage wraps around from last to first`() {
        val enabled = listOf(InputLanguage.ENGLISH, InputLanguage.SPANISH, InputLanguage.FRENCH)
        assertEquals(InputLanguage.ENGLISH, InputLanguage.nextLanguage(InputLanguage.FRENCH, enabled))
    }

    @Test
    fun `nextLanguage handles current not in list by defaulting to first`() {
        val enabled = listOf(InputLanguage.SPANISH, InputLanguage.FRENCH)
        // ENGLISH is not in the enabled list — should wrap from index 0 to index 1
        val next = InputLanguage.nextLanguage(InputLanguage.ENGLISH, enabled)
        assertEquals(InputLanguage.FRENCH, next)
    }

    @Test
    fun `nextLanguage returns current when enabled list is empty`() {
        assertEquals(
            InputLanguage.ENGLISH,
            InputLanguage.nextLanguage(InputLanguage.ENGLISH, emptyList())
        )
    }

    // --- switchToLanguage side-effects (layout mapping) ---

    @Test
    fun `switching to UKRAINIAN uses CYRILLIC layout`() {
        assertEquals(LayoutId.CYRILLIC, layoutById(InputLanguage.UKRAINIAN.defaultLayoutId).id)
    }

    @Test
    fun `switching to FRENCH uses AZERTY layout`() {
        assertEquals(LayoutId.AZERTY, layoutById(InputLanguage.FRENCH.defaultLayoutId).id)
    }

    @Test
    fun `switching to GERMAN uses QWERTZ layout`() {
        assertEquals(LayoutId.QWERTZ, layoutById(InputLanguage.GERMAN.defaultLayoutId).id)
    }

    @Test
    fun `switching to ENGLISH or SPANISH uses QWERTY layout`() {
        assertEquals(LayoutId.QWERTY, layoutById(InputLanguage.ENGLISH.defaultLayoutId).id)
        assertEquals(LayoutId.QWERTY, layoutById(InputLanguage.SPANISH.defaultLayoutId).id)
    }
}
