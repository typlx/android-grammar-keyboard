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
}
