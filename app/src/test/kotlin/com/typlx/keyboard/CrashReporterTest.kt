package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class CrashReporterTest {

    private fun domain(url: String) = CrashReporter.extractDomain(url)

    @Test
    fun `standard https URL returns host`() {
        assertEquals("api.openai.com", domain("https://api.openai.com"))
    }

    @Test
    fun `URL with path strips path component`() {
        assertEquals("api.openai.com", domain("https://api.openai.com/v1/chat/completions"))
    }

    @Test
    fun `URL with query string strips query`() {
        assertEquals("api.openai.com", domain("https://api.openai.com/v1?key=abc"))
    }

    @Test
    fun `URL with port strips port`() {
        assertEquals("localhost", domain("http://localhost:8080"))
    }

    @Test
    fun `URL with trailing slash returns host`() {
        assertEquals("api.openai.com", domain("https://api.openai.com/"))
    }

    @Test
    fun `uppercase host is lowercased`() {
        assertEquals("api.openai.com", domain("https://API.OpenAI.com"))
    }

    @Test
    fun `leading and trailing whitespace is trimmed`() {
        assertEquals("api.openai.com", domain("  https://api.openai.com  "))
    }

    @Test
    fun `http URL returns host`() {
        assertEquals("example.com", domain("http://example.com"))
    }

    @Test
    fun `IPv4 address URL returns IP string`() {
        assertEquals("192.168.1.1", domain("http://192.168.1.1:11434"))
    }

    @Test
    fun `opaque URI with no host returns unknown`() {
        // "foo:bar" is a valid URI but has no host component
        assertEquals("unknown", domain("foo:bar"))
    }

    @Test
    fun `empty string returns unknown`() {
        assertEquals("unknown", domain(""))
    }

    @Test
    fun `URL with only spaces returns unknown`() {
        assertEquals("unknown", domain("   "))
    }

    @Test
    fun `completely malformed URL returns invalid_url`() {
        assertEquals("invalid_url", domain("not a url ://"))
    }
}
