package com.typlx.keyboard

import android.text.InputType

/**
 * Returns true if [inputType] represents a field whose content must not be sent to external APIs.
 * Password and URI fields are considered private — the keyboard should silently skip all
 * grammar-fix, tone-rewrite, translation, and auto-suggest API calls when this returns true.
 */
internal fun isPrivateInputType(inputType: Int): Boolean {
    val variation = inputType and InputType.TYPE_MASK_VARIATION
    return variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
        variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
        variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
        variation == InputType.TYPE_TEXT_VARIATION_URI
}
