package com.typlx.keyboard

enum class TranslationLanguage(
    val displayLabel: String,
    val systemPrompt: String,
) {
    SPANISH(
        displayLabel = "Spanish",
        systemPrompt = "Translate the following text to Spanish. Return only the translated text, nothing else.",
    ),
    FRENCH(
        displayLabel = "French",
        systemPrompt = "Translate the following text to French. Return only the translated text, nothing else.",
    ),
    GERMAN(
        displayLabel = "German",
        systemPrompt = "Translate the following text to German. Return only the translated text, nothing else.",
    ),
    ITALIAN(
        displayLabel = "Italian",
        systemPrompt = "Translate the following text to Italian. Return only the translated text, nothing else.",
    ),
    PORTUGUESE(
        displayLabel = "Portuguese",
        systemPrompt = "Translate the following text to Portuguese. Return only the translated text, nothing else.",
    ),
    CHINESE_SIMPLIFIED(
        displayLabel = "Chinese",
        systemPrompt = "Translate the following text to Simplified Chinese. Return only the translated text, nothing else.",
    ),
    JAPANESE(
        displayLabel = "Japanese",
        systemPrompt = "Translate the following text to Japanese. Return only the translated text, nothing else.",
    ),
    KOREAN(
        displayLabel = "Korean",
        systemPrompt = "Translate the following text to Korean. Return only the translated text, nothing else.",
    ),
    ARABIC(
        displayLabel = "Arabic",
        systemPrompt = "Translate the following text to Arabic. Return only the translated text, nothing else.",
    ),
    RUSSIAN(
        displayLabel = "Russian",
        systemPrompt = "Translate the following text to Russian. Return only the translated text, nothing else.",
    ),
    HINDI(
        displayLabel = "Hindi",
        systemPrompt = "Translate the following text to Hindi. Return only the translated text, nothing else.",
    ),
    DUTCH(
        displayLabel = "Dutch",
        systemPrompt = "Translate the following text to Dutch. Return only the translated text, nothing else.",
    ),
    POLISH(
        displayLabel = "Polish",
        systemPrompt = "Translate the following text to Polish. Return only the translated text, nothing else.",
    ),
    SWEDISH(
        displayLabel = "Swedish",
        systemPrompt = "Translate the following text to Swedish. Return only the translated text, nothing else.",
    ),
    TURKISH(
        displayLabel = "Turkish",
        systemPrompt = "Translate the following text to Turkish. Return only the translated text, nothing else.",
    ),
}
