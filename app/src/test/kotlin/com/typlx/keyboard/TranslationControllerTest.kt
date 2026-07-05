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

class TranslationControllerTest {

    private val server = MockWebServer()
    private val testClient = OkHttpClient.Builder()
        .connectTimeout(500, TimeUnit.MILLISECONDS)
        .readTimeout(500, TimeUnit.MILLISECONDS)
        .writeTimeout(500, TimeUnit.MILLISECONDS)
        .build()
    private lateinit var controller: TranslationController

    @Before
    fun setUp() {
        server.start()
        controller = TranslationController(GrammarService(testClient))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun baseUrl() = server.url("/").toString()
    private fun successBody(content: String) = """{"choices":[{"message":{"content":"$content"}}]}"""

    @Test
    fun `translateText returns success with translated text`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("Hola mundo.")))
        val result = controller.translateText(
            text = "Hello world.",
            language = TranslationLanguage.SPANISH,
            apiUrl = baseUrl(),
            model = "gpt-4o-mini",
            token = "token",
        )
        assertTrue(result.isSuccess)
        assertEquals("Hola mundo.", result.getOrThrow())
    }

    @Test
    fun `translateText sends language systemPrompt in request body`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("ok")))
        controller.translateText("some text", TranslationLanguage.FRENCH, baseUrl(), "gpt-4o-mini", "token")
        val recorded = server.takeRequest()
        assertTrue(recorded.body.readUtf8().contains(TranslationLanguage.FRENCH.systemPrompt))
    }

    @Test
    fun `translateText returns failure wrapping GrammarServiceException on API error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401))
        val result = controller.translateText("text", TranslationLanguage.GERMAN, baseUrl(), "gpt-4o-mini", "bad-token")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GrammarServiceException)
    }

    @Test
    fun `translateText returns failure on server error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        val result = controller.translateText("text", TranslationLanguage.JAPANESE, baseUrl(), "gpt-4o-mini", "token")
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(ex!!.message!!.lowercase().contains("server error"))
    }

    @Test
    fun `translateText returns failure on read timeout`() = runTest {
        server.enqueue(MockResponse().setBodyDelay(2, TimeUnit.SECONDS).setBody("{}"))
        val result = controller.translateText("text", TranslationLanguage.ARABIC, baseUrl(), "gpt-4o-mini", "token")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GrammarServiceException)
    }

    @Test
    fun `translateText each language sends its own systemPrompt`() = runTest {
        for (language in TranslationLanguage.entries) {
            server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("ok")))
            controller.translateText("text", language, baseUrl(), "gpt-4o-mini", "token")
            val recorded = server.takeRequest()
            assertTrue(
                "Expected systemPrompt for $language in request body",
                recorded.body.readUtf8().contains(language.systemPrompt),
            )
        }
    }
}
