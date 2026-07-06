package com.typlx.keyboard

import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.inputmethod.CompletionInfo
import android.view.inputmethod.CorrectionInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputContentInfo

/**
 * Test double for InputConnection.
 *
 * Simulates a simple text buffer. Meaningful implementations:
 *   - getTextBeforeCursor  — returns the last n chars of the buffer
 *   - commitText           — appends text (or replaces selection) and records the call
 *   - deleteSurroundingText — removes chars before cursor and records the call
 *   - getSelectedText      — returns [simulatedSelection] when set
 *
 * Everything else is a no-op so tests can focus on the interactions above.
 *
 * Reusable across test files.
 */
class MockInputConnection(initialText: String = "") : InputConnection {

    private val buffer = StringBuilder(initialText)

    /** Set this to simulate selected text; commitText() will replace it in the buffer. */
    var simulatedSelection: String? = null

    /** All (text, newCursorPosition) pairs passed to commitText in call order. */
    val commitTextCalls = mutableListOf<Pair<String, Int>>()

    /** All (beforeLength, afterLength) pairs passed to deleteSurroundingText in call order. */
    val deleteSurroundingTextCalls = mutableListOf<Pair<Int, Int>>()

    /** The n value from the most recent getTextBeforeCursor call. */
    var lastGetTextBeforeCursorN = 0
        private set

    /** Current buffer content — reflects all commits and deletions. */
    fun currentText(): String = buffer.toString()

    // --- Meaningful implementations ---

    override fun getTextBeforeCursor(n: Int, flags: Int): CharSequence {
        lastGetTextBeforeCursorN = n
        val text = buffer.toString()
        return if (n >= text.length) text else text.substring(text.length - n)
    }

    override fun commitText(text: CharSequence, newCursorPosition: Int): Boolean {
        commitTextCalls.add(text.toString() to newCursorPosition)
        val sel = simulatedSelection
        if (sel != null) {
            // Simulate replacing selection in-place.
            val idx = buffer.indexOf(sel)
            if (idx >= 0) buffer.replace(idx, idx + sel.length, text.toString())
            else buffer.append(text)
            simulatedSelection = null
        } else {
            buffer.append(text)
        }
        return true
    }

    override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
        deleteSurroundingTextCalls.add(beforeLength to afterLength)
        val start = maxOf(0, buffer.length - beforeLength)
        buffer.delete(start, buffer.length)
        return true
    }

    // --- No-op implementations for the rest of the interface ---

    override fun getTextAfterCursor(n: Int, flags: Int): CharSequence = ""
    override fun getSelectedText(flags: Int): CharSequence? = simulatedSelection
    override fun getExtractedText(request: ExtractedTextRequest?, flags: Int): ExtractedText? = null
    override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean = false
    override fun setComposingText(text: CharSequence, newCursorPosition: Int): Boolean = false
    override fun setComposingRegion(start: Int, end: Int): Boolean = false
    override fun finishComposingText(): Boolean = false
    override fun commitCompletion(text: CompletionInfo?): Boolean = false
    override fun commitCorrection(correctionInfo: CorrectionInfo?): Boolean = false
    override fun setSelection(start: Int, end: Int): Boolean = false
    override fun performEditorAction(editorAction: Int): Boolean = false
    override fun performContextMenuAction(id: Int): Boolean = false
    override fun beginBatchEdit(): Boolean = false
    override fun endBatchEdit(): Boolean = false
    override fun sendKeyEvent(event: KeyEvent?): Boolean = false
    override fun clearMetaKeyStates(states: Int): Boolean = false
    override fun reportFullscreenMode(enabled: Boolean): Boolean = false
    override fun performPrivateCommand(action: String?, data: Bundle?): Boolean = false
    override fun requestCursorUpdates(cursorUpdateMode: Int): Boolean = false
    override fun getCursorCapsMode(reqModes: Int): Int = 0
    override fun getHandler(): Handler? = null
    override fun closeConnection() {}
    override fun commitContent(inputContentInfo: InputContentInfo, flags: Int, opts: Bundle?): Boolean = false
}
