package com.typlx.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typlx.keyboard.ui.theme.LocalKeyboardColors

private val ROW1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
private val ROW2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
private val ROW3 = listOf("z", "x", "c", "v", "b", "n", "m")

private val SYM_ROW1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
private val SYM_ROW2 = listOf("@", "#", "$", "%", "&", "-", "+", "(", ")")
private val SYM_ROW3 = listOf("*", "\"", "'", ":", ";", "!", "?")

@Composable
fun KeyboardScreen(
    isFixingGrammar: Boolean,
    grammarError: String?,
    onKeyPress: (String) -> Unit,
    onDelete: () -> Unit,
    onFixGrammar: () -> Unit,
    onReturn: () -> Unit,
    onErrorDismiss: () -> Unit,
) {
    var isCaps by remember { mutableStateOf(false) }
    var isSymbols by remember { mutableStateOf(false) }
    val colors = LocalKeyboardColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.keyboardBg)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ToolbarRow(
            isFixingGrammar = isFixingGrammar,
            grammarError = grammarError,
            onFixGrammar = onFixGrammar,
            onErrorDismiss = onErrorDismiss,
        )

        if (isSymbols) {
            KeyRow(SYM_ROW1, isCaps = false, onKeyPress = onKeyPress, colors = colors)
            KeyRow(SYM_ROW2, isCaps = false, onKeyPress = onKeyPress, colors = colors)
            SymbolRow3(SYM_ROW3, onKeyPress = onKeyPress, onDelete = onDelete, colors = colors)
        } else {
            KeyRow(ROW1, isCaps = isCaps, onKeyPress = onKeyPress, colors = colors)
            KeyRow(ROW2, isCaps = isCaps, onKeyPress = onKeyPress, colors = colors)
            AlphaRow3(
                keys = ROW3,
                isCaps = isCaps,
                onCapsToggle = { isCaps = !isCaps },
                onKeyPress = { key ->
                    onKeyPress(key)
                    if (isCaps) isCaps = false  // single-tap shift: revert after one key
                },
                onDelete = onDelete,
                colors = colors,
            )
        }

        BottomRow(
            isSymbols = isSymbols,
            onSymbolToggle = { isSymbols = !isSymbols },
            onKeyPress = onKeyPress,
            onReturn = onReturn,
            colors = colors,
        )
    }
}

@Composable
private fun ToolbarRow(
    isFixingGrammar: Boolean,
    grammarError: String?,
    onFixGrammar: () -> Unit,
    onErrorDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (grammarError != null) {
            SuggestionChip(
                onClick = onErrorDismiss,
                label = {
                    Text(
                        text = grammarError,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                    )
                },
                modifier = Modifier.weight(1f),
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
            )
        } else {
            Spacer(Modifier.weight(1f))
        }

        Button(
            onClick = onFixGrammar,
            enabled = !isFixingGrammar,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
            modifier = Modifier.height(36.dp),
        ) {
            if (isFixingGrammar) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = if (isFixingGrammar) "Fixing…" else "Fix Grammar",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun KeyRow(
    keys: List<String>,
    isCaps: Boolean,
    onKeyPress: (String) -> Unit,
    colors: com.typlx.keyboard.ui.theme.KeyboardColors,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
    ) {
        keys.forEach { key ->
            val label = if (isCaps) key.uppercase() else key
            KeyButton(
                label = label,
                modifier = Modifier.weight(1f),
                bgColor = colors.keyBg,
                textColor = colors.keyText,
                onClick = { onKeyPress(label) },
            )
        }
    }
}

@Composable
private fun AlphaRow3(
    keys: List<String>,
    isCaps: Boolean,
    onCapsToggle: () -> Unit,
    onKeyPress: (String) -> Unit,
    onDelete: () -> Unit,
    colors: com.typlx.keyboard.ui.theme.KeyboardColors,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KeyButton(
            label = "⇧",
            modifier = Modifier.weight(1.5f),
            bgColor = if (isCaps) MaterialTheme.colorScheme.primary else colors.keyActionBg,
            textColor = if (isCaps) MaterialTheme.colorScheme.onPrimary else colors.keyText,
            onClick = onCapsToggle,
        )
        keys.forEach { key ->
            val label = if (isCaps) key.uppercase() else key
            KeyButton(
                label = label,
                modifier = Modifier.weight(1f),
                bgColor = colors.keyBg,
                textColor = colors.keyText,
                onClick = { onKeyPress(label) },
            )
        }
        KeyButton(
            label = "⌫",
            modifier = Modifier.weight(1.5f),
            bgColor = colors.keyActionBg,
            textColor = colors.keyText,
            onClick = onDelete,
        )
    }
}

@Composable
private fun SymbolRow3(
    keys: List<String>,
    onKeyPress: (String) -> Unit,
    onDelete: () -> Unit,
    colors: com.typlx.keyboard.ui.theme.KeyboardColors,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.weight(1.5f))
        keys.forEach { key ->
            KeyButton(
                label = key,
                modifier = Modifier.weight(1f),
                bgColor = colors.keyBg,
                textColor = colors.keyText,
                onClick = { onKeyPress(key) },
            )
        }
        KeyButton(
            label = "⌫",
            modifier = Modifier.weight(1.5f),
            bgColor = colors.keyActionBg,
            textColor = colors.keyText,
            onClick = onDelete,
        )
    }
}

@Composable
private fun BottomRow(
    isSymbols: Boolean,
    onSymbolToggle: () -> Unit,
    onKeyPress: (String) -> Unit,
    onReturn: () -> Unit,
    colors: com.typlx.keyboard.ui.theme.KeyboardColors,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KeyButton(
            label = if (isSymbols) "ABC" else "?123",
            modifier = Modifier.weight(1.5f),
            bgColor = colors.keyActionBg,
            textColor = colors.keyText,
            onClick = onSymbolToggle,
        )
        KeyButton(
            label = ",",
            modifier = Modifier.weight(1f),
            bgColor = colors.keyBg,
            textColor = colors.keyText,
            onClick = { onKeyPress(",") },
        )
        KeyButton(
            label = " ",
            modifier = Modifier.weight(4f),
            bgColor = colors.keyBg,
            textColor = colors.keyText,
            onClick = { onKeyPress(" ") },
        )
        KeyButton(
            label = ".",
            modifier = Modifier.weight(1f),
            bgColor = colors.keyBg,
            textColor = colors.keyText,
            onClick = { onKeyPress(".") },
        )
        KeyButton(
            label = "↵",
            modifier = Modifier.weight(1.5f),
            bgColor = MaterialTheme.colorScheme.primary,
            textColor = MaterialTheme.colorScheme.onPrimary,
            onClick = onReturn,
        )
    }
}

@Composable
private fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    bgColor: Color,
    textColor: Color,
    height: Dp = 46.dp,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = if (label.length == 1) 18.sp else 13.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
        )
    }
}
