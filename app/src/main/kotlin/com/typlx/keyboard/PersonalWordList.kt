package com.typlx.keyboard

/** Manages a set of words that the grammar engine should never flag or rewrite.
 *  Pure class — no Android dependencies; persistence handled by the caller. */
class PersonalWordList(val maxSize: Int = 500) {

    companion object {
        /** Maximum custom words allowed on the free tier. Premium unlocks unlimited. */
        const val FREE_WORD_LIMIT = 100
    }

    // lowercase key → original-cased value, for case-insensitive dedup + casing preservation
    private val words = mutableMapOf<String, String>()

    val size: Int get() = words.size

    /** Adds a word, preserving original casing. Returns false if empty, duplicate, or at capacity. */
    fun add(word: String): Boolean {
        val trimmed = word.trim()
        val key = trimmed.lowercase()
        if (key.isEmpty() || key in words || words.size >= maxSize) return false
        words[key] = trimmed
        return true
    }

    /** Removes a word. No-op if not present. */
    fun remove(word: String) {
        words.remove(word.trim().lowercase())
    }

    /** Case-insensitive membership check. */
    fun contains(word: String): Boolean = word.trim().lowercase() in words

    /** Returns all words sorted alphabetically (case-insensitive), with original casing. */
    fun getAll(): List<String> = words.values.sortedWith(String.CASE_INSENSITIVE_ORDER)

    /**
     * Returns true when the only textual differences between [original] and [corrected]
     * are tokens found in the personal word list. When true, the correction should be
     * suppressed — the grammar engine is only "fixing" words the user considers correct.
     *
     * Token-by-token comparison: suppresses when every changed token in [original] is a
     * known personal word (the API may replace it with a different word, which we reject).
     */
    fun shouldSuppressCorrection(original: String, corrected: String): Boolean {
        if (original == corrected) return true
        if (words.isEmpty()) return false

        val origTokens = original.split(Regex("\\s+")).filter { it.isNotEmpty() }
        val corrTokens = corrected.split(Regex("\\s+")).filter { it.isNotEmpty() }

        // Structural change (different word count) — don't suppress
        if (origTokens.size != corrTokens.size) return false

        for (i in origTokens.indices) {
            val ot = origTokens[i].trimEnd('.', ',', '!', '?', ';', ':').lowercase()
            val ct = corrTokens[i].trimEnd('.', ',', '!', '?', ';', ':').lowercase()
            if (ot != ct && ot !in words) return false
        }
        return true
    }

    fun loadFromJson(json: String) {
        words.clear()
        val trimmed = json.trim()
        if (!trimmed.startsWith('[') || !trimmed.endsWith(']')) return
        val inner = trimmed.substring(1, trimmed.length - 1)
        val pattern = Regex(""""((?:[^"\\]|\\.)*)"""")
        for (match in pattern.findAll(inner)) {
            if (words.size >= maxSize) break
            val entry = match.groupValues[1]
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
            if (entry.isNotBlank()) {
                val e = entry.trim()
                words[e.lowercase()] = e
            }
        }
    }

    fun toJson(): String = buildString {
        append('[')
        val sorted = words.values.sortedWith(String.CASE_INSENSITIVE_ORDER)
        sorted.forEachIndexed { i, word ->
            if (i > 0) append(',')
            append('"')
            append(word.replace("\\", "\\\\").replace("\"", "\\\""))
            append('"')
        }
        append(']')
    }

    /** Returns export text: one word per line, newline-terminated. */
    fun toExportText(): String = buildString {
        getAll().forEach { word -> append(word).append('\n') }
    }

    /**
     * Parses import text (one word per line).
     * Blank lines and whitespace-only lines are silently skipped.
     * Duplicate or capacity-exceeded words are silently skipped.
     * Returns the count of words actually added.
     */
    fun importFromText(text: String): Int =
        text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .count { add(it) }

    /**
     * Adds every token from [original] that differs from its counterpart in [corrected].
     * Used when the user long-presses a grammar suggestion to suppress that specific correction.
     * Returns true if at least one word was successfully added.
     * Returns false when the token counts differ (structural change) or nothing was added.
     */
    fun addChangedTokens(original: String, corrected: String): Boolean {
        val origTokens = original.split(Regex("\\s+")).filter { it.isNotEmpty() }
        val corrTokens = corrected.split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (origTokens.size != corrTokens.size) return false
        var added = false
        for (i in origTokens.indices) {
            val ot = origTokens[i].trimEnd('.', ',', '!', '?', ';', ':').lowercase()
            val ct = corrTokens[i].trimEnd('.', ',', '!', '?', ';', ':').lowercase()
            if (ot != ct && add(ot)) added = true
        }
        return added
    }
}
