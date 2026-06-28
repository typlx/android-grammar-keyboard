package com.typlx.keyboard

sealed class SuggestionState {
    object Idle : SuggestionState()
    object Loading : SuggestionState()
    data class Available(val original: String, val corrected: String) : SuggestionState()
}
