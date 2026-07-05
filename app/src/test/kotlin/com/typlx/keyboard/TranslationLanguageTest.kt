package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class TranslationLanguageTest {

    @Test
    fun `all languages have non-blank display labels`() {
        TranslationLanguage.entries.forEach { lang ->
            assertTrue("${lang.name} has blank displayLabel", lang.displayLabel.isNotBlank())
        }
    }

    @Test
    fun `all languages have non-blank system prompts`() {
        TranslationLanguage.entries.forEach { lang ->
            assertTrue("${lang.name} has blank systemPrompt", lang.systemPrompt.isNotBlank())
        }
    }

    @Test
    fun `all system prompts contain translate instruction`() {
        TranslationLanguage.entries.forEach { lang ->
            assertTrue(
                "${lang.name} system prompt does not start with 'Translate'",
                lang.systemPrompt.startsWith("Translate"),
            )
        }
    }

    @Test
    fun `all system prompts instruct return-only behaviour`() {
        TranslationLanguage.entries.forEach { lang ->
            assertTrue(
                "${lang.name} system prompt missing 'Return only the translated text'",
                lang.systemPrompt.contains("Return only the translated text"),
            )
        }
    }

    @Test
    fun `twenty languages are defined`() {
        assertEquals(20, TranslationLanguage.entries.size)
    }

    @Test
    fun `english display label is English`() {
        assertEquals("English", TranslationLanguage.ENGLISH.displayLabel)
    }

    @Test
    fun `indonesian display label is Indonesian`() {
        assertEquals("Indonesian", TranslationLanguage.INDONESIAN.displayLabel)
    }

    @Test
    fun `vietnamese display label is Vietnamese`() {
        assertEquals("Vietnamese", TranslationLanguage.VIETNAMESE.displayLabel)
    }

    @Test
    fun `thai display label is Thai`() {
        assertEquals("Thai", TranslationLanguage.THAI.displayLabel)
    }

    @Test
    fun `ukrainian display label is Ukrainian`() {
        assertEquals("Ukrainian", TranslationLanguage.UKRAINIAN.displayLabel)
    }

    @Test
    fun `all display labels are unique`() {
        val labels = TranslationLanguage.entries.map { it.displayLabel }
        assertEquals("Duplicate display labels found", labels.size, labels.toSet().size)
    }

    @Test
    fun `all system prompts are unique`() {
        val prompts = TranslationLanguage.entries.map { it.systemPrompt }
        assertEquals("Duplicate system prompts found", prompts.size, prompts.toSet().size)
    }

    @Test
    fun `spanish display label is Spanish`() {
        assertEquals("Spanish", TranslationLanguage.SPANISH.displayLabel)
    }

    @Test
    fun `french display label is French`() {
        assertEquals("French", TranslationLanguage.FRENCH.displayLabel)
    }

    @Test
    fun `japanese system prompt targets Japanese`() {
        assertTrue(
            TranslationLanguage.JAPANESE.systemPrompt.contains("Japanese"),
        )
    }

    @Test
    fun `chinese display label is Chinese`() {
        assertEquals("Chinese", TranslationLanguage.CHINESE_SIMPLIFIED.displayLabel)
    }
}
