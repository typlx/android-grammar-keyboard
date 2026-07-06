package com.typlx.keyboard

class WordPredictor {

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

        val common = COMMON_WORDS
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
        // ~500 most common English words, sorted by frequency (most common first)
        private val COMMON_WORDS = listOf(
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
            "any", "these", "give", "day", "most", "us", "between", "need",
            "large", "often", "hand", "high", "place", "hold", "turn", "start",
            "show", "hear", "play", "run", "move", "live", "believe", "hold",
            "bring", "happen", "must", "write", "provide", "sit", "stand",
            "lose", "pay", "meet", "include", "continue", "set", "learn",
            "change", "lead", "understand", "watch", "follow", "stop", "create",
            "speak", "read", "spend", "grow", "open", "walk", "win", "offer",
            "remember", "love", "consider", "appear", "buy", "wait", "serve",
            "seem", "ask", "try", "call", "feel", "help", "talk", "send",
            "expect", "build", "stay", "fall", "cut", "reach", "kill", "remain",
            "suggest", "raise", "pass", "sell", "decide", "return", "explain",
            "hope", "develop", "carry", "break", "receive", "agree", "support",
            "hit", "produce", "eat", "cover", "catch", "draw", "choose", "cause",
            "require", "become", "place", "allow", "point", "quite", "little",
            "being", "know", "feel", "both", "life", "state", "never", "still",
            "every", "world", "left", "right", "same", "however", "might",
            "another", "again", "during", "each", "something", "through",
            "while", "before", "without", "many", "much", "city", "here",
            "number", "group", "often", "always", "together", "next", "free",
            "office", "company", "close", "problem", "system", "program",
            "question", "against", "school", "different", "home", "family",
            "although", "himself", "herself", "themselves", "everything",
            "nothing", "someone", "anyone", "everyone", "anything", "somewhere",
            "really", "because", "already", "certainly", "possible", "probably",
            "usually", "actually", "quickly", "simply", "clearly", "almost",
            "perhaps", "especially", "recently", "directly", "suddenly",
            "immediately", "certainly", "definitely", "obviously", "strongly",
            "several", "whether", "between", "matter", "thought", "rather",
            "enough", "around", "example", "called", "known", "given",
            "further", "getting", "having", "making", "taking", "going",
            "saying", "looking", "thinking", "coming", "working", "trying",
            "using", "following", "through", "including", "during", "according",
            "across", "against", "within", "without", "along", "since",
            "before", "after", "above", "below", "under", "beyond", "toward",
            "until", "while", "though", "since", "unless", "because",
            "better", "best", "great", "important", "national", "local",
            "public", "private", "real", "true", "early", "late", "long",
            "short", "small", "big", "hard", "easy", "simple", "clear",
            "strong", "light", "dark", "young", "old", "white", "black",
            "full", "open", "far", "near", "wide", "deep", "low", "slow",
            "fast", "fine", "kind", "nice", "cold", "hot", "fresh", "rich",
            "poor", "safe", "free", "ready", "able", "available", "certain",
            "different", "difficult", "possible", "general", "natural",
            "military", "political", "economic", "social", "human", "personal",
            "physical", "special", "specific", "major", "central", "original",
            "ancient", "modern", "common", "medical", "scientific", "legal",
            "financial", "educational", "environmental", "international",
            "million", "billion", "hundred", "thousand", "minutes", "hours",
            "weeks", "months", "years", "morning", "evening", "afternoon",
            "tonight", "today", "yesterday", "tomorrow", "monday", "tuesday",
            "wednesday", "thursday", "friday", "saturday", "sunday",
            "january", "february", "march", "april", "june", "july",
            "august", "september", "october", "november", "december",
            "thanks", "thank", "please", "sorry", "hello", "goodbye",
            "welcome", "okay", "great", "perfect", "wonderful", "amazing",
            "interesting", "beautiful", "important", "necessary", "useful",
            "wonderful", "excellent", "fantastic", "terrible", "horrible",
            "information", "knowledge", "experience", "development", "management",
            "community", "technology", "government", "department", "organization",
            "situation", "opportunity", "relationship", "environment",
            "performance", "responsibility", "investment", "consideration",
            "improvement", "communication", "understanding", "professional",
            "successful", "significant", "traditional", "individual", "particular",
            "additional", "appropriate", "commercial", "comprehensive",
            "throughout", "themselves", "absolutely", "completely", "different",
            "especially", "everything", "following", "government", "including",
            "something", "sometimes", "therefore", "themselves"
        )
    }
}
