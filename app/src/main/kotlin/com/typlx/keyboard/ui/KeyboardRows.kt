package com.typlx.keyboard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.typlx.keyboard.KEY_ALTERNATIVES
import com.typlx.keyboard.ui.theme.KeyboardColors

@Composable
internal fun NumberRow(
    keys: List<String>,
    onKeyPress: (String) -> Unit,
    colors: KeyboardColors,
    height: Dp = 38.dp,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
    ) {
        keys.forEach { key ->
            KeyButton(
                label = key,
                contentDescription = "Digit $key",
                modifier = Modifier.weight(1f),
                height = height,
                bgColor = colors.keyBg,
                textColor = colors.keyText,
                onClick = { onKeyPress(key) },
            )
        }
    }
}

@Composable
internal fun KeyRow(
    keys: List<String>,
    isCaps: Boolean,
    onKeyPress: (String) -> Unit,
    colors: KeyboardColors,
    onShowAlternatives: ((String, Boolean, List<String>) -> Unit)? = null,
    alternativesMap: Map<String, List<String>> = KEY_ALTERNATIVES,
    height: Dp = 46.dp,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
    ) {
        keys.forEach { key ->
            val label = if (isCaps) key.uppercase() else key
            val rawAlts = alternativesMap[key.lowercase()]
            val alts = if (rawAlts != null && onShowAlternatives != null) {
                if (isCaps) rawAlts.map { it.uppercase() } else rawAlts
            } else null
            KeyButton(
                label = label,
                contentDescription = "Letter ${label.uppercase()}",
                modifier = Modifier.weight(1f),
                height = height,
                bgColor = colors.keyBg,
                textColor = colors.keyText,
                onClick = { onKeyPress(label) },
                onLongPress = if (alts != null) {
                    { onShowAlternatives!!(label, isCaps, alts) }
                } else null,
            )
        }
    }
}

@Composable
internal fun AlphaRow3(
    keys: List<String>,
    shiftState: ShiftState,
    onShiftTap: () -> Unit,
    onKeyPress: (String) -> Unit,
    onDelete: () -> Unit,
    onDeleteWord: () -> Unit,
    colors: KeyboardColors,
    onShowAlternatives: ((String, Boolean, List<String>) -> Unit)? = null,
    alternativesMap: Map<String, List<String>> = KEY_ALTERNATIVES,
    height: Dp = 46.dp,
) {
    val isCaps = shiftState != ShiftState.OFF
    val shiftLabel = if (shiftState == ShiftState.CAPS_LOCK) "⇪" else "⇧"
    val shiftDesc = when (shiftState) {
        ShiftState.OFF -> "Shift, double-tap for caps lock"
        ShiftState.SHIFT_ONCE -> "Shift active, double-tap for caps lock, tap again to cancel"
        ShiftState.CAPS_LOCK -> "Caps lock active, tap to disable"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KeyButton(
            label = shiftLabel,
            contentDescription = shiftDesc,
            modifier = Modifier.weight(1.5f),
            height = height,
            bgColor = if (isCaps) MaterialTheme.colorScheme.primary else colors.keyActionBg,
            textColor = if (isCaps) MaterialTheme.colorScheme.onPrimary else colors.keyText,
            onClick = onShiftTap,
        )
        keys.forEach { key ->
            val label = if (isCaps) key.uppercase() else key
            val rawAlts = alternativesMap[key.lowercase()]
            val alts = if (rawAlts != null && onShowAlternatives != null) {
                if (isCaps) rawAlts.map { it.uppercase() } else rawAlts
            } else null
            KeyButton(
                label = label,
                contentDescription = "Letter ${label.uppercase()}",
                modifier = Modifier.weight(1f),
                height = height,
                bgColor = colors.keyBg,
                textColor = colors.keyText,
                onClick = { onKeyPress(label) },
                onLongPress = if (alts != null) {
                    { onShowAlternatives!!(label, isCaps, alts) }
                } else null,
            )
        }
        DeleteButton(
            modifier = Modifier.weight(1.5f),
            colors = colors,
            height = height,
            onDelete = onDelete,
            onDeleteWord = onDeleteWord,
        )
    }
}

@Composable
internal fun SymbolRow3(
    keys: List<String>,
    onKeyPress: (String) -> Unit,
    onDelete: () -> Unit,
    onDeleteWord: () -> Unit,
    colors: KeyboardColors,
    height: Dp = 46.dp,
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
                contentDescription = key,
                modifier = Modifier.weight(1f),
                height = height,
                bgColor = colors.keyBg,
                textColor = colors.keyText,
                onClick = { onKeyPress(key) },
            )
        }
        DeleteButton(
            modifier = Modifier.weight(1.5f),
            colors = colors,
            height = height,
            onDelete = onDelete,
            onDeleteWord = onDeleteWord,
        )
    }
}

@Composable
internal fun BottomRow(
    isSymbols: Boolean,
    onSymbolToggle: () -> Unit,
    onKeyPress: (String) -> Unit,
    onSpacePress: () -> Unit,
    onReturn: () -> Unit,
    returnKeyDescription: String,
    colors: KeyboardColors,
    height: Dp = 46.dp,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val symbolToggleDesc = if (isSymbols) "Switch to letters" else "Switch to symbols"
        KeyButton(
            label = if (isSymbols) "ABC" else "?123",
            contentDescription = symbolToggleDesc,
            modifier = Modifier.weight(1.5f),
            height = height,
            bgColor = colors.keyActionBg,
            textColor = colors.keyText,
            onClick = onSymbolToggle,
        )
        KeyButton(
            label = ",",
            contentDescription = "Comma",
            modifier = Modifier.weight(1f),
            height = height,
            bgColor = colors.keyBg,
            textColor = colors.keyText,
            onClick = { onKeyPress(",") },
        )
        KeyButton(
            label = " ",
            contentDescription = "Space",
            modifier = Modifier.weight(4f),
            height = height,
            bgColor = colors.keyBg,
            textColor = colors.keyText,
            onClick = onSpacePress,
        )
        KeyButton(
            label = ".",
            contentDescription = "Period",
            modifier = Modifier.weight(1f),
            height = height,
            bgColor = colors.keyBg,
            textColor = colors.keyText,
            onClick = { onKeyPress(".") },
        )
        KeyButton(
            label = "↵",
            contentDescription = returnKeyDescription,
            modifier = Modifier.weight(1.5f),
            height = height,
            bgColor = MaterialTheme.colorScheme.primary,
            textColor = MaterialTheme.colorScheme.onPrimary,
            onClick = onReturn,
        )
    }
}
