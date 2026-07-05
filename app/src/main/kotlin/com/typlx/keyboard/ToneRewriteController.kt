package com.typlx.keyboard

class ToneRewriteController(private val grammarService: GrammarService) {

    suspend fun rewriteText(
        text: String,
        tone: ToneOption,
        apiUrl: String,
        model: String,
        token: String,
    ): Result<String> = runCatching {
        grammarService.fixGrammar(
            apiUrl = apiUrl,
            model = model,
            token = token,
            text = text,
            systemPrompt = tone.systemPrompt,
        )
    }
}
