package com.typlx.keyboard

import android.content.Context

class WordPredictor(val wordList: List<String> = FALLBACK_WORDS) {

    fun predict(
        prefix: String,
        personalWords: Collection<String> = emptyList(),
        maxResults: Int = 3,
    ): List<String> {
        if (prefix.length < 2) return emptyList()

        val lowerPrefix = prefix.lowercase()
        val capitalize = prefix[0].isUpperCase()
        val allUpperCase = prefix.all { it.isUpperCase() }

        val personal = personalWords
            .filter { it.lowercase().startsWith(lowerPrefix) && !it.equals(prefix, ignoreCase = true) }
            .take(maxResults)
            .map { applyCase(it, capitalize, allUpperCase) }

        val remaining = maxResults - personal.size
        if (remaining == 0) return personal

        val common = wordList
            .asSequence()
            .filter { it.startsWith(lowerPrefix) && !it.equals(lowerPrefix, ignoreCase = true) }
            .filter { candidate -> personal.none { it.equals(candidate, ignoreCase = true) } }
            .take(remaining)
            .map { applyCase(it, capitalize, allUpperCase) }
            .toList()

        return personal + common
    }

    private fun applyCase(word: String, capitalize: Boolean, allUpperCase: Boolean): String = when {
        allUpperCase -> word.uppercase()
        capitalize -> word.replaceFirstChar { it.uppercaseChar() }
        else -> word
    }

    companion object {
        fun fromContext(context: Context): WordPredictor {
            val words = try {
                context.resources.openRawResource(R.raw.word_list)
                    .bufferedReader()
                    .readLines()
                    .filter { it.isNotBlank() }
            } catch (_: Exception) {
                FALLBACK_WORDS
            }
            return WordPredictor(words)
        }

        // Kept for tests that instantiate WordPredictor() without a Context.
        // Also used as fallback if the raw resource cannot be read.
        internal val FALLBACK_WORDS = listOf(
            "the", "be", "to", "of", "and", "in", "that", "have", "it", "for",
            "not", "on", "with", "he", "as", "you", "do", "at", "this", "but",
            "his", "by", "from", "they", "we", "say", "her", "she", "or", "an",
            "will", "my", "one", "all", "would", "there", "their", "what", "so",
            "up", "out", "if", "about", "who", "get", "which", "go", "me", "when",
            "make", "can", "like", "time", "no", "just", "him", "know", "take",
            "people", "into", "year", "your", "good", "some", "could", "them",
            "see", "other", "than", "then", "now", "look", "only", "come", "its",
            "over", "think", "also", "back", "after", "use", "two", "how", "our",
            "work", "first", "well", "way", "even", "new", "want", "because",
            "any", "these", "give", "day", "most", "us", "great", "between",
            "need", "large", "often", "hand", "high", "place", "hold", "turn",
            "start", "show", "hear", "play", "run", "move", "live", "believe",
            "bring", "happen", "must", "write", "provide", "include", "continue",
            "change", "lead", "understand", "follow", "stop", "create", "learn",
            "remember", "love", "consider", "appear", "buy", "feel", "help",
            "think", "call", "expect", "build", "stay", "fall", "reach", "remain",
            "suggest", "develop", "carry", "break", "receive", "agree", "support",
            "better", "best", "great", "important", "national", "local", "public",
            "real", "true", "early", "late", "long", "short", "small", "big",
            "hard", "easy", "simple", "clear", "strong", "young", "old", "full",
            "really", "already", "certainly", "possible", "probably", "usually",
            "actually", "quickly", "simply", "clearly", "almost", "perhaps",
            "especially", "recently", "definitely", "obviously", "several",
            "whether", "matter", "thought", "rather", "enough", "around",
            "example", "information", "knowledge", "experience", "development",
            "community", "technology", "government", "department", "organization",
            "situation", "opportunity", "relationship", "environment",
            "performance", "responsibility", "communication", "understanding",
            "beautiful", "important", "necessary", "excellent", "fantastic",
            "something", "everything", "nothing", "someone", "anyone", "everyone",
            "together", "morning", "evening", "afternoon", "tonight", "today",
            "yesterday", "tomorrow", "thanks", "please", "sorry", "hello",
            "welcome", "okay", "perfect", "wonderful", "amazing", "interesting"
        )

        // Alias kept for SwipeTypingDecoder call-site in GrammarKeyboardService
        @Deprecated("Use wordList property on instance instead", ReplaceWith("wordPredictor.wordList"))
        internal val COMMON_WORDS get() = FALLBACK_WORDS
    }
}
