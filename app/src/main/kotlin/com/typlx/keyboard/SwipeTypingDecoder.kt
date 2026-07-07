package com.typlx.keyboard

class SwipeTypingDecoder {

    /**
     * Decodes a swipe path into candidate words.
     *
     * @param keyPath ordered list of key labels (lowercase) visited during the swipe
     * @param wordList dictionary to search
     * @param maxResults maximum number of candidates to return
     */
    fun decode(
        keyPath: List<String>,
        wordList: List<String>,
        maxResults: Int = 3,
    ): List<String> {
        if (keyPath.size < 2) return emptyList()

        val compact = dedup(keyPath.map { it.lowercase() })
        if (compact.length < 2) return emptyList()

        return wordList
            .asSequence()
            .filter { word -> word.length in 2..(compact.length + MAX_EXTRA) }
            .mapNotNull { word ->
                val score = scoreWord(compact, word.lowercase())
                if (score != Int.MAX_VALUE) Pair(word, score) else null
            }
            .sortedWith(compareBy({ it.second }, { it.first.length }))
            .map { it.first }
            .take(maxResults)
            .toList()
    }

    /**
     * Collapses runs of the same key to at most [MAX_CONSECUTIVE] occurrences.
     * Keeping 2 consecutive identical keys preserves double-letter words (e.g. "hello")
     * while still compressing long drifting runs on a single key.
     */
    private fun dedup(keys: List<String>): String {
        val sb = StringBuilder()
        var last = ""
        var run = 0
        for (k in keys) {
            if (k == last) {
                run++
                if (run <= MAX_CONSECUTIVE) sb.append(k)
            } else {
                sb.append(k)
                last = k
                run = 1
            }
        }
        return sb.toString()
    }

    /**
     * Returns the "extra keys" penalty for the word against the compact path,
     * or Int.MAX_VALUE if the word is not a subsequence of the path.
     */
    private fun scoreWord(compactPath: String, word: String): Int {
        if (!isSubsequence(word, compactPath)) return Int.MAX_VALUE
        val extra = compactPath.length - word.length
        return if (extra > MAX_EXTRA) Int.MAX_VALUE else extra
    }

    private fun isSubsequence(sub: String, str: String): Boolean {
        var si = 0
        for (c in str) {
            if (si < sub.length && c == sub[si]) si++
        }
        return si == sub.length
    }

    companion object {
        // Tolerate up to this many extra keys in the swipe path beyond the word length.
        internal const val MAX_EXTRA = 6
        // Keep at most this many consecutive identical keys when deduplicating (preserves double letters).
        private const val MAX_CONSECUTIVE = 2
    }
}
