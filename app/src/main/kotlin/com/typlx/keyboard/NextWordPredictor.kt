package com.typlx.keyboard

/**
 * Offline next-word prediction using a curated English bigram map.
 *
 * Used when the user is between words (no current prefix): the keyboard passes
 * the last committed word and gets back up to [maxResults] likely next words.
 * All lookups are < 1 ms — no network involved.
 */
class NextWordPredictor {

    fun predict(lastWord: String, maxResults: Int = 3): List<String> {
        if (lastWord.isBlank()) return emptyList()
        val key = lastWord.lowercase().trimEnd('.', '!', '?', ',', ';', ':', '"', '\'')
        return BIGRAMS[key]?.take(maxResults) ?: emptyList()
    }

    companion object {
        internal val BIGRAMS: Map<String, List<String>> = mapOf(
            // Pronouns
            "i" to listOf("am", "have", "think", "want", "will"),
            "you" to listOf("can", "are", "will", "have", "should"),
            "we" to listOf("can", "have", "will", "need", "should"),
            "they" to listOf("are", "have", "will", "can", "said"),
            "it" to listOf("is", "was", "has", "will", "seems"),
            "he" to listOf("is", "was", "has", "will", "said"),
            "she" to listOf("is", "was", "has", "will", "said"),
            // Articles / determiners
            "the" to listOf("most", "best", "first", "last", "same"),
            "a" to listOf("great", "good", "new", "few", "lot"),
            // Common verbs
            "is" to listOf("a", "the", "not", "very", "now"),
            "are" to listOf("you", "we", "they", "the", "a"),
            "was" to listOf("a", "the", "not", "very", "just"),
            "have" to listOf("a", "been", "to", "the", "your"),
            "can" to listOf("you", "we", "help", "also", "do"),
            "will" to listOf("be", "you", "help", "have", "make"),
            "would" to listOf("you", "be", "like", "love", "help"),
            "could" to listOf("you", "be", "also", "help", "have"),
            "should" to listOf("be", "we", "you", "have", "also"),
            "want" to listOf("to", "you", "your", "a", "the"),
            "need" to listOf("to", "your", "a", "the", "help"),
            "think" to listOf("about", "we", "that", "it", "the"),
            "know" to listOf("what", "how", "that", "if", "the"),
            "see" to listOf("you", "the", "if", "what", "how"),
            "get" to listOf("the", "a", "back", "more", "your"),
            "make" to listOf("sure", "it", "the", "a", "your"),
            "go" to listOf("to", "ahead", "back", "for", "with"),
            "let" to listOf("me", "us", "know", "it", "the"),
            "hope" to listOf("you", "this", "it", "we", "to"),
            "looking" to listOf("forward", "for", "good", "at", "into"),
            // Prepositions / conjunctions
            "in" to listOf("the", "a", "your", "our", "this"),
            "on" to listOf("the", "a", "your", "our", "this"),
            "for" to listOf("you", "the", "a", "your", "more"),
            "with" to listOf("you", "the", "a", "your", "our"),
            "from" to listOf("the", "a", "your", "our", "this"),
            "to" to listOf("the", "a", "your", "our", "be"),
            "be" to listOf("the", "a", "able", "sure", "there"),
            "at" to listOf("the", "a", "your", "least", "all"),
            "if" to listOf("you", "we", "the", "there", "possible"),
            "so" to listOf("much", "many", "far", "good", "great"),
            "not" to listOf("sure", "only", "just", "really", "yet"),
            "just" to listOf("a", "want", "need", "got", "said"),
            "all" to listOf("the", "of", "right", "good", "about"),
            // Greetings / closings
            "hello" to listOf("there", "everyone", "team", "world"),
            "hi" to listOf("there", "everyone", "team", "all"),
            "hey" to listOf("there", "everyone", "all"),
            "dear" to listOf("team", "all", "everyone"),
            "thanks" to listOf("for", "so", "again", "a"),
            "thank" to listOf("you"),
            "please" to listOf("find", "note", "let", "be", "make"),
            "sorry" to listOf("for", "about", "to", "if", "I"),
            // Common phrases
            "good" to listOf("morning", "afternoon", "evening", "night", "luck"),
            "happy" to listOf("birthday", "to", "with", "new", "about"),
            "great" to listOf("job", "work", "idea", "point", "question"),
            "how" to listOf("are", "do", "can", "much", "many"),
            "what" to listOf("do", "is", "are", "if", "about"),
            "this" to listOf("is", "was", "will", "should", "means"),
            "that" to listOf("is", "was", "will", "could", "means"),
            "there" to listOf("is", "are", "was", "were", "will"),
        )
    }
}
