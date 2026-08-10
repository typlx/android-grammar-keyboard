package com.typlx.keyboard

import android.view.inputmethod.InputConnection

class AutoSuggestController(
    private val grammarService: GrammarService,
    private val wordListProvider: () -> PersonalWordList,
) {
    constructor(service: GrammarService, wordList: PersonalWordList) : this(service, { wordList })

    /**
     * Reads up to 5000 chars from [ic] and calls the grammar API.
     * Returns [SuggestionState.Available] when a meaningful correction exists,
     * [SuggestionState.Idle] for empty/unchanged text, any API error, or when [enabledRules] is empty.
     */
    suspend fun suggest(
        ic: InputConnection,
        apiUrl: String,
        model: String,
        token: String,
        systemPromptSuffix: String = "",
        language: InputLanguage? = null,
        enabledRules: Set<GrammarRule> = GrammarRule.ALL,
    ): SuggestionState {
        if (enabledRules.isEmpty()) return SuggestionState.Idle
        val personalWordList = wordListProvider()
        val text = ic.getTextBeforeCursor(5000, 0)?.toString()
        if (text.isNullOrBlank()) return SuggestionState.Idle
        return try {
            val systemPrompt = GrammarService.buildSystemPrompt(language, systemPromptSuffix, enabledRules)
            val fixed = grammarService.fixGrammar(apiUrl, model, token, text, systemPrompt = systemPrompt)
            if (fixed != text && !personalWordList.shouldSuppressCorrection(text, fixed))
                SuggestionState.Available(text, fixed, diffWords(text, fixed))
            else SuggestionState.Idle
        } catch (_: GrammarServiceException) {
            SuggestionState.Idle
        }
    }
}
