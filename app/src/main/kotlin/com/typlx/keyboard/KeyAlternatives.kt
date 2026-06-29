package com.typlx.keyboard

val KEY_ALTERNATIVES: Map<String, List<String>> = mapOf(
    "a" to listOf("à", "á", "â", "ã", "ä", "å", "æ"),
    "c" to listOf("ç"),
    "d" to listOf("ð"),
    "e" to listOf("è", "é", "ê", "ë"),
    "g" to listOf("ğ"),
    "i" to listOf("ì", "í", "î", "ï"),
    "l" to listOf("ł"),
    "n" to listOf("ñ"),
    "o" to listOf("ò", "ó", "ô", "õ", "ö", "ø", "œ"),
    "s" to listOf("ß", "š", "ś"),
    "t" to listOf("þ"),
    "u" to listOf("ù", "ú", "û", "ü"),
    "y" to listOf("ý", "ÿ"),
    "z" to listOf("ž", "ź", "ż"),
)

/**
 * Returns locale-variant alternatives for [key], uppercased when [isCaps] is true.
 * Returns null when no alternatives exist for the key.
 */
fun getAlternatives(key: String, isCaps: Boolean): List<String>? {
    val alts = KEY_ALTERNATIVES[key.lowercase()] ?: return null
    return if (isCaps) alts.map { it.uppercase() } else alts
}
