package com.typlx.keyboard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemePreset { SYSTEM, DARK, LIGHT, AMOLED }

data class KeyboardColors(
    val keyboardBg: Color,
    val keyBg: Color,
    val keyActionBg: Color,
    val keyText: Color,
    val cornerRadiusDp: Int = 6,
)

val LocalKeyboardColors = staticCompositionLocalOf {
    KeyboardColors(
        keyboardBg = KeyboardBg,
        keyBg = KeyBg,
        keyActionBg = KeyActionBg,
        keyText = KeyTextColor,
    )
}

internal fun resolveIsDark(preset: ThemePreset, isDarkSystem: Boolean): Boolean = when (preset) {
    ThemePreset.SYSTEM -> isDarkSystem
    ThemePreset.DARK, ThemePreset.AMOLED -> true
    ThemePreset.LIGHT -> false
}

internal fun resolveKeyboardColors(
    preset: ThemePreset,
    isDarkSystem: Boolean,
    cornerRadiusDp: Int,
    keyAlphaPercent: Int,
): KeyboardColors {
    val dark = resolveIsDark(preset, isDarkSystem)
    val alpha = (keyAlphaPercent / 100f).coerceIn(0f, 1f)
    return if (dark) {
        val isAmoled = preset == ThemePreset.AMOLED
        KeyboardColors(
            keyboardBg = if (isAmoled) Color.Black else KeyboardBgDark,
            keyBg = (if (isAmoled) Color(0xFF111111) else KeyBgDark).copy(alpha = alpha),
            keyActionBg = (if (isAmoled) Color(0xFF0A0A0A) else KeyActionBgDark).copy(alpha = alpha),
            keyText = KeyTextColorDark,
            cornerRadiusDp = cornerRadiusDp,
        )
    } else {
        KeyboardColors(
            keyboardBg = KeyboardBg,
            keyBg = KeyBg.copy(alpha = alpha),
            keyActionBg = KeyActionBg.copy(alpha = alpha),
            keyText = KeyTextColor,
            cornerRadiusDp = cornerRadiusDp,
        )
    }
}

private val LightColorScheme = lightColorScheme(
    primary = TyplxBlue,
    primaryContainer = TyplxBlueContainer,
    onPrimaryContainer = TyplxOnBlueContainer,
    secondary = TyplxSecondary,
    secondaryContainer = TyplxSecondaryContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary = TyplxBlueContainer,
    primaryContainer = TyplxBlue,
    onPrimaryContainer = TyplxBlueContainer,
)

@Composable
fun TyplxKeyboardTheme(
    preset: ThemePreset = ThemePreset.SYSTEM,
    cornerRadiusDp: Int = 6,
    keyAlphaPercent: Int = 100,
    content: @Composable () -> Unit,
) {
    val isDarkSystem = isSystemInDarkTheme()
    val isDark = resolveIsDark(preset, isDarkSystem)
    val keyboardColors = resolveKeyboardColors(preset, isDarkSystem, cornerRadiusDp, keyAlphaPercent)

    CompositionLocalProvider(LocalKeyboardColors provides keyboardColors) {
        MaterialTheme(
            colorScheme = if (isDark) DarkColorScheme else LightColorScheme,
            content = content,
        )
    }
}
