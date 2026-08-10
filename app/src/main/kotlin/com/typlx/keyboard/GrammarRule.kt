package com.typlx.keyboard

enum class GrammarRule(val label: String, val description: String, val settingsDescription: String) {
    SPELLING(
        label = "Spelling",
        description = "spelling errors",
        settingsDescription = "Fix misspelled words",
    ),
    PUNCTUATION(
        label = "Punctuation",
        description = "punctuation errors",
        settingsDescription = "Fix missing or wrong punctuation",
    ),
    CAPITALIZATION(
        label = "Capitalization",
        description = "capitalization errors",
        settingsDescription = "Fix improper capitalization",
    ),
    GRAMMAR(
        label = "Grammar",
        description = "grammatical errors",
        settingsDescription = "Fix sentence structure and word agreement",
    );

    companion object {
        val ALL: Set<GrammarRule> = entries.toSet()
    }
}
