package com.typlx.keyboard

import androidx.compose.ui.graphics.Color
import com.typlx.keyboard.ui.theme.ThemePreset
import com.typlx.keyboard.ui.theme.resolveIsDark
import com.typlx.keyboard.ui.theme.resolveKeyboardColors
import org.junit.Assert.*
import org.junit.Test

class KeyboardThemeTest {

    @Test
    fun `SYSTEM preset dark when system is dark`() {
        assertTrue(resolveIsDark(ThemePreset.SYSTEM, isDarkSystem = true))
    }

    @Test
    fun `SYSTEM preset light when system is light`() {
        assertFalse(resolveIsDark(ThemePreset.SYSTEM, isDarkSystem = false))
    }

    @Test
    fun `DARK preset always dark regardless of system`() {
        assertTrue(resolveIsDark(ThemePreset.DARK, isDarkSystem = false))
        assertTrue(resolveIsDark(ThemePreset.DARK, isDarkSystem = true))
    }

    @Test
    fun `LIGHT preset always light regardless of system`() {
        assertFalse(resolveIsDark(ThemePreset.LIGHT, isDarkSystem = true))
        assertFalse(resolveIsDark(ThemePreset.LIGHT, isDarkSystem = false))
    }

    @Test
    fun `AMOLED preset always dark`() {
        assertTrue(resolveIsDark(ThemePreset.AMOLED, isDarkSystem = false))
    }

    @Test
    fun `AMOLED keyboard background is pure black`() {
        val colors = resolveKeyboardColors(ThemePreset.AMOLED, isDarkSystem = true, cornerRadiusDp = 6, keyAlphaPercent = 100)
        assertEquals(Color.Black, colors.keyboardBg)
    }

    @Test
    fun `HIGH_CONTRAST preset always dark`() {
        assertTrue(resolveIsDark(ThemePreset.HIGH_CONTRAST, isDarkSystem = false))
        assertTrue(resolveIsDark(ThemePreset.HIGH_CONTRAST, isDarkSystem = true))
    }

    @Test
    fun `HIGH_CONTRAST keyboard background is pure black`() {
        val colors = resolveKeyboardColors(ThemePreset.HIGH_CONTRAST, isDarkSystem = false, cornerRadiusDp = 6, keyAlphaPercent = 100)
        assertEquals(Color(0xFF000000), colors.keyboardBg)
    }

    @Test
    fun `HIGH_CONTRAST key text is white`() {
        val colors = resolveKeyboardColors(ThemePreset.HIGH_CONTRAST, isDarkSystem = false, cornerRadiusDp = 6, keyAlphaPercent = 100)
        assertEquals(Color(0xFFFFFFFF), colors.keyText)
    }

    @Test
    fun `HIGH_CONTRAST key background is near-black`() {
        val colors = resolveKeyboardColors(ThemePreset.HIGH_CONTRAST, isDarkSystem = false, cornerRadiusDp = 6, keyAlphaPercent = 100)
        // Key bg is darker than standard dark theme
        val keyBgLuminance = colors.keyBg.red * 0.299f + colors.keyBg.green * 0.587f + colors.keyBg.blue * 0.114f
        assertTrue("HIGH_CONTRAST key background should be very dark", keyBgLuminance < 0.15f)
    }

    @Test
    fun `corner radius is preserved in resolved colors`() {
        val colors = resolveKeyboardColors(ThemePreset.DARK, isDarkSystem = true, cornerRadiusDp = 12, keyAlphaPercent = 100)
        assertEquals(12, colors.cornerRadiusDp)
    }

    @Test
    fun `full opacity produces alpha 1f on key background`() {
        val colors = resolveKeyboardColors(ThemePreset.LIGHT, isDarkSystem = false, cornerRadiusDp = 6, keyAlphaPercent = 100)
        assertEquals(1f, colors.keyBg.alpha, 0.01f)
    }

    @Test
    fun `50 percent opacity halves key background alpha`() {
        val colors = resolveKeyboardColors(ThemePreset.LIGHT, isDarkSystem = false, cornerRadiusDp = 6, keyAlphaPercent = 50)
        assertEquals(0.5f, colors.keyBg.alpha, 0.01f)
    }

    @Test
    fun `zero opacity produces fully transparent key background`() {
        val colors = resolveKeyboardColors(ThemePreset.DARK, isDarkSystem = true, cornerRadiusDp = 6, keyAlphaPercent = 0)
        assertEquals(0f, colors.keyBg.alpha, 0.01f)
    }

    @Test
    fun `ThemePreset enum round-trips via name`() {
        ThemePreset.entries.forEach { preset ->
            assertEquals(preset, ThemePreset.valueOf(preset.name))
        }
    }

    @Test
    fun `custom key background overrides resolved key background`() {
        val custom = Color(0xFF1565C0)
        val colors = resolveKeyboardColors(
            ThemePreset.LIGHT, isDarkSystem = false, cornerRadiusDp = 6, keyAlphaPercent = 100,
            customKeyBg = custom,
        )
        assertEquals(custom, colors.keyBg)
    }

    @Test
    fun `custom key text overrides resolved key text`() {
        val custom = Color(0xFFFFFFFF)
        val colors = resolveKeyboardColors(
            ThemePreset.LIGHT, isDarkSystem = false, cornerRadiusDp = 6, keyAlphaPercent = 100,
            customKeyText = custom,
        )
        assertEquals(custom, colors.keyText)
    }

    @Test
    fun `custom accent overrides resolved action key background`() {
        val custom = Color(0xFFC62828)
        val colors = resolveKeyboardColors(
            ThemePreset.DARK, isDarkSystem = true, cornerRadiusDp = 6, keyAlphaPercent = 100,
            customAccent = custom,
        )
        assertEquals(custom, colors.keyActionBg)
    }

    @Test
    fun `custom key background respects opacity`() {
        val custom = Color(0xFF1565C0)
        val colors = resolveKeyboardColors(
            ThemePreset.LIGHT, isDarkSystem = false, cornerRadiusDp = 6, keyAlphaPercent = 50,
            customKeyBg = custom,
        )
        assertEquals(0.5f, colors.keyBg.alpha, 0.01f)
    }

    @Test
    fun `null custom colors leave preset colors unchanged`() {
        val withCustom = resolveKeyboardColors(
            ThemePreset.DARK, isDarkSystem = true, cornerRadiusDp = 6, keyAlphaPercent = 100,
            customKeyBg = null, customKeyText = null, customAccent = null,
        )
        val withoutCustom = resolveKeyboardColors(
            ThemePreset.DARK, isDarkSystem = true, cornerRadiusDp = 6, keyAlphaPercent = 100,
        )
        assertEquals(withoutCustom, withCustom)
    }

    @Test
    fun `CUSTOM_COLOR_SCHEME feature is not free tier`() {
        assertFalse(FeatureGate.isFreeTier(FeatureGate.Feature.CUSTOM_COLOR_SCHEME))
    }

    @Test
    fun `built-in themes do not require premium`() {
        // All preset switching is free — only custom color scheme is gated
        assertTrue(FeatureGate.isFreeTier(FeatureGate.Feature.GRAMMAR_FIX))
        assertFalse(FeatureGate.isFreeTier(FeatureGate.Feature.CUSTOM_COLOR_SCHEME))
    }
}
