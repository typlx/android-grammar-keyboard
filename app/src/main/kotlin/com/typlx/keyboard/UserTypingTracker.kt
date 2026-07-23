package com.typlx.keyboard

/**
 * Tracks how often the user commits each word so that frequently-typed words
 * surface as high-priority predictions. Pure class — no Android dependencies;
 * persistence is handled by the caller via [toJson] / [loadFromJson].
 *
 * A word is "learned" when it has been committed at least [MIN_LEARN_COUNT] times
 * and can then be returned by [getLearnedWords] to boost prediction priority.
 */
class UserTypingTracker(private val maxTracked: Int = 500) {

    private val counts = mutableMapOf<String, Int>()

    /**
     * Records that [word] was committed by the user.
     * Words shorter than 2 characters or consisting entirely of digits are ignored.
     * Once [maxTracked] distinct words are stored, new words are silently dropped.
     */
    fun recordWord(word: String) {
        val normalized = word.trim().lowercase()
        if (normalized.length < 2) return
        if (normalized.all { it.isDigit() }) return
        if (!counts.containsKey(normalized) && counts.size >= maxTracked) return
        counts[normalized] = (counts[normalized] ?: 0) + 1
    }

    /**
     * Returns words that have been committed at least [minCount] times,
     * sorted by descending frequency, capped at [maxWords].
     */
    fun getLearnedWords(minCount: Int = 3, maxWords: Int = 50): List<String> =
        counts.entries
            .filter { it.value >= minCount }
            .sortedByDescending { it.value }
            .take(maxWords)
            .map { it.key }

    /** Returns the commit count for [word], or 0 if not tracked. */
    fun getCount(word: String): Int = counts[word.trim().lowercase()] ?: 0

    /** Number of distinct words being tracked. */
    val size: Int get() = counts.size

    fun clear() = counts.clear()

    fun toJson(): String = buildString {
        append('{')
        counts.entries.sortedBy { it.key }.forEachIndexed { i, (word, count) ->
            if (i > 0) append(',')
            append('"')
            append(word.replace("\\", "\\\\").replace("\"", "\\\""))
            append('"')
            append(':')
            append(count)
        }
        append('}')
    }

    fun loadFromJson(json: String) {
        counts.clear()
        val trimmed = json.trim()
        if (!trimmed.startsWith('{') || !trimmed.endsWith('}')) return
        val pattern = Regex(""""((?:[^"\\]|\\.)*)"\s*:\s*(\d+)""")
        var loaded = 0
        for (match in pattern.findAll(trimmed)) {
            if (loaded >= maxTracked) break
            val word = match.groupValues[1]
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
            val count = match.groupValues[2].toIntOrNull() ?: continue
            if (word.isNotBlank() && count > 0) {
                counts[word] = count
                loaded++
            }
        }
    }

    companion object {
        /** Minimum commit count before a word surfaces as a learned suggestion. */
        const val MIN_LEARN_COUNT = 3
    }
}
