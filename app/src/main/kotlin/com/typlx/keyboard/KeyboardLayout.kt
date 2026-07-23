package com.typlx.keyboard

enum class LayoutId { QWERTY, AZERTY, QWERTZ, DVORAK, CYRILLIC }

data class KeyboardLayout(
    val id: LayoutId,
    val displayName: String,
    val row1: List<String>,
    val row2: List<String>,
    val row3: List<String>,
    val longPressAlternatives: Map<String, List<String>> = KEY_ALTERNATIVES,
)

val LAYOUT_QWERTY = KeyboardLayout(
    id = LayoutId.QWERTY,
    displayName = "QWERTY",
    row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
    row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
    row3 = listOf("z", "x", "c", "v", "b", "n", "m"),
)

val LAYOUT_AZERTY = KeyboardLayout(
    id = LayoutId.AZERTY,
    displayName = "AZERTY",
    row1 = listOf("a", "z", "e", "r", "t", "y", "u", "i", "o", "p"),
    row2 = listOf("q", "s", "d", "f", "g", "h", "j", "k", "l", "m"),
    row3 = listOf("w", "x", "c", "v", "b", "n"),
    longPressAlternatives = KEY_ALTERNATIVES + mapOf(
        "a" to listOf("à", "â", "æ"),
        "e" to listOf("é", "è", "ê", "ë"),
        "i" to listOf("î", "ï"),
        "o" to listOf("ô", "ö", "œ"),
        "u" to listOf("ù", "û", "ü"),
        "c" to listOf("ç"),
    ),
)

val LAYOUT_QWERTZ = KeyboardLayout(
    id = LayoutId.QWERTZ,
    displayName = "QWERTZ",
    row1 = listOf("q", "w", "e", "r", "t", "z", "u", "i", "o", "p"),
    row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
    row3 = listOf("y", "x", "c", "v", "b", "n", "m"),
    longPressAlternatives = KEY_ALTERNATIVES + mapOf(
        "a" to listOf("ä", "à", "á", "â", "ã", "å", "æ"),
        "o" to listOf("ö", "ò", "ó", "ô", "õ", "ø", "œ"),
        "u" to listOf("ü", "ù", "ú", "û"),
        "s" to listOf("ß", "š", "ś"),
        "e" to listOf("è", "é", "ê", "ë"),
    ),
)

val LAYOUT_DVORAK = KeyboardLayout(
    id = LayoutId.DVORAK,
    displayName = "Dvorak",
    row1 = listOf("'", ",", ".", "p", "y", "f", "g", "c", "r", "l"),
    row2 = listOf("a", "o", "e", "u", "i", "d", "h", "t", "n", "s"),
    row3 = listOf(";", "q", "j", "k", "x", "b", "m", "w", "v", "z"),
    longPressAlternatives = mapOf(
        "a" to listOf("à", "á", "â", "ã", "ä", "å", "æ"),
        "c" to listOf("ç"),
        "e" to listOf("è", "é", "ê", "ë"),
        "i" to listOf("ì", "í", "î", "ï"),
        "n" to listOf("ñ"),
        "o" to listOf("ò", "ó", "ô", "õ", "ö", "ø", "œ"),
        "s" to listOf("ß", "š", "ś"),
        "u" to listOf("ù", "ú", "û", "ü"),
    ),
)

val LAYOUT_CYRILLIC = KeyboardLayout(
    id = LayoutId.CYRILLIC,
    displayName = "Кирилиця",
    row1 = listOf("й", "ц", "у", "к", "е", "н", "г", "ш", "щ", "з"),
    row2 = listOf("ф", "и", "в", "а", "п", "р", "о", "л", "д"),
    row3 = listOf("я", "ч", "с", "м", "т", "ь", "б"),
    longPressAlternatives = mapOf(
        "е" to listOf("є", "э"),
        "з" to listOf("х", "ї"),
        "и" to listOf("і"),
        "д" to listOf("ж"),
        "б" to listOf("ю"),
        "г" to listOf("ґ"),
    ),
)

val ALL_LAYOUTS = listOf(LAYOUT_QWERTY, LAYOUT_AZERTY, LAYOUT_QWERTZ, LAYOUT_DVORAK, LAYOUT_CYRILLIC)

fun layoutById(id: LayoutId): KeyboardLayout = ALL_LAYOUTS.first { it.id == id }
