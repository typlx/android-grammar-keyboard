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
 * Integration tests for the grammar fix flow:
 * GrammarFixController + MockInputConnection + MockWebServer (mocked API).
 *
 * Note: private-field detection (isPrivateInputType) is covered by PrivateFieldCheckTest.
 * The service skips calling GrammarFixController entirely for private fields.
 */
class GrammarFixFlowTest {

    private val server = MockWebServer()
    private val testClient = OkHttpClient.Builder()
        .connectTimeout(500, TimeUnit.MILLISECONDS)
        .readTimeout(500, TimeUnit.MILLISECONDS)
        .writeTimeout(500, TimeUnit.MILLISECONDS)
        .build()
    private lateinit var controller: GrammarFixController
    private lateinit var personalWordList: PersonalWordList

    @Before
    fun setUp() {
        server.start()
        personalWordList = PersonalWordList()
        controller = GrammarFixController(GrammarService(testClient), personalWordList)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun baseUrl() = server.url("/").toString()
    private fun successBody(content: String) = """{"choices":[{"message":{"content":"$content"}}]}"""

    // --- Happy path ---

    @Test
    fun `happy path - corrected text is deleted then committed to InputConnection`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("Hello world.")))
        val ic = MockInputConnection("helo wrld")

        val result = controller.fix(ic, baseUrl(), "gpt-4o-mini", "token")

        assertTrue(result.isSuccess)
        val fixResult = result.getOrThrow()
        assertNotNull(fixResult)
        assertEquals("helo wrld", fixResult!!.original)
        assertEquals("Hello world.", fixResult.fixed)

        assertEquals(1, ic.deleteSurroundingTextCalls.size)
        assertEquals("helo wrld".length, ic.deleteSurroundingTextCalls[0].first)
        assertEquals(1, ic.commitTextCalls.size)
        assertEquals("Hello world.", ic.commitTextCalls[0].first)
        assertEquals("Hello world.", ic.currentText())
    }

    @Test
    fun `happy path - sends correct text to API`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("Corrected.")))
        val ic = MockInputConnection("i hav bad grammer")

        controller.fix(ic, baseUrl(), "gpt-4o-mini", "token")

        val recorded = server.takeRequest()
        assertTrue(recorded.body.readUtf8().contains("i hav bad grammer"))
    }

    // --- Empty / blank text ---

    @Test
    fun `empty text - returns failure without calling API`() = runTest {
        val ic = MockInputConnection("")

        val result = controller.fix(ic, baseUrl(), "gpt-4o-mini", "token")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GrammarServiceException)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("No text", ignoreCase = true))
        assertEquals(0, server.requestCount)
        assertTrue(ic.commitTextCalls.isEmpty())
        assertTrue(ic.deleteSurroundingTextCalls.isEmpty())
    }

    @Test
    fun `blank whitespace text - returns failure without calling API`() = runTest {
        val ic = MockInputConnection("   \n  ")

        val result = controller.fix(ic, baseUrl(), "gpt-4o-mini", "token")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GrammarServiceException)
        assertEquals(0, server.requestCount)
    }

    // --- 5000-char limit ---

    @Test
    fun `getTextBeforeCursor is always called with maxLength 5000`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("ok")))
        val ic = MockInputConnection("some text")

        controller.fix(ic, baseUrl(), "gpt-4o-mini", "token")

        assertEquals(5000, ic.lastGetTextBeforeCursorN)
    }

    @Test
    fun `text longer than 5000 chars is truncated to last 5000 by MockInputConnection`() = runTest {
        val longText = "a".repeat(6000)
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("corrected")))
        val ic = MockInputConnection(longText)

        controller.fix(ic, baseUrl(), "gpt-4o-mini", "token")

        // getTextBeforeCursor(5000, 0) — only the last 5000 chars reach the API
        assertEquals(5000, ic.lastGetTextBeforeCursorN)
        val requestBody = server.takeRequest().body.readUtf8()
        // The API received exactly 5000 'a' chars (not 6000)
        assertTrue(requestBody.contains("a".repeat(5000)))
        assertFalse(requestBody.contains("a".repeat(5001)))
    }

    // --- API errors ---

    @Test
    fun `API 401 - returns failure with GrammarServiceException, no IC writes`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401))
        val ic = MockInputConnection("some text")

        val result = controller.fix(ic, baseUrl(), "gpt-4o-mini", "bad-token")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GrammarServiceException)
        assertTrue(ic.commitTextCalls.isEmpty())
        assertTrue(ic.deleteSurroundingTextCalls.isEmpty())
    }

    @Test
    fun `API 429 - returns failure with rate-limit message`() = runTest {
        // GrammarService retries 429 up to maxRetries (2) times — enqueue enough responses
        repeat(3) { server.enqueue(MockResponse().setResponseCode(429)) }
        val ic = MockInputConnection("some text")

        val result = controller.fix(ic, baseUrl(), "gpt-4o-mini", "token")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.lowercase().contains("rate limit"))
    }

    @Test
    fun `API 500 - returns failure with server-error message`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        val ic = MockInputConnection("some text")

        val result = controller.fix(ic, baseUrl(), "gpt-4o-mini", "token")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.lowercase().contains("server error"))
    }

    @Test
    fun `read timeout - returns failure`() = runTest {
        server.enqueue(MockResponse().setBodyDelay(2, TimeUnit.SECONDS).setBody("{}"))
        val ic = MockInputConnection("some text")

        val result = controller.fix(ic, baseUrl(), "gpt-4o-mini", "token")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GrammarServiceException)
    }

    // --- Undo ---

    @Test
    fun `undo - FixResult carries original and fixed text needed by GrammarUndoState`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("Hello world.")))
        val ic = MockInputConnection("helo wrld")
        val undoState = GrammarUndoState()

        val result = controller.fix(ic, baseUrl(), "gpt-4o-mini", "token")
        val fixResult = result.getOrThrow()!!
        undoState.recordFix(fixResult.original, fixResult.fixed)

        assertTrue(undoState.canUndo)
        val (original, fixed) = undoState.consume()!!
        assertEquals("helo wrld", original)
        assertEquals("Hello world.", fixed)
    }

    @Test
    fun `undo - applying undo via InputConnection restores original text`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("Hello world.")))
        val ic = MockInputConnection("helo wrld")

        val fixResult = controller.fix(ic, baseUrl(), "gpt-4o-mini", "token").getOrThrow()!!
        assertEquals("Hello world.", ic.currentText())

        // Simulate the undo operation (as the service would do it)
        ic.deleteSurroundingText(fixResult.fixed.length, 0)
        ic.commitText(fixResult.original, 1)

        assertEquals("helo wrld", ic.currentText())
    }

    // --- Personal word list suppression ---

    @Test
    fun `personal word list suppression - returns Success(null), no IC writes`() = runTest {
        personalWordList.add("typlx")
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("typlex")))
        val ic = MockInputConnection("typlx")

        val result = controller.fix(ic, baseUrl(), "gpt-4o-mini", "token")

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
        assertTrue(ic.commitTextCalls.isEmpty())
        assertTrue(ic.deleteSurroundingTextCalls.isEmpty())
    }

    // --- Selection-aware fix ---

    @Test
    fun `selection - only selected text is sent to API`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("Fixed sentence.")))
        val ic = MockInputConnection("First sentence. bad grammer here. Last sentence.")
        ic.simulatedSelection = "bad grammer here"

        controller.fix(ic, baseUrl(), "gpt-4o-mini", "token", hasSelection = true)

        val recorded = server.takeRequest()
        val body = recorded.body.readUtf8()
        assertTrue("should send selected text to API", body.contains("bad grammer here"))
        assertFalse("should NOT send full text when selection active", body.contains("First sentence."))
    }

    @Test
    fun `selection - commitText replaces selection without deleteSurroundingText`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("Fixed sentence.")))
        val ic = MockInputConnection("First sentence. bad grammer here. Last sentence.")
        ic.simulatedSelection = "bad grammer here"

        val result = controller.fix(ic, baseUrl(), "gpt-4o-mini", "token", hasSelection = true)

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrThrow())
        // Selection path: commitText only, no deleteSurroundingText
        assertTrue("should not call deleteSurroundingText when selection active",
            ic.deleteSurroundingTextCalls.isEmpty())
        assertEquals(1, ic.commitTextCalls.size)
        assertEquals("Fixed sentence.", ic.commitTextCalls[0].first)
    }

    @Test
    fun `selection - buffer reflects in-place replacement`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("correct text")))
        val ic = MockInputConnection("prefix wrnog suffix")
        ic.simulatedSelection = "wrnog"

        controller.fix(ic, baseUrl(), "gpt-4o-mini", "token", hasSelection = true)

        assertEquals("prefix correct text suffix", ic.currentText())
    }

    @Test
    fun `selection - FixResult carries selected original and LLM fixed text`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("corrected")))
        val ic = MockInputConnection("some wrnog word")
        ic.simulatedSelection = "wrnog"

        val result = controller.fix(ic, baseUrl(), "gpt-4o-mini", "token", hasSelection = true)

        val fixResult = result.getOrThrow()!!
        assertEquals("wrnog", fixResult.original)
        assertEquals("corrected", fixResult.fixed)
    }

    @Test
    fun `selection - empty selection falls back to full text before cursor`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(successBody("Hello world.")))
        val ic = MockInputConnection("helo wrld")
        // simulatedSelection is null by default — no selection

        val result = controller.fix(ic, baseUrl(), "gpt-4o-mini", "token", hasSelection = true)

        assertTrue(result.isSuccess)
        // Falls back to getTextBeforeCursor path: deleteSurroundingText is called
        assertEquals(1, ic.deleteSurroundingTextCalls.size)
        assertEquals("helo wrld".length, ic.deleteSurroundingTextCalls[0].first)
    }

    // --- Private field detection (via isPrivateInputType — unit tested in PrivateFieldCheckTest) ---

    @Test
    fun `password inputType is detected as private`() {
        // TYPE_CLASS_TEXT = 0x1, TYPE_TEXT_VARIATION_PASSWORD = 0x80
        assertTrue(isPrivateInputType(0x00000001 or 0x00000080))
    }

    @Test
    fun `URI inputType is detected as private`() {
        // TYPE_CLASS_TEXT = 0x1, TYPE_TEXT_VARIATION_URI = 0x10
        assertTrue(isPrivateInputType(0x00000001 or 0x00000010))
    }

    @Test
    fun `normal text field is not private`() {
        assertFalse(isPrivateInputType(0x00000001))
    }
}
