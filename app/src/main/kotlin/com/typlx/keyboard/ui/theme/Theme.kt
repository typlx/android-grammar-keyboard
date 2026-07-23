package com.typlx.keyboard.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ThemePreset { SYSTEM, DARK, LIGHT, AMOLED, HIGH_CONTRAST }

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

fun resolveIsDark(preset: ThemePreset, isDarkSystem: Boolean): Boolean = when (preset) {
    ThemePreset.SYSTEM -> isDarkSystem
    ThemePreset.DARK, ThemePreset.AMOLED, ThemePreset.HIGH_CONTRAST -> true
    ThemePreset.LIGHT -> false
}

fun resolveKeyboardColors(
    preset: ThemePreset,
    isDarkSystem: Boolean,
    cornerRadiusDp: Int,
    keyAlphaPercent: Int,
    customKeyBg: Color? = null,
    customKeyText: Color? = null,
    customAccent: Color? = null,
): KeyboardColors {
    val dark = resolveIsDark(preset, isDarkSystem)
    val alpha = (keyAlphaPercent / 100f).coerceIn(0f, 1f)
    val base = when {
        preset == ThemePreset.HIGH_CONTRAST -> KeyboardColors(
            keyboardBg = KeyboardBgHighContrast,
            keyBg = KeyBgHighContrast.copy(alpha = alpha),
            keyActionBg = KeyActionBgHighContrast.copy(alpha = alpha),
            keyText = KeyTextColorHighContrast,
            cornerRadiusDp = cornerRadiusDp,
        )
        dark -> {
            val isAmoled = preset == ThemePreset.AMOLED
            KeyboardColors(
                keyboardBg = if (isAmoled) Color.Black else KeyboardBgDark,
                keyBg = (if (isAmoled) Color(0xFF111111) else KeyBgDark).copy(alpha = alpha),
                keyActionBg = (if (isAmoled) Color(0xFF0A0A0A) else KeyActionBgDark).copy(alpha = alpha),
                keyText = KeyTextColorDark,
                cornerRadiusDp = cornerRadiusDp,
            )
        }
        else -> KeyboardColors(
            keyboardBg = KeyboardBg,
            keyBg = KeyBg.copy(alpha = alpha),
            keyActionBg = KeyActionBg.copy(alpha = alpha),
            keyText = KeyTextColor,
            cornerRadiusDp = cornerRadiusDp,
        )
    }
    if (customKeyBg == null && customKeyText == null && customAccent == null) return base
    return base.copy(
        keyBg = customKeyBg?.copy(alpha = alpha) ?: base.keyBg,
        keyText = customKeyText ?: base.keyText,
        keyActionBg = customAccent?.copy(alpha = alpha) ?: base.keyActionBg,
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
    preset: ThemePreset = ThemePreset.SYSTEM,
    cornerRadiusDp: Int = 6,
    keyAlphaPercent: Int = 100,
    customKeyBg: Color? = null,
    customKeyText: Color? = null,
    customAccent: Color? = null,
    content: @Composable () -> Unit,
) {
    val isDarkSystem = isSystemInDarkTheme()
    val isDark = resolveIsDark(preset, isDarkSystem)
    val keyboardColors = resolveKeyboardColors(
        preset, isDarkSystem, cornerRadiusDp, keyAlphaPercent, customKeyBg, customKeyText, customAccent,
    )

    CompositionLocalProvider(LocalKeyboardColors provides keyboardColors) {
        MaterialTheme(
            colorScheme = if (isDark) DarkColorScheme else LightColorScheme,
            content = content,
        )
    }
}

/** Mini keyboard preview card shown in Settings before the user applies a theme. */
@Composable
fun ThemePreviewCard(
    preset: ThemePreset,
    cornerRadiusDp: Int = 6,
    customKeyBg: Color? = null,
    customKeyText: Color? = null,
    customAccent: Color? = null,
    modifier: Modifier = Modifier,
) {
    val isDarkSystem = isSystemInDarkTheme()
    val colors = resolveKeyboardColors(
        preset, isDarkSystem, cornerRadiusDp, keyAlphaPercent = 100,
        customKeyBg, customKeyText, customAccent,
    )
    val radius: Dp = cornerRadiusDp.dp.coerceAtLeast(2.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.keyboardBg),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PreviewKeyRow(
                keys = listOf("Q", "W", "E", "R", "T", "Y"),
                keyBg = colors.keyBg,
                keyText = colors.keyText,
                radius = radius,
            )
            PreviewKeyRow(
                keys = listOf("A", "S", "D", "F", "G"),
                keyBg = colors.keyBg,
                keyText = colors.keyText,
                radius = radius,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(radius))
                        .background(colors.keyActionBg),
                )
                PreviewKeyRow(
                    keys = listOf("Z", "X", "C"),
                    keyBg = colors.keyBg,
                    keyText = colors.keyText,
                    radius = radius,
                )
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(radius))
                        .background(colors.keyActionBg),
                )
            }
        }
    }
}

@Composable
private fun PreviewKeyRow(keys: List<String>, keyBg: Color, keyText: Color, radius: Dp) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        keys.forEach { label ->
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(radius))
                    .background(keyBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = keyText,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 7.sp,
                )
            }
        }
    }
}
