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

/**
 * Integration tests for the auto-suggest flow:
 * AutoSuggestController + MockInputConnection + MockWebServer (mocked API).
 *
 * Note: debounce timing (1500 ms) is a service-level concern handled by
 * scheduleAutoSuggest() in GrammarKeyboardService and is not part of the controller.
 */
class AutoSuggestFlowTest {

    private val server = MockWebServer()
    private val testClient = OkHttpClient.Builder()
        .connectTimeout(500, TimeUnit.MILLISECONDS)
        .readTimeout(500, TimeUnit.MILLISECONDS)
        .writeTimeout(500, TimeUnit.MILLISECONDS)
        .build()
    private lateinit var controller: AutoSuggestController
    private lateinit var personalWordList: PersonalWordList

    @Before
    fun setUp() {
        server.start()
        personalWordList = PersonalWordList()
        controller = AutoSuggestController(GrammarService(testClient), personalWordList)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun baseUrl() = server.url("/").toString()
    private fun successBody(content: String) = """{"choices":[{"message":{"content":"$content"}}]}"""

    // --- Suggestion available ---

    @Test
    fun `available suggestion - returns Available state with original and corrected text`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("Hello world.")))
        val ic = MockInputConnection("helo wrld")

        val state = controller.suggest(ic, baseUrl(), "gpt-4o-mini", "token")

        assertTrue(state is SuggestionState.Available)
        val available = state as SuggestionState.Available
        assertEquals("helo wrld", available.original)
        assertEquals("Hello world.", available.corrected)
    }

    @Test
    fun `available suggestion - diff segments are computed`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("Hello world.")))
        val ic = MockInputConnection("helo wrld")

        val state = controller.suggest(ic, baseUrl(), "gpt-4o-mini", "token")

        assertTrue(state is SuggestionState.Available)
        assertTrue("Diff should be non-empty for changed text", (state as SuggestionState.Available).diff.isNotEmpty())
    }

    // --- Empty / blank text ---

    @Test
    fun `empty text - returns Idle without calling API`() = runTest {
        val ic = MockInputConnection("")

        val state = controller.suggest(ic, baseUrl(), "gpt-4o-mini", "token")

        assertEquals(SuggestionState.Idle, state)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `blank whitespace text - returns Idle without calling API`() = runTest {
        val ic = MockInputConnection("   ")

        val state = controller.suggest(ic, baseUrl(), "gpt-4o-mini", "token")

        assertEquals(SuggestionState.Idle, state)
        assertEquals(0, server.requestCount)
    }

    // --- Unchanged text ---

    @Test
    fun `unchanged text - API returns identical text - returns Idle`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("already correct")))
        val ic = MockInputConnection("already correct")

        val state = controller.suggest(ic, baseUrl(), "gpt-4o-mini", "token")

        assertEquals(SuggestionState.Idle, state)
    }

    // --- API errors ---

    @Test
    fun `API error - returns Idle silently`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401))
        val ic = MockInputConnection("some text")

        val state = controller.suggest(ic, baseUrl(), "gpt-4o-mini", "bad-token")

        assertEquals(SuggestionState.Idle, state)
    }

    @Test
    fun `API 500 - returns Idle silently`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        val ic = MockInputConnection("some text")

        val state = controller.suggest(ic, baseUrl(), "gpt-4o-mini", "token")

        assertEquals(SuggestionState.Idle, state)
    }

    @Test
    fun `read timeout - returns Idle silently`() = runTest {
        server.enqueue(MockResponse().setBodyDelay(2, TimeUnit.SECONDS).setBody("{}"))
        val ic = MockInputConnection("some text")

        val state = controller.suggest(ic, baseUrl(), "gpt-4o-mini", "token")

        assertEquals(SuggestionState.Idle, state)
    }

    // --- Suggestion acceptance ---

    @Test
    fun `suggestion acceptance - applying to InputConnection replaces text`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("Hello world.")))
        val ic = MockInputConnection("helo wrld")

        val state = controller.suggest(ic, baseUrl(), "gpt-4o-mini", "token")
        assertTrue(state is SuggestionState.Available)
        val available = state as SuggestionState.Available

        // Simulate service accepting the suggestion
        ic.deleteSurroundingText(available.original.length, 0)
        ic.commitText(available.corrected, 1)

        assertEquals("Hello world.", ic.currentText())
        assertEquals(1, ic.deleteSurroundingTextCalls.size)
        assertEquals("helo wrld".length, ic.deleteSurroundingTextCalls[0].first)
        assertEquals(1, ic.commitTextCalls.size)
        assertEquals("Hello world.", ic.commitTextCalls[0].first)
    }

    // --- Suggestion rejection ---

    @Test
    fun `suggestion rejection - dismissing makes no changes to InputConnection`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("Hello world.")))
        val ic = MockInputConnection("helo wrld")

        val state = controller.suggest(ic, baseUrl(), "gpt-4o-mini", "token")
        assertTrue(state is SuggestionState.Available)

        // Rejection = no IC calls
        assertTrue(ic.commitTextCalls.isEmpty())
        assertTrue(ic.deleteSurroundingTextCalls.isEmpty())
        assertEquals("helo wrld", ic.currentText())
    }

    // --- 5000-char limit ---

    @Test
    fun `getTextBeforeCursor is always called with maxLength 5000`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("ok")))
        val ic = MockInputConnection("some text")

        controller.suggest(ic, baseUrl(), "gpt-4o-mini", "token")

        assertEquals(5000, ic.lastGetTextBeforeCursorN)
    }

    // --- Personal word list suppression ---

    @Test
    fun `personal word list suppression - returns Idle when correction is suppressed`() = runTest {
        personalWordList.add("typlx")
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("typlex")))
        val ic = MockInputConnection("typlx")

        val state = controller.suggest(ic, baseUrl(), "gpt-4o-mini", "token")

        assertEquals(SuggestionState.Idle, state)
    }
}
