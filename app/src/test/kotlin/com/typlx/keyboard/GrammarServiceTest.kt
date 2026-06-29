package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class GrammarServiceTest {

    @Test
    fun `url construction handles base url without v1`() {
        val apiUrl = "https://api.openai.com"
        val normalised = apiUrl.trimEnd('/').removeSuffix("/v1")
        val url = "$normalised/v1/chat/completions"
        assertEquals("https://api.openai.com/v1/chat/completions", url)
    }

    @Test
    fun `url construction handles base url with v1 suffix`() {
        val apiUrl = "https://api.openai.com/v1"
        val normalised = apiUrl.trimEnd('/').removeSuffix("/v1")
        val url = "$normalised/v1/chat/completions"
        assertEquals("https://api.openai.com/v1/chat/completions", url)
    }

    @Test
    fun `url construction handles base url with trailing slash`() {
        val apiUrl = "https://api.openai.com/"
        val normalised = apiUrl.trimEnd('/').removeSuffix("/v1")
        val url = "$normalised/v1/chat/completions"
        assertEquals("https://api.openai.com/v1/chat/completions", url)
    }

    @Test
    fun `url construction handles local ollama url`() {
        val apiUrl = "http://10.0.2.2:11434"
        val normalised = apiUrl.trimEnd('/').removeSuffix("/v1")
        val url = "$normalised/v1/chat/completions"
        assertEquals("http://10.0.2.2:11434/v1/chat/completions", url)
    }

    @Test
    fun `httpErrorMessage 401 reports invalid token`() {
        assertTrue(httpErrorMessage(401).contains("401"))
    }

    @Test
    fun `httpErrorMessage 403 reports invalid token`() {
        assertTrue(httpErrorMessage(403).contains("403"))
    }

    @Test
    fun `httpErrorMessage 429 reports rate limit`() {
        assertTrue(httpErrorMessage(429).lowercase().contains("rate limit"))
    }

    @Test
    fun `httpErrorMessage 500 reports server error`() {
        assertTrue(httpErrorMessage(500).lowercase().contains("server error"))
    }

    @Test
    fun `httpErrorMessage 503 reports server error with code`() {
        val msg = httpErrorMessage(503)
        assertTrue(msg.lowercase().contains("server error"))
        assertTrue(msg.contains("503"))
    }

    @Test
    fun `httpErrorMessage unknown code includes code in message`() {
        assertTrue(httpErrorMessage(418).contains("418"))
    }
}
