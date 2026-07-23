package com.typlx.keyboard

/** Manages a set of words that the grammar engine should never flag or rewrite.
 *  Pure class — no Android dependencies; persistence handled by the caller. */
class PersonalWordList(val maxSize: Int = 500) {

    companion object {
        /** Maximum custom words allowed on the free tier. Premium unlocks unlimited. */
        const val FREE_WORD_LIMIT = 100
    }

    private val words = mutableSetOf<String>()

    val size: Int get() = words.size

    /** Adds a word (lowercased, trimmed). Returns false if empty, duplicate, or at capacity. */
    fun add(word: String): Boolean {
        val normalized = word.trim().lowercase()
        if (normalized.isEmpty() || normalized in words || words.size >= maxSize) return false
        words.add(normalized)
        return true
    }

    /** Removes a word. No-op if not present. */
    fun remove(word: String) {
        words.remove(word.trim().lowercase())
    }

    /** Case-insensitive membership check. */
    fun contains(word: String): Boolean = word.trim().lowercase() in words

    /** Returns all words sorted alphabetically. */
    fun getAll(): List<String> = words.sorted()

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
            if (entry.isNotBlank()) words.add(entry.trim().lowercase())
        }
    }

    fun toJson(): String = buildString {
        append('[')
        val sorted = words.sorted()
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
}
