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

class GrammarInstructionSuffixTest {

    private val server = MockWebServer()
    private val testClient = OkHttpClient.Builder()
        .connectTimeout(500, TimeUnit.MILLISECONDS)
        .readTimeout(500, TimeUnit.MILLISECONDS)
        .writeTimeout(500, TimeUnit.MILLISECONDS)
        .build()
    private lateinit var grammarService: GrammarService
    private lateinit var fixController: GrammarFixController
    private lateinit var autoSuggestController: AutoSuggestController

    @Before
    fun setUp() {
        server.start()
        grammarService = GrammarService(testClient)
        fixController = GrammarFixController(grammarService, PersonalWordList())
        autoSuggestController = AutoSuggestController(grammarService, PersonalWordList())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun baseUrl() = server.url("/").toString()
    private fun successBody(content: String) = """{"choices":[{"message":{"content":"$content"}}]}"""

    // --- GrammarService.buildSystemPrompt ---

    @Test
    fun `buildSystemPrompt returns base prompt when suffix is blank`() {
        assertEquals(GrammarService.SYSTEM_PROMPT, GrammarService.buildSystemPrompt(""))
    }

    @Test
    fun `buildSystemPrompt returns base prompt when suffix is whitespace only`() {
        assertEquals(GrammarService.SYSTEM_PROMPT, GrammarService.buildSystemPrompt("   "))
    }

    @Test
    fun `buildSystemPrompt appends suffix to base prompt when non-blank`() {
        val suffix = "Use formal language."
        val expected = "${GrammarService.SYSTEM_PROMPT} $suffix"
        assertEquals(expected, GrammarService.buildSystemPrompt(suffix))
    }

    @Test
    fun `buildSystemPrompt handles multi-line suffix`() {
        val suffix = "Use formal language.\nKeep medical terms."
        val result = GrammarService.buildSystemPrompt(suffix)
        assertTrue(result.contains(GrammarService.SYSTEM_PROMPT))
        assertTrue(result.contains(suffix))
    }

    // --- GrammarFixController passes suffix to API ---

    @Test
    fun `GrammarFixController passes suffix in system prompt to API`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("Hello world.")))
        val ic = MockInputConnection("helo wrld")

        val result = fixController.fix(
            ic, baseUrl(), "gpt-4o-mini", "token",
            systemPromptSuffix = "Use formal language."
        )

        assertTrue(result.isSuccess)
        val body = server.takeRequest().body.readUtf8()
        assertTrue("Request should contain suffix", body.contains("Use formal language."))
        assertTrue("Request should contain base prompt", body.contains("Fix grammar and spelling"))
    }

    @Test
    fun `GrammarFixController with blank suffix behaves identically to default call`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("Hello.")))
        val ic = MockInputConnection("hello")

        val result = fixController.fix(ic, baseUrl(), "gpt-4o-mini", "token", systemPromptSuffix = "")

        assertTrue(result.isSuccess)
        val body = server.takeRequest().body.readUtf8()
        assertTrue("Request should use base prompt", body.contains("Fix grammar and spelling"))
    }

    // --- AutoSuggestController passes suffix to API ---

    @Test
    fun `AutoSuggestController passes suffix in system prompt to API`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("Hello, world.")))
        val ic = MockInputConnection("Hello world")

        autoSuggestController.suggest(
            ic, baseUrl(), "gpt-4o-mini", "token",
            systemPromptSuffix = "Keep casual tone."
        )

        val body = server.takeRequest().body.readUtf8()
        assertTrue("Request should contain suffix", body.contains("Keep casual tone."))
    }
}
