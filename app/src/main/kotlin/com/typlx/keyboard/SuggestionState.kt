package com.typlx.keyboard

sealed class SuggestionState {
    object Idle : SuggestionState()
    object Loading : SuggestionState()
    data class Available(
        val original: String,
        val corrected: String,
        val diff: List<DiffSegment> = emptyList(),
    ) : SuggestionState()
    data class WordSuggestions(val words: List<String>, val emojis: List<String> = emptyList()) : SuggestionState()
}
