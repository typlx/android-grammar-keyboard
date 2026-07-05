package com.typlx.keyboard

enum class ApiProvider(
    val displayName: String,
    val apiUrl: String,
    val defaultModel: String,
) {
    OPENAI("OpenAI", "https://api.openai.com", "gpt-4o-mini"),
    GROQ("Groq", "https://api.groq.com/openai", "llama-3.1-8b-instant"),
    OLLAMA("Ollama (local)", "http://localhost:11434", "llama3.2"),
    CUSTOM("Custom", "", "");

    companion object {
        /**
         * Returns the provider whose apiUrl matches [url] (ignoring trailing slash and /v1 suffix),
         * or CUSTOM when no preset matches.
         */
        fun inferFromUrl(url: String): ApiProvider {
            val normalised = url.trimEnd('/').removeSuffix("/v1")
            return entries.firstOrNull { it != CUSTOM && it.apiUrl == normalised } ?: CUSTOM
        }
    }
}
