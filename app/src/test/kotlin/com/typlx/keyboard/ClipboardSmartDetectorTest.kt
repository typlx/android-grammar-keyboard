package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class ClipboardSmartDetectorTest {

    // OTP detection

    @Test
    fun `6-digit number is detected as OTP`() {
        val result = ClipboardSmartDetector.detect("123456")
        assertTrue(result is ClipboardSmartDetector.Detection.Otp)
    }

    @Test
    fun `4-digit number is detected as OTP`() {
        val result = ClipboardSmartDetector.detect("9876")
        assertTrue(result is ClipboardSmartDetector.Detection.Otp)
    }

    @Test
    fun `8-digit number is detected as OTP`() {
        val result = ClipboardSmartDetector.detect("12345678")
        assertTrue(result is ClipboardSmartDetector.Detection.Otp)
    }

    @Test
    fun `spaced 4+4 digit code is detected as OTP`() {
        val result = ClipboardSmartDetector.detect("1234 5678")
        assertTrue(result is ClipboardSmartDetector.Detection.Otp)
    }

    @Test
    fun `3-digit number is NOT detected as OTP`() {
        val result = ClipboardSmartDetector.detect("123")
        assertTrue(result is ClipboardSmartDetector.Detection.None)
    }

    @Test
    fun `9-digit number is NOT detected as OTP`() {
        val result = ClipboardSmartDetector.detect("123456789")
        assertFalse(result is ClipboardSmartDetector.Detection.Otp)
    }

    // URL detection

    @Test
    fun `http URL is detected`() {
        val result = ClipboardSmartDetector.detect("http://example.com")
        assertTrue(result is ClipboardSmartDetector.Detection.Url)
    }

    @Test
    fun `https URL is detected`() {
        val result = ClipboardSmartDetector.detect("https://www.google.com/search?q=test")
        assertTrue(result is ClipboardSmartDetector.Detection.Url)
    }

    @Test
    fun `URL with path is detected`() {
        val result = ClipboardSmartDetector.detect("https://github.com/typlx/android-grammar-keyboard")
        assertTrue(result is ClipboardSmartDetector.Detection.Url)
    }

    @Test
    fun `plain domain without scheme is NOT detected as URL`() {
        val result = ClipboardSmartDetector.detect("example.com")
        assertFalse(result is ClipboardSmartDetector.Detection.Url)
    }

    // Email detection

    @Test
    fun `email address is detected`() {
        val result = ClipboardSmartDetector.detect("user@example.com")
        assertTrue(result is ClipboardSmartDetector.Detection.Email)
    }

    @Test
    fun `email with subdomain is detected`() {
        val result = ClipboardSmartDetector.detect("user@mail.example.org")
        assertTrue(result is ClipboardSmartDetector.Detection.Email)
    }

    @Test
    fun `string without @ is NOT detected as email`() {
        val result = ClipboardSmartDetector.detect("notanemail.com")
        assertFalse(result is ClipboardSmartDetector.Detection.Email)
    }

    // Phone number detection

    @Test
    fun `international phone number is detected`() {
        val result = ClipboardSmartDetector.detect("+1 800 555 1234")
        assertTrue(result is ClipboardSmartDetector.Detection.PhoneNumber)
    }

    @Test
    fun `US phone with dashes is detected`() {
        val result = ClipboardSmartDetector.detect("555-867-5309")
        assertTrue(result is ClipboardSmartDetector.Detection.PhoneNumber)
    }

    // None / edge cases

    @Test
    fun `empty string returns None`() {
        val result = ClipboardSmartDetector.detect("")
        assertTrue(result is ClipboardSmartDetector.Detection.None)
    }

    @Test
    fun `regular sentence returns None`() {
        val result = ClipboardSmartDetector.detect("Hello, this is a normal sentence.")
        assertTrue(result is ClipboardSmartDetector.Detection.None)
    }

    @Test
    fun `very long text returns None`() {
        val result = ClipboardSmartDetector.detect("a".repeat(300))
        assertTrue(result is ClipboardSmartDetector.Detection.None)
    }

    // Chip label formatting

    @Test
    fun `OTP chip label shows code`() {
        val detection = ClipboardSmartDetector.Detection.Otp("123456")
        assertEquals("Paste OTP: 123456", ClipboardSmartDetector.chipLabel(detection))
    }

    @Test
    fun `URL chip label truncates long URLs`() {
        val longUrl = "https://example.com/" + "a".repeat(50)
        val detection = ClipboardSmartDetector.Detection.Url(longUrl)
        val label = ClipboardSmartDetector.chipLabel(detection)
        assertTrue(label.startsWith("Paste URL: "))
        assertTrue(label.endsWith("…"))
    }

    @Test
    fun `short URL chip label does not add ellipsis`() {
        val detection = ClipboardSmartDetector.Detection.Url("https://example.com")
        val label = ClipboardSmartDetector.chipLabel(detection)
        assertFalse(label.endsWith("…"))
    }
}
