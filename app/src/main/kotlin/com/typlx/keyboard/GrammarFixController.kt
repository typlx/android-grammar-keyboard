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
     * Returns Success(FixResult) when text was replaced, Success(null) when replacement
     * was suppressed, or Failure(GrammarServiceException) on any error.
     */
    suspend fun fix(
        ic: InputConnection,
        apiUrl: String,
        model: String,
        token: String,
        systemPromptSuffix: String = "",
        hasSelection: Boolean = false,
        language: InputLanguage? = null,
    ): Result<FixResult?> = runCatching {
        val personalWordList = wordListProvider()
        val selectedText = if (hasSelection) ic.getSelectedText(0)?.toString() else null
        val isRealSelection = !selectedText.isNullOrBlank()
        val text = if (isRealSelection) selectedText!! else ic.getTextBeforeCursor(5000, 0)?.toString()
        if (text.isNullOrBlank()) throw GrammarServiceException("No text found")
        val fixed = grammarService.fixGrammar(apiUrl, model, token, text,
            systemPromptSuffix = systemPromptSuffix, language = language)
        if (personalWordList.shouldSuppressCorrection(text, fixed)) return@runCatching null
        if (isRealSelection) {
            // commitText replaces the current selection in-place
            ic.commitText(fixed, 1)
        } else {
            ic.deleteSurroundingText(text.length, 0)
            ic.commitText(fixed, 1)
        }
        FixResult(original = text, fixed = fixed)
    }
}
