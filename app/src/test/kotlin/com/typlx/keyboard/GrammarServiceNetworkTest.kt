package com.typlx.keyboard

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class GrammarServiceNetworkTest {

    private val server = MockWebServer()
    private val testClient = OkHttpClient.Builder()
        .connectTimeout(500, TimeUnit.MILLISECONDS)
        .readTimeout(500, TimeUnit.MILLISECONDS)
        .writeTimeout(500, TimeUnit.MILLISECONDS)
        .build()
    private lateinit var service: GrammarService

    @Before
    fun setUp() {
        server.start()
        // maxRetries = 0 keeps existing tests deterministic (one enqueued response per call)
        service = GrammarService(testClient, maxRetries = 0)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun baseUrl() = server.url("/").toString()

    private fun successBody(content: String) =
        """{"choices":[{"message":{"content":"$content"}}]}"""

    @Test
    fun `fixGrammar returns corrected text on success`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("Hello world.")))
        val result = service.fixGrammar(baseUrl(), "gpt-4o-mini", "sk-token", "Helo wrld")
        assertEquals("Hello world.", result)
    }

    @Test
    fun `fixGrammar trims leading and trailing whitespace from response`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{"choices":[{"message":{"content":"  Trimmed.  "}}]}""")
        )
        val result = service.fixGrammar(baseUrl(), "gpt-4o-mini", "sk-token", "text")
        assertEquals("Trimmed.", result)
    }

    @Test
    fun `fixGrammar sends Authorization header with bearer token`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("ok")))
        service.fixGrammar(baseUrl(), "gpt-4o-mini", "my-secret-token", "text")
        val recorded = server.takeRequest()
        assertEquals("Bearer my-secret-token", recorded.getHeader("Authorization"))
    }

    @Test
    fun `fixGrammar sends correct model in request body`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("ok")))
        service.fixGrammar(baseUrl(), "claude-3-haiku", "token", "text")
        val recorded = server.takeRequest()
        val body = recorded.body.readUtf8()
        assertTrue("body should contain model name", body.contains("\"model\":\"claude-3-haiku\""))
    }

    @Test
    fun `fixGrammar appends v1 chat completions path to base url`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("ok")))
        // Pass url without /v1 — service should add /v1/chat/completions
        val urlWithoutV1 = server.url("/").toString().trimEnd('/')
        service.fixGrammar(urlWithoutV1, "gpt-4o-mini", "token", "text")
        val recorded = server.takeRequest()
        assertEquals("/v1/chat/completions", recorded.path)
    }

    @Test
    fun `fixGrammar deduplicates v1 in url`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("ok")))
        // Pass url already ending in /v1 — should not produce /v1/v1/chat/completions
        val urlWithV1 = server.url("/v1").toString()
        service.fixGrammar(urlWithV1, "gpt-4o-mini", "token", "text")
        val recorded = server.takeRequest()
        assertEquals("/v1/chat/completions", recorded.path)
    }

    @Test
    fun `fixGrammar throws GrammarServiceException on 401`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401))
        try {
            service.fixGrammar(baseUrl(), "gpt-4o-mini", "bad-token", "text")
            fail("Expected GrammarServiceException")
        } catch (e: GrammarServiceException) {
            assertTrue("message should mention 401", e.message!!.contains("401"))
        }
    }

    @Test
    fun `fixGrammar throws GrammarServiceException on 403`() = runTest {
        server.enqueue(MockResponse().setResponseCode(403))
        try {
            service.fixGrammar(baseUrl(), "gpt-4o-mini", "bad-token", "text")
            fail("Expected GrammarServiceException")
        } catch (e: GrammarServiceException) {
            assertTrue("message should mention 403", e.message!!.contains("403"))
        }
    }

    @Test
    fun `fixGrammar throws on 429 with rate limit message`() = runTest {
        server.enqueue(MockResponse().setResponseCode(429))
        try {
            service.fixGrammar(baseUrl(), "gpt-4o-mini", "token", "text")
            fail("Expected GrammarServiceException")
        } catch (e: GrammarServiceException) {
            assertTrue("message should mention rate limit", e.message!!.lowercase().contains("rate limit"))
        }
    }

    @Test
    fun `fixGrammar throws on 500 with server error message`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        try {
            service.fixGrammar(baseUrl(), "gpt-4o-mini", "token", "text")
            fail("Expected GrammarServiceException")
        } catch (e: GrammarServiceException) {
            assertTrue("message should mention server error", e.message!!.lowercase().contains("server error"))
        }
    }

    @Test
    fun `fixGrammar throws on malformed JSON response`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("not-json{{{"))
        try {
            service.fixGrammar(baseUrl(), "gpt-4o-mini", "token", "text")
            fail("Expected GrammarServiceException")
        } catch (e: GrammarServiceException) {
            assertNotNull(e)
        }
    }

    @Test
    fun `fixGrammar throws on empty choices array`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"choices":[]}"""))
        try {
            service.fixGrammar(baseUrl(), "gpt-4o-mini", "token", "text")
            fail("Expected GrammarServiceException")
        } catch (e: GrammarServiceException) {
            assertTrue("message should mention no choices", e.message!!.contains("No choices"))
        }
    }

    @Test
    fun `fixGrammar throws on read timeout`() = runTest {
        // Body delay exceeds the 500ms read timeout on testClient
        server.enqueue(MockResponse().setBodyDelay(2, TimeUnit.SECONDS).setBody("{}"))
        try {
            service.fixGrammar(baseUrl(), "gpt-4o-mini", "token", "text")
            fail("Expected GrammarServiceException")
        } catch (e: GrammarServiceException) {
            assertTrue("message should mention timed out", e.message!!.lowercase().contains("timed out"))
        }
    }

    // --- Retry behaviour ---

    @Test
    fun `fixGrammar retries on timeout and succeeds on second attempt`() = runTest {
        val retryService = GrammarService(testClient, maxRetries = 1)
        // First response: timeout; second response: success
        server.enqueue(MockResponse().setBodyDelay(2, TimeUnit.SECONDS).setBody("{}"))
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("Retried.")))
        val result = retryService.fixGrammar(baseUrl(), "gpt-4o-mini", "token", "text")
        assertEquals("Retried.", result)
        assertEquals("server should have received 2 requests", 2, server.requestCount)
    }

    @Test
    fun `fixGrammar retries on 503 and succeeds on second attempt`() = runTest {
        val retryService = GrammarService(testClient, maxRetries = 1)
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("After 503.")))
        val result = retryService.fixGrammar(baseUrl(), "gpt-4o-mini", "token", "text")
        assertEquals("After 503.", result)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `fixGrammar retries on 429 and succeeds on second attempt`() = runTest {
        val retryService = GrammarService(testClient, maxRetries = 1)
        server.enqueue(MockResponse().setResponseCode(429))
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("After 429.")))
        val result = retryService.fixGrammar(baseUrl(), "gpt-4o-mini", "token", "text")
        assertEquals("After 429.", result)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `fixGrammar does not retry on 401`() = runTest {
        val retryService = GrammarService(testClient, maxRetries = 2)
        server.enqueue(MockResponse().setResponseCode(401))
        try {
            retryService.fixGrammar(baseUrl(), "gpt-4o-mini", "bad-token", "text")
            fail("Expected GrammarServiceException")
        } catch (e: GrammarServiceException) {
            assertTrue(e.message!!.contains("401"))
        }
        // Only one request should have been sent (no retry for auth errors)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `fixGrammar does not retry on 400`() = runTest {
        val retryService = GrammarService(testClient, maxRetries = 2)
        server.enqueue(MockResponse().setResponseCode(400))
        try {
            retryService.fixGrammar(baseUrl(), "gpt-4o-mini", "token", "text")
            fail("Expected GrammarServiceException")
        } catch (e: GrammarServiceException) {
            assertNotNull(e)
        }
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `fixGrammar exhausts retries and throws on persistent timeout`() = runTest {
        val retryService = GrammarService(testClient, maxRetries = 2)
        // Three timeouts — all attempts should fail
        repeat(3) { server.enqueue(MockResponse().setBodyDelay(2, TimeUnit.SECONDS).setBody("{}")) }
        try {
            retryService.fixGrammar(baseUrl(), "gpt-4o-mini", "token", "text")
            fail("Expected GrammarServiceException")
        } catch (e: GrammarServiceException) {
            assertTrue(e.message!!.lowercase().contains("timed out"))
        }
        assertEquals(3, server.requestCount)
    }

    @Test
    fun `GrammarServiceException isRetryable false by default`() {
        val e = GrammarServiceException("oops")
        assertFalse(e.isRetryable)
    }

    @Test
    fun `isRetryableHttpCode returns true for 429, 503, 504`() {
        assertTrue(isRetryableHttpCode(429))
        assertTrue(isRetryableHttpCode(503))
        assertTrue(isRetryableHttpCode(504))
    }

    @Test
    fun `isRetryableHttpCode returns false for 400, 401, 403, 500`() {
        assertFalse(isRetryableHttpCode(400))
        assertFalse(isRetryableHttpCode(401))
        assertFalse(isRetryableHttpCode(403))
        assertFalse(isRetryableHttpCode(500))
    }
}
