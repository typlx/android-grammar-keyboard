package com.typlx.keyboard

/** Returns true when the text before cursor ends with a sentence-ending pattern (e.g. ". "). */
internal fun shouldShiftAfterSpace(textBeforeCursor: String): Boolean =
    textBeforeCursor.endsWith(". ") ||
    textBeforeCursor.endsWith("! ") ||
    textBeforeCursor.endsWith("? ")

/** Returns true when the field's inputType flags indicate auto-capitalisation should apply. */
internal fun shouldAutoCapOnFieldFocus(inputType: Int): Boolean {
    val variation = inputType and android.text.InputType.TYPE_MASK_VARIATION
    if (variation == android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD ||
        variation == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
        variation == android.text.InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
        variation == android.text.InputType.TYPE_TEXT_VARIATION_URI ||
        variation == android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
        variation == android.text.InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
    ) return false
    val flags = inputType and android.text.InputType.TYPE_MASK_FLAGS
    return flags and (
        android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
        android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS or
        android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    ) != 0
}
