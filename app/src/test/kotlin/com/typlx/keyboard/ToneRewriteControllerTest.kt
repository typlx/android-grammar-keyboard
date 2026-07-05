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

class ToneRewriteControllerTest {

    private val server = MockWebServer()
    private val testClient = OkHttpClient.Builder()
        .connectTimeout(500, TimeUnit.MILLISECONDS)
        .readTimeout(500, TimeUnit.MILLISECONDS)
        .writeTimeout(500, TimeUnit.MILLISECONDS)
        .build()
    private lateinit var controller: ToneRewriteController

    @Before
    fun setUp() {
        server.start()
        controller = ToneRewriteController(GrammarService(testClient))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun baseUrl() = server.url("/").toString()
    private fun successBody(content: String) = """{"choices":[{"message":{"content":"$content"}}]}"""

    @Test
    fun `rewriteText returns success with rewritten text`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("Be professional.")))
        val result = controller.rewriteText(
            text = "be professional",
            tone = ToneOption.PROFESSIONAL,
            apiUrl = baseUrl(),
            model = "gpt-4o-mini",
            token = "token",
        )
        assertTrue(result.isSuccess)
        assertEquals("Be professional.", result.getOrThrow())
    }

    @Test
    fun `rewriteText sends tone systemPrompt in request body`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("ok")))
        controller.rewriteText("some text", ToneOption.CASUAL, baseUrl(), "gpt-4o-mini", "token")
        val recorded = server.takeRequest()
        assertTrue(recorded.body.readUtf8().contains(ToneOption.CASUAL.systemPrompt))
    }

    @Test
    fun `rewriteText returns failure wrapping GrammarServiceException on API error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401))
        val result = controller.rewriteText("text", ToneOption.FORMAL, baseUrl(), "gpt-4o-mini", "bad-token")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GrammarServiceException)
    }

    @Test
    fun `rewriteText returns failure on server error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        val result = controller.rewriteText("text", ToneOption.FRIENDLY, baseUrl(), "gpt-4o-mini", "token")
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(ex!!.message!!.lowercase().contains("server error"))
    }

    @Test
    fun `rewriteText returns failure on read timeout`() = runTest {
        server.enqueue(MockResponse().setBodyDelay(2, TimeUnit.SECONDS).setBody("{}"))
        val result = controller.rewriteText("text", ToneOption.CONCISE, baseUrl(), "gpt-4o-mini", "token")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GrammarServiceException)
    }

    @Test
    fun `rewriteText each ToneOption sends its own systemPrompt`() = runTest {
        for (tone in ToneOption.entries) {
            server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("ok")))
            controller.rewriteText("text", tone, baseUrl(), "gpt-4o-mini", "token")
            val recorded = server.takeRequest()
            assertTrue(
                "Expected systemPrompt for $tone in request body",
                recorded.body.readUtf8().contains(tone.systemPrompt),
            )
        }
    }
}
