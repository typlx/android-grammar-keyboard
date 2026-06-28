package com.typlx.keyboard

internal class GrammarUndoState {
    var canUndo: Boolean = false
        private set

    private var originalText: String? = null
    private var fixedText: String? = null

    fun recordFix(original: String, fixed: String) {
        originalText = original
        fixedText = fixed
        canUndo = true
    }

    /** No-op if undo state is already clear. */
    fun clear() {
        if (!canUndo) return
        canUndo = false
        originalText = null
        fixedText = null
    }

    /**
     * Returns the (originalText, fixedText) pair needed to perform the undo and clears state.
     * Returns null if no undo is available.
     */
    fun consume(): Pair<String, String>? {
        val original = originalText ?: return null
        val fixed = fixedText ?: return null
        canUndo = false
        originalText = null
        fixedText = null
        return Pair(original, fixed)
    }
}
