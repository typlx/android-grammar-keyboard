package com.typlx.keyboard

enum class ToneOption(
    val displayLabel: String,
    val systemPrompt: String,
) {
    PROFESSIONAL(
        displayLabel = "Professional",
        systemPrompt = "Rewrite the following text in a professional tone suitable for workplace communication. Return only the rewritten text, nothing else.",
    ),
    CASUAL(
        displayLabel = "Casual",
        systemPrompt = "Rewrite the following text in a casual, friendly tone. Return only the rewritten text, nothing else.",
    ),
    FORMAL(
        displayLabel = "Formal",
        systemPrompt = "Rewrite the following text in a formal tone. Return only the rewritten text, nothing else.",
    ),
    FRIENDLY(
        displayLabel = "Friendly",
        systemPrompt = "Rewrite the following text in a warm, friendly and approachable tone. Return only the rewritten text, nothing else.",
    ),
    CONCISE(
        displayLabel = "Concise",
        systemPrompt = "Rewrite the following text as concisely as possible while preserving meaning. Return only the rewritten text, nothing else.",
    ),
}
