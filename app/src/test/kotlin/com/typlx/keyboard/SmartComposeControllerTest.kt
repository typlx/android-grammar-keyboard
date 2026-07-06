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

class SmartComposeControllerTest {

    private val server = MockWebServer()
    private val testClient = OkHttpClient.Builder()
        .connectTimeout(500, TimeUnit.MILLISECONDS)
        .readTimeout(500, TimeUnit.MILLISECONDS)
        .writeTimeout(500, TimeUnit.MILLISECONDS)
        .build()
    private lateinit var controller: SmartComposeController

    @Before
    fun setUp() {
        server.start()
        controller = SmartComposeController(GrammarService(testClient, maxRetries = 0))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun baseUrl() = server.url("/").toString()
    private fun successBody(content: String) = """{"choices":[{"message":{"content":"$content"}}]}"""

    // --- Happy path ---

    @Test
    fun `returns continuation text on success`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("and it was wonderful.")))

        val result = controller.compose(
            text = "The day began",
            apiUrl = baseUrl(),
            model = "gpt-4o-mini",
            token = "tok",
        )

        assertTrue(result.isSuccess)
        assertEquals("and it was wonderful.", result.getOrNull())
    }

    @Test
    fun `sends correct system prompt`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("continuation")))

        controller.compose("hello", baseUrl(), "gpt-4o-mini", "tok")

        val body = server.takeRequest().body.readUtf8()
        assertTrue(
            "System prompt should contain continuation instruction",
            body.contains("Continue the following text naturally"),
        )
        assertTrue(
            "System prompt should instruct to not repeat original",
            body.contains("Do not repeat any part of the original text"),
        )
    }

    // --- Empty / blank input ---

    @Test
    fun `blank text returns failure without calling API`() = runTest {
        val result = controller.compose(
            text = "   ",
            apiUrl = baseUrl(),
            model = "gpt-4o-mini",
            token = "tok",
        )

        assertTrue(result.isFailure)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `empty text returns failure without calling API`() = runTest {
        val result = controller.compose(
            text = "",
            apiUrl = baseUrl(),
            model = "gpt-4o-mini",
            token = "tok",
        )

        assertTrue(result.isFailure)
        assertEquals(0, server.requestCount)
    }

    // --- API errors ---

    @Test
    fun `API 401 returns failure`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401))

        val result = controller.compose("some text", baseUrl(), "gpt-4o-mini", "bad-token")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GrammarServiceException)
    }

    @Test
    fun `API 500 returns failure`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = controller.compose("some text", baseUrl(), "gpt-4o-mini", "tok")

        assertTrue(result.isFailure)
    }

    @Test
    fun `read timeout returns failure`() = runTest {
        server.enqueue(MockResponse().setBodyDelay(2, TimeUnit.SECONDS).setBody("{}"))

        val result = controller.compose("some text", baseUrl(), "gpt-4o-mini", "tok")

        assertTrue(result.isFailure)
    }
}
