package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class GrammarRuleTest {

    @Test
    fun `ALL set contains all four rules`() {
        assertEquals(4, GrammarRule.ALL.size)
        assertTrue(GrammarRule.ALL.containsAll(GrammarRule.entries))
    }

    @Test
    fun `buildSystemPrompt with ALL rules uses default prompt`() {
        val prompt = GrammarService.buildSystemPrompt(enabledRules = GrammarRule.ALL)
        assertEquals(GrammarService.SYSTEM_PROMPT, prompt)
    }

    @Test
    fun `buildSystemPrompt with ALL rules and suffix appends suffix`() {
        val prompt = GrammarService.buildSystemPrompt(suffix = "Be concise.", enabledRules = GrammarRule.ALL)
        assertTrue(prompt.endsWith("Be concise."))
        assertTrue(prompt.startsWith(GrammarService.SYSTEM_PROMPT))
    }

    @Test
    fun `buildSystemPrompt with empty rules still returns a prompt`() {
        val prompt = GrammarService.buildSystemPrompt(enabledRules = emptySet())
        assertFalse(prompt.isBlank())
    }

    @Test
    fun `buildSystemPrompt with single rule names that rule explicitly`() {
        val prompt = GrammarService.buildSystemPrompt(enabledRules = setOf(GrammarRule.SPELLING))
        assertTrue(prompt.contains("spelling errors"))
        assertFalse(prompt.lowercase().contains("punctuation"))
        assertFalse(prompt.lowercase().contains("capitalization"))
        assertFalse(prompt.lowercase().contains("grammatical"))
    }

    @Test
    fun `buildSystemPrompt with two rules names both rules`() {
        val prompt = GrammarService.buildSystemPrompt(
            enabledRules = setOf(GrammarRule.SPELLING, GrammarRule.PUNCTUATION),
        )
        assertTrue(prompt.contains("spelling errors"))
        assertTrue(prompt.contains("punctuation errors"))
        assertFalse(prompt.lowercase().contains("capitalization"))
    }

    @Test
    fun `buildSystemPrompt subset prompt contains fix only prefix`() {
        val prompt = GrammarService.buildSystemPrompt(
            enabledRules = setOf(GrammarRule.GRAMMAR),
        )
        assertTrue(prompt.startsWith("Fix only"))
    }

    @Test
    fun `buildSystemPrompt subset prompt with suffix appends suffix`() {
        val prompt = GrammarService.buildSystemPrompt(
            suffix = "Use simple words.",
            enabledRules = setOf(GrammarRule.CAPITALIZATION),
        )
        assertTrue(prompt.contains("capitalization errors"))
        assertTrue(prompt.endsWith("Use simple words."))
    }

    @Test
    fun `buildSystemPrompt no-arg variant returns default prompt`() {
        val prompt = GrammarService.buildSystemPrompt()
        assertEquals(GrammarService.SYSTEM_PROMPT, prompt)
    }
}
