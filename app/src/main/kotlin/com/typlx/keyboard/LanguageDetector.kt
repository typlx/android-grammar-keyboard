package com.typlx.keyboard

/**
 * Lightweight language detector using Unicode script analysis.
 *
 * Latin-script languages (EN/ES/FR/DE/PT) are indistinguishable without
 * n-gram models, so detection only resolves script family. The caller uses
 * this to auto-switch between Latin-script and Cyrillic-script keyboard modes.
 */
object LanguageDetector {

    private const val MIN_LETTER_COUNT = 6
    private const val SCRIPT_CONFIDENCE_THRESHOLD = 0.70f

    /**
     * Detects the dominant script in [text] and returns the first enabled language
     * that matches that script, or null if detection is inconclusive or no switch
     * is needed.
     *
     * Only switches when the detected script differs from [current]'s script —
     * avoids unnecessary churn when the user is already in the right mode.
     */
    fun detectLanguage(
        text: String,
        current: InputLanguage,
        enabled: List<InputLanguage>,
    ): InputLanguage? {
        if (enabled.size < 2) return null
        val letters = text.filter { it.isLetter() }
        if (letters.length < MIN_LETTER_COUNT) return null

        val script = dominantScript(letters) ?: return null
        if (script == current.scriptFamily) return null  // already on the right script

        return enabled.firstOrNull { it.scriptFamily == script && it != current }
    }

    private fun dominantScript(letters: String): ScriptFamily? {
        val cyrillicCount = letters.count { it in 'Ѐ'..'ӿ' }
        val latinCount = letters.count { Character.UnicodeScript.of(it.code) == Character.UnicodeScript.LATIN }
        val total = letters.length.toFloat()

        return when {
            cyrillicCount / total >= SCRIPT_CONFIDENCE_THRESHOLD -> ScriptFamily.CYRILLIC
            latinCount / total >= SCRIPT_CONFIDENCE_THRESHOLD -> ScriptFamily.LATIN
            else -> null
        }
    }
}
