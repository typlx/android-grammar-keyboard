package com.typlx.keyboard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class KeyboardColors(
    val keyboardBg: Color,
    val keyBg: Color,
    val keyActionBg: Color,
    val keyText: Color,
)

val LocalKeyboardColors = staticCompositionLocalOf {
    KeyboardColors(
        keyboardBg = KeyboardBg,
        keyBg = KeyBg,
        keyActionBg = KeyActionBg,
        keyText = KeyTextColor,
    )
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
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val keyboardColors = if (darkTheme) {
        KeyboardColors(
            keyboardBg = KeyboardBgDark,
            keyBg = KeyBgDark,
            keyActionBg = KeyActionBgDark,
            keyText = KeyTextColorDark,
        )
    } else {
        KeyboardColors(
            keyboardBg = KeyboardBg,
            keyBg = KeyBg,
            keyActionBg = KeyActionBg,
            keyText = KeyTextColor,
        )
    }

    CompositionLocalProvider(LocalKeyboardColors provides keyboardColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            content = content,
        )
    }
}
