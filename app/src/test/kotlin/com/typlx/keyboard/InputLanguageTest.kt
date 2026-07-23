package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class InputLanguageTest {

    @Test
    fun `ALL contains exactly five languages`() {
        assertEquals(5, InputLanguage.ALL.size)
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
    fun `ENGLISH and SPANISH use QWERTY layout`() {
        assertEquals(LayoutId.QWERTY, InputLanguage.ENGLISH.defaultLayoutId)
        assertEquals(LayoutId.QWERTY, InputLanguage.SPANISH.defaultLayoutId)
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
}
