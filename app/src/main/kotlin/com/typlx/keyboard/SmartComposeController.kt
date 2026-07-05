package com.typlx.keyboard

class SmartComposeController(private val grammarService: GrammarService) {

    /**
     * Sends [text] to the LLM asking it to continue naturally.
     * Returns [Result.success] with the continuation string, or [Result.failure]
     * wrapping a [GrammarServiceException] on any error.
     */
    suspend fun compose(
        text: String,
        apiUrl: String,
        model: String,
        token: String,
    ): Result<String> = runCatching {
        if (text.isBlank()) throw GrammarServiceException("No text to continue")
        grammarService.fixGrammar(
            apiUrl = apiUrl,
            model = model,
            token = token,
            text = text,
            systemPrompt = SYSTEM_PROMPT,
        )
    }

    companion object {
        internal const val SYSTEM_PROMPT =
            "Continue the following text naturally in the same language, tone, and style. " +
                "Return only the continuation text. Do not repeat any part of the original text."
    }
}
