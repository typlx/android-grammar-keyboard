package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class ToneOptionTest {

    @Test
    fun `all tones have non-blank display labels`() {
        ToneOption.entries.forEach { tone ->
            assertTrue("${tone.name} has blank displayLabel", tone.displayLabel.isNotBlank())
        }
    }

    @Test
    fun `all tones have non-blank system prompts`() {
        ToneOption.entries.forEach { tone ->
            assertTrue("${tone.name} has blank systemPrompt", tone.systemPrompt.isNotBlank())
        }
    }

    @Test
    fun `all system prompts contain rewrite instruction`() {
        ToneOption.entries.forEach { tone ->
            assertTrue(
                "${tone.name} system prompt does not start with 'Rewrite'",
                tone.systemPrompt.startsWith("Rewrite"),
            )
        }
    }

    @Test
    fun `all system prompts instruct return-only behaviour`() {
        ToneOption.entries.forEach { tone ->
            assertTrue(
                "${tone.name} system prompt missing 'Return only the rewritten text'",
                tone.systemPrompt.contains("Return only the rewritten text"),
            )
        }
    }

    @Test
    fun `five tones are defined`() {
        assertEquals(5, ToneOption.entries.size)
    }

    @Test
    fun `professional tone display label is Professional`() {
        assertEquals("Professional", ToneOption.PROFESSIONAL.displayLabel)
    }

    @Test
    fun `casual tone display label is Casual`() {
        assertEquals("Casual", ToneOption.CASUAL.displayLabel)
    }

    @Test
    fun `formal tone display label is Formal`() {
        assertEquals("Formal", ToneOption.FORMAL.displayLabel)
    }

    @Test
    fun `friendly tone display label is Friendly`() {
        assertEquals("Friendly", ToneOption.FRIENDLY.displayLabel)
    }

    @Test
    fun `concise tone display label is Concise`() {
        assertEquals("Concise", ToneOption.CONCISE.displayLabel)
    }

    @Test
    fun `all display labels are unique`() {
        val labels = ToneOption.entries.map { it.displayLabel }
        assertEquals("Duplicate display labels found", labels.size, labels.toSet().size)
    }

    @Test
    fun `all system prompts are unique`() {
        val prompts = ToneOption.entries.map { it.systemPrompt }
        assertEquals("Duplicate system prompts found", prompts.size, prompts.toSet().size)
    }
}
