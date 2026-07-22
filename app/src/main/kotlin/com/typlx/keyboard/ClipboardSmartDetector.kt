package com.typlx.keyboard

/**
 * Detects structured content in clipboard text and returns a quick-paste label.
 * Used to surface a smart-paste chip in the suggestion strip.
 */
object ClipboardSmartDetector {

    sealed class Detection {
        data class Otp(val text: String) : Detection()
        data class PhoneNumber(val text: String) : Detection()
        data class Url(val text: String) : Detection()
        data class Email(val text: String) : Detection()
        object None : Detection()
    }

    private val OTP_REGEX = Regex("""^\d{4,6}$""")
    private val PHONE_REGEX = Regex("""^\+?[\d\s\-().]{7,15}$""")
    private val URL_REGEX = Regex("""^https?://\S+$""", RegexOption.IGNORE_CASE)
    private val EMAIL_REGEX = Regex("""^\S+@\S+\.\S{2,}$""")

    fun detect(text: String): Detection {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || trimmed.length > 200) return Detection.None
        return when {
            OTP_REGEX.matches(trimmed) -> Detection.Otp(trimmed)
            URL_REGEX.matches(trimmed) -> Detection.Url(trimmed)
            EMAIL_REGEX.matches(trimmed) -> Detection.Email(trimmed)
            PHONE_REGEX.matches(trimmed) -> Detection.PhoneNumber(trimmed)
            else -> Detection.None
        }
    }

    fun chipLabel(detection: Detection): String = when (detection) {
        is Detection.Otp -> "Paste OTP: ${detection.text.take(12)}"
        is Detection.PhoneNumber -> "Paste number: ${detection.text.take(12)}"
        is Detection.Url -> "Paste URL: ${detection.text.take(30).let { if (detection.text.length > 30) "$it…" else it }}"
        is Detection.Email -> "Paste email: ${detection.text.take(20).let { if (detection.text.length > 20) "$it…" else it }}"
        Detection.None -> ""
    }
}
