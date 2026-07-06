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

class GrammarServiceConnectionTest {

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
        service = GrammarService(testClient, maxRetries = 0)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun baseUrl() = server.url("/").toString()
    private fun successBody() = """{"choices":[{"message":{"content":"Hello."}}]}"""

    @Test
    fun `testConnection returns positive elapsed time on success`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody()))
        val ms = service.testConnection(baseUrl(), "gpt-4o-mini", "sk-token")
        assertTrue("Expected positive elapsed time, got $ms", ms >= 0)
    }

    @Test
    fun `testConnection sends a request to the correct endpoint`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody()))
        service.testConnection(baseUrl(), "gpt-4o-mini", "sk-token")
        val recorded = server.takeRequest()
        assertTrue(recorded.path?.endsWith("/v1/chat/completions") == true)
    }

    @Test
    fun `testConnection throws GrammarServiceException on 401`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401).setBody("Unauthorized"))
        val ex = runCatching {
            service.testConnection(baseUrl(), "gpt-4o-mini", "bad-token")
        }.exceptionOrNull()
        assertNotNull("Expected exception on 401", ex)
        assertTrue("Expected GrammarServiceException, got ${ex?.javaClass}", ex is GrammarServiceException)
        assertTrue(ex!!.message?.contains("401") == true)
    }

    @Test
    fun `testConnection throws GrammarServiceException on server error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))
        val ex = runCatching {
            service.testConnection(baseUrl(), "gpt-4o-mini", "sk-token")
        }.exceptionOrNull()
        assertNotNull("Expected exception on 500", ex)
        assertTrue("Expected GrammarServiceException, got ${ex?.javaClass}", ex is GrammarServiceException)
    }

    @Test
    fun `testConnection throws GrammarServiceException when server unreachable`() = runTest {
        // Use a port that is not bound to anything after shutdown
        server.shutdown()
        val closedUrl = "http://127.0.0.1:1"
        val ex = runCatching {
            service.testConnection(closedUrl, "gpt-4o-mini", "sk-token")
        }.exceptionOrNull()
        assertNotNull("Expected exception when server unreachable", ex)
        assertTrue("Expected GrammarServiceException, got ${ex?.javaClass}", ex is GrammarServiceException)
    }
}
