package com.typlx.keyboard

/**
 * Returns the number of characters to delete before the cursor for a "delete word" action.
 * Skips trailing whitespace, then deletes the preceding word.
 * Returns 0 for empty input.
 */
internal fun wordDeleteCount(text: String): Int {
    if (text.isEmpty()) return 0
    var count = 0
    var i = text.length - 1
    while (i >= 0 && text[i].isWhitespace()) { count++; i-- }
    while (i >= 0 && !text[i].isWhitespace()) { count++; i-- }
    return maxOf(count, 1)
}
