package com.typlx.keyboard

/**
 * Supported keyboard input languages. Each language defines a display code,
 * human-readable name, BCP-47 locale tag, and the default physical layout.
 *
 * Free tier allows up to [FREE_TIER_LIMIT] enabled languages; premium
 * (via [FeatureGate.Feature.MULTI_LANGUAGE]) unlocks all entries.
 */
enum class InputLanguage(
    val code: String,
    val displayName: String,
    val localeTag: String,
    val defaultLayoutId: LayoutId,
) {
    ENGLISH("EN", "English", "en_US", LayoutId.QWERTY),
    UKRAINIAN("UK", "Ukrainian", "uk_UA", LayoutId.CYRILLIC),
    SPANISH("ES", "Spanish", "es_ES", LayoutId.QWERTY),
    FRENCH("FR", "French", "fr_FR", LayoutId.AZERTY),
    GERMAN("DE", "German", "de_DE", LayoutId.QWERTZ),
    PORTUGUESE("PT", "Portuguese", "pt_BR", LayoutId.QWERTY),
    ;

    /** Unicode script family used for auto-detection heuristics. */
    val scriptFamily: ScriptFamily
        get() = when (defaultLayoutId) {
            LayoutId.CYRILLIC -> ScriptFamily.CYRILLIC
            else -> ScriptFamily.LATIN
        }

    companion object {
        const val FREE_TIER_LIMIT = 2

        /** Ordered list used for cycling and Settings display. */
        val ALL: List<InputLanguage> = values().toList()

        fun fromCode(code: String): InputLanguage? = ALL.firstOrNull { it.code == code }
        fun fromName(name: String): InputLanguage? =
            runCatching { valueOf(name) }.getOrNull()

        /**
         * Pure function: given the current language and the ordered list of enabled
         * languages, returns the next language to switch to (wraps around). If fewer
         * than 2 languages are enabled, returns [current] unchanged.
         */
        fun nextLanguage(current: InputLanguage, enabled: List<InputLanguage>): InputLanguage {
            if (enabled.size < 2) return current
            val idx = enabled.indexOf(current)
            val effectiveIdx = if (idx < 0) 0 else idx
            return enabled[(effectiveIdx + 1) % enabled.size]
        }
    }
}

/** Unicode script category used for keyboard language auto-detection. */
enum class ScriptFamily { LATIN, CYRILLIC }
