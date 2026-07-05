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
    REPHRASE(
        displayLabel = "Rephrase",
        systemPrompt = "Rewrite the following text in a more natural and fluent way while preserving its meaning. Return only the rewritten text, nothing else.",
    ),
    SIMPLIFY(
        displayLabel = "Simplify",
        systemPrompt = "Rewrite the following text using simpler language that is easy for anyone to understand. Return only the rewritten text, nothing else.",
    ),
    EXPAND(
        displayLabel = "Expand",
        systemPrompt = "Rewrite the following text with more detail and explanation while staying focused on the original topic. Return only the rewritten text, nothing else.",
    ),
}
