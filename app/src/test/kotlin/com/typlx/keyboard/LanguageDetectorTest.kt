package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class LanguageDetectorTest {

    private val allEnabled = InputLanguage.ALL
    private val enUk = listOf(InputLanguage.ENGLISH, InputLanguage.UKRAINIAN)

    // --- Not enough text ---

    @Test
    fun `returns null when text is too short`() {
        assertNull(LanguageDetector.detectLanguage("hi", InputLanguage.ENGLISH, allEnabled))
    }

    @Test
    fun `returns null when only one language is enabled`() {
        val text = "Hello world how are you doing"
        assertNull(LanguageDetector.detectLanguage(text, InputLanguage.ENGLISH, listOf(InputLanguage.ENGLISH)))
    }

    // --- Cyrillic detection ---

    @Test
    fun `detects Cyrillic script and suggests Ukrainian when enabled`() {
        val text = "Привіт як справи у вас все добре"
        val result = LanguageDetector.detectLanguage(text, InputLanguage.ENGLISH, enUk)
        assertEquals(InputLanguage.UKRAINIAN, result)
    }

    @Test
    fun `returns null for Cyrillic when Ukrainian is already active`() {
        val text = "Привіт як справи у вас все добре"
        val result = LanguageDetector.detectLanguage(text, InputLanguage.UKRAINIAN, enUk)
        assertNull(result)
    }

    @Test
    fun `returns null for Cyrillic text when Ukrainian is not enabled`() {
        val text = "Привіт як справи у вас все добре"
        val onlyLatin = listOf(InputLanguage.ENGLISH, InputLanguage.SPANISH)
        assertNull(LanguageDetector.detectLanguage(text, InputLanguage.ENGLISH, onlyLatin))
    }

    // --- Latin detection ---

    @Test
    fun `detects Latin script and suggests English when on Ukrainian`() {
        val text = "Hello how are you doing today friend"
        val result = LanguageDetector.detectLanguage(text, InputLanguage.UKRAINIAN, enUk)
        assertEquals(InputLanguage.ENGLISH, result)
    }

    @Test
    fun `returns null for Latin text when already on a Latin-script language`() {
        val text = "Hello world how are you doing today"
        assertNull(LanguageDetector.detectLanguage(text, InputLanguage.ENGLISH, allEnabled))
    }

    // --- Latin diacritics detection ---

    @Test
    fun `detects Latin script for heavily-accented French text`() {
        // é è ê à ç ù are Latin Extended — must exceed 70% threshold
        val text = "éléphant crème brûlée café résumé naïve"
        val result = LanguageDetector.detectLanguage(text, InputLanguage.UKRAINIAN, enUk)
        assertEquals(InputLanguage.ENGLISH, result)
    }

    @Test
    fun `detects Latin script for Portuguese text with diacritics`() {
        val text = "ação opinião comunicação nação coração paixão"
        val result = LanguageDetector.detectLanguage(text, InputLanguage.UKRAINIAN, enUk)
        assertEquals(InputLanguage.ENGLISH, result)
    }

    @Test
    fun `detects Latin script for German text with umlauts`() {
        val text = "Entschuldigung Straßenbahn Überraschung schöne Möglichkeit"
        val result = LanguageDetector.detectLanguage(text, InputLanguage.UKRAINIAN, enUk)
        assertEquals(InputLanguage.ENGLISH, result)
    }

    // --- Mixed / inconclusive ---

    @Test
    fun `returns null for mixed script text below confidence threshold`() {
        // ~50% Cyrillic, ~50% Latin — neither exceeds 70%
        val text = "Hello Привіт World Привіт Hello Привіт"
        assertNull(LanguageDetector.detectLanguage(text, InputLanguage.ENGLISH, enUk))
    }
}
