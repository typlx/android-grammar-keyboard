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
}
