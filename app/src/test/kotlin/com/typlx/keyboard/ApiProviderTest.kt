package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class ApiProviderTest {

    @Test
    fun `OPENAI has correct url and model`() {
        assertEquals("https://api.openai.com", ApiProvider.OPENAI.apiUrl)
        assertEquals("gpt-4o-mini", ApiProvider.OPENAI.defaultModel)
    }

    @Test
    fun `GROQ has correct url and model`() {
        assertEquals("https://api.groq.com/openai", ApiProvider.GROQ.apiUrl)
        assertEquals("llama-3.1-8b-instant", ApiProvider.GROQ.defaultModel)
    }

    @Test
    fun `OLLAMA has correct url and model`() {
        assertEquals("http://localhost:11434", ApiProvider.OLLAMA.apiUrl)
        assertEquals("llama3.2", ApiProvider.OLLAMA.defaultModel)
    }

    @Test
    fun `CUSTOM has empty url and model`() {
        assertEquals("", ApiProvider.CUSTOM.apiUrl)
        assertEquals("", ApiProvider.CUSTOM.defaultModel)
    }

    @Test
    fun `inferFromUrl returns OPENAI for exact OpenAI URL`() {
        assertEquals(ApiProvider.OPENAI, ApiProvider.inferFromUrl("https://api.openai.com"))
    }

    @Test
    fun `inferFromUrl returns OPENAI for URL with trailing slash`() {
        assertEquals(ApiProvider.OPENAI, ApiProvider.inferFromUrl("https://api.openai.com/"))
    }

    @Test
    fun `inferFromUrl returns OPENAI for URL with v1 suffix`() {
        assertEquals(ApiProvider.OPENAI, ApiProvider.inferFromUrl("https://api.openai.com/v1"))
    }

    @Test
    fun `inferFromUrl returns GROQ for Groq URL`() {
        assertEquals(ApiProvider.GROQ, ApiProvider.inferFromUrl("https://api.groq.com/openai"))
    }

    @Test
    fun `inferFromUrl returns OLLAMA for local Ollama URL`() {
        assertEquals(ApiProvider.OLLAMA, ApiProvider.inferFromUrl("http://localhost:11434"))
    }

    @Test
    fun `inferFromUrl returns CUSTOM for unknown URL`() {
        assertEquals(ApiProvider.CUSTOM, ApiProvider.inferFromUrl("https://my-custom-server.example.com"))
    }

    @Test
    fun `inferFromUrl returns CUSTOM for empty string`() {
        assertEquals(ApiProvider.CUSTOM, ApiProvider.inferFromUrl(""))
    }

    @Test
    fun `all non-CUSTOM providers have non-empty displayName`() {
        ApiProvider.entries.filter { it != ApiProvider.CUSTOM }.forEach { provider ->
            assertTrue("${provider.name} displayName should not be blank", provider.displayName.isNotBlank())
        }
    }
}
