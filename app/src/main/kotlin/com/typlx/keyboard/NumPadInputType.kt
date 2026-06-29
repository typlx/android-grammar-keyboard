package com.typlx.keyboard

import android.text.InputType

data class NumPadConfig(
    val isNumPad: Boolean,
    val isPhoneMode: Boolean,
    val isDecimalAllowed: Boolean,
    val isSignedAllowed: Boolean,
)

/** Returns the numpad configuration for a given EditorInfo inputType. */
fun numPadConfig(inputType: Int): NumPadConfig {
    return when (inputType and InputType.TYPE_MASK_CLASS) {
        InputType.TYPE_CLASS_PHONE -> NumPadConfig(
            isNumPad = true,
            isPhoneMode = true,
            isDecimalAllowed = false,
            isSignedAllowed = false,
        )
        InputType.TYPE_CLASS_NUMBER -> NumPadConfig(
            isNumPad = true,
            isPhoneMode = false,
            isDecimalAllowed = (inputType and InputType.TYPE_NUMBER_FLAG_DECIMAL) != 0,
            isSignedAllowed = (inputType and InputType.TYPE_NUMBER_FLAG_SIGNED) != 0,
        )
        else -> NumPadConfig(
            isNumPad = false,
            isPhoneMode = false,
            isDecimalAllowed = false,
            isSignedAllowed = false,
        )
    }
}
