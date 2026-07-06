package com.typlx.keyboard

import android.view.inputmethod.InputConnection

class GrammarFixController(
    private val grammarService: GrammarService,
    private val personalWordList: PersonalWordList,
) {
    data class FixResult(val original: String, val fixed: String)

    /**
     * Reads up to 5000 chars from [ic], calls the grammar API, and if the text changed
     * and is not suppressed by the personal word list, replaces it via [ic].
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
    ): Result<FixResult?> = runCatching {
        val text = ic.getTextBeforeCursor(5000, 0)?.toString()
        if (text.isNullOrBlank()) throw GrammarServiceException("No text found")
        val fixed = grammarService.fixGrammar(apiUrl, model, token, text,
            systemPromptSuffix = systemPromptSuffix)
        if (personalWordList.shouldSuppressCorrection(text, fixed)) return@runCatching null
        ic.deleteSurroundingText(text.length, 0)
        ic.commitText(fixed, 1)
        FixResult(original = text, fixed = fixed)
    }
}
