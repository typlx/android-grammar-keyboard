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
    fun `7-digit number is NOT detected as OTP — avoids phone misclassification`() {
        val result = ClipboardSmartDetector.detect("1234567")
        assertFalse("7-digit number should not be OTP", result is ClipboardSmartDetector.Detection.Otp)
    }

    @Test
    fun `8-digit number is NOT detected as OTP`() {
        val result = ClipboardSmartDetector.detect("12345678")
        assertFalse("8-digit number should not be OTP (narrowed to 4-6)", result is ClipboardSmartDetector.Detection.Otp)
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

    @Test
    fun `5-digit number is detected as OTP`() {
        val result = ClipboardSmartDetector.detect("12345")
        assertTrue(result is ClipboardSmartDetector.Detection.Otp)
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
    fun `6-digit OTP chip label masks trailing digits for shoulder-surf protection`() {
        val detection = ClipboardSmartDetector.Detection.Otp("123456")
        assertEquals("Paste OTP: 1234••", ClipboardSmartDetector.chipLabel(detection))
    }

    @Test
    fun `4-digit OTP chip label shows all digits when length equals visible count`() {
        val detection = ClipboardSmartDetector.Detection.Otp("9876")
        assertEquals("Paste OTP: 9876", ClipboardSmartDetector.chipLabel(detection))
    }

    @Test
    fun `5-digit OTP chip label masks one trailing digit`() {
        val detection = ClipboardSmartDetector.Detection.Otp("12345")
        assertEquals("Paste OTP: 1234•", ClipboardSmartDetector.chipLabel(detection))
    }

    @Test
    fun `phone number chip label masks trailing digits`() {
        val detection = ClipboardSmartDetector.Detection.PhoneNumber("+1 800 555 1234")
        val label = ClipboardSmartDetector.chipLabel(detection)
        assertTrue(label.startsWith("Paste number: +1 8"))
        assertTrue(label.contains("•"))
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
