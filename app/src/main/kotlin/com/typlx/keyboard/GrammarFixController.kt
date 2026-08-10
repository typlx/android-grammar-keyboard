package com.typlx.keyboard

import android.view.inputmethod.InputConnection

class GrammarFixController(
    private val grammarService: GrammarService,
    private val wordListProvider: () -> PersonalWordList,
) {
    constructor(service: GrammarService, wordList: PersonalWordList) : this(service, { wordList })

    data class FixResult(val original: String, val fixed: String)

    /**
     * Calls the grammar API on the current text and replaces it via [ic].
     *
     * When [hasSelection] is true, operates on the selected text only (using
     * getSelectedText + commitText, which automatically replaces the selection).
     * When false, falls back to all text before the cursor.
     *
     * When [enabledRules] is empty, no correction is applied and Success(null) is returned.
     *
     * Returns Success(FixResult) when text was replaced, Success(null) when replacement
     * was suppressed or skipped, or Failure(GrammarServiceException) on any error.
     */
    suspend fun fix(
        ic: InputConnection,
        apiUrl: String,
        model: String,
        token: String,
        systemPromptSuffix: String = "",
        hasSelection: Boolean = false,
        language: InputLanguage? = null,
        enabledRules: Set<GrammarRule> = GrammarRule.ALL,
    ): Result<FixResult?> = runCatching {
        if (enabledRules.isEmpty()) return@runCatching null
        val personalWordList = wordListProvider()
        val selectedText = if (hasSelection) ic.getSelectedText(0)?.toString() else null
        val isRealSelection = !selectedText.isNullOrBlank()
        val text = if (isRealSelection) selectedText!! else ic.getTextBeforeCursor(5000, 0)?.toString()
        if (text.isNullOrBlank()) throw GrammarServiceException("No text found")
        val systemPrompt = GrammarService.buildSystemPrompt(language, systemPromptSuffix, enabledRules)
        val fixed = grammarService.fixGrammar(apiUrl, model, token, text, systemPrompt = systemPrompt)
        if (personalWordList.shouldSuppressCorrection(text, fixed)) return@runCatching null
        if (isRealSelection) {
            ic.commitText(fixed, 1)
        } else {
            ic.deleteSurroundingText(text.length, 0)
            ic.commitText(fixed, 1)
        }
        FixResult(original = text, fixed = fixed)
    }
}
