package com.typlx.keyboard

class TranslationController(private val grammarService: GrammarService) {

    suspend fun translateText(
        text: String,
        language: TranslationLanguage,
        apiUrl: String,
        model: String,
        token: String,
    ): Result<String> = runCatching {
        grammarService.fixGrammar(
            apiUrl = apiUrl,
            model = model,
            token = token,
            text = text,
            systemPrompt = language.systemPrompt,
        )
    }
}
