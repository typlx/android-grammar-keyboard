package com.typlx.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.typlx.keyboard.SuggestionState
import com.typlx.keyboard.getAlternatives
import com.typlx.keyboard.ui.theme.LocalKeyboardColors

private val NUM_ROW = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
private val ROW1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
private val ROW2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
private val ROW3 = listOf("z", "x", "c", "v", "b", "n", "m")

private val SYM_ROW1 = listOf("@", "#", "$", "%", "&", "-", "+", "(", ")")
private val SYM_ROW2 = listOf("*", "\"", "'", ":", ";", "!", "?", "~", "/", "\\")
private val SYM_ROW3 = listOf("<", ">", "[", "]", "{", "}", "=")

internal enum class ShiftState { OFF, SHIFT_ONCE, CAPS_LOCK }

/**
 * Pure state machine for shift key transitions. Returns the next (state, lastTapMs) pair.
 * [now] and [lastTapMs] are epoch-millisecond timestamps; double-tap window is 400 ms.
 */
internal fun nextShiftState(
    current: ShiftState,
    now: Long,
    lastTapMs: Long,
): Pair<ShiftState, Long> = when (current) {
    ShiftState.OFF -> Pair(ShiftState.SHIFT_ONCE, now)
    ShiftState.SHIFT_ONCE -> if (now - lastTapMs < 400L) {
        Pair(ShiftState.CAPS_LOCK, lastTapMs)
    } else {
        Pair(ShiftState.SHIFT_ONCE, now)
    }
    ShiftState.CAPS_LOCK -> Pair(ShiftState.OFF, lastTapMs)
}

@Composable
fun KeyboardScreen(
    isFixingGrammar: Boolean,
    grammarError: String?,
    canUndo: Boolean = false,
    returnKeyDescription: String = "Return",
    emojiRecents: List<String> = emptyList(),
    suggestionState: SuggestionState = SuggestionState.Idle,
    autoShiftSignal: Long = 0L,
    onKeyPress: (String) -> Unit,
    onSpacePress: () -> Unit = {},
    onDelete: () -> Unit,
    onDeleteWord: () -> Unit,
    onFixGrammar: () -> Unit,
    onReturn: () -> Unit,
    onErrorDismiss: () -> Unit,
    onUndoGrammarFix: () -> Unit = {},
    onEmojiPress: (String) -> Unit = {},
    onAcceptSuggestion: () -> Unit = {},
    onDismissSuggestion: () -> Unit = {},
    onMoveCursorLeft: () -> Unit = {},
    onMoveCursorRight: () -> Unit = {},
    onMoveCursorUp: () -> Unit = {},
    onMoveCursorDown: () -> Unit = {},
    onMoveCursorWordLeft: () -> Unit = {},
    onMoveCursorWordRight: () -> Unit = {},
    onCursorHome: () -> Unit = {},
    onCursorEnd: () -> Unit = {},
    onSelectAll: () -> Unit = {},
    onCopyText: () -> Unit = {},
    onCutText: () -> Unit = {},
    onPasteText: () -> Unit = {},
    onOpenSettings: () -> Unit,
) {
    var shiftState by remember { mutableStateOf(ShiftState.OFF) }
    var lastShiftTapMs by remember { mutableLongStateOf(0L) }
    var isSymbols by remember { mutableStateOf(false) }
    var isEmoji by remember { mutableStateOf(false) }
    var isNav by remember { mutableStateOf(false) }
    // Triple: (displayLabel, isCaps, alternatives) — non-null when the alternatives bar is visible.
    var activeAlternatives by remember { mutableStateOf<Triple<String, Boolean, List<String>>?>(null) }
    val colors = LocalKeyboardColors.current

    val isCaps = shiftState != ShiftState.OFF

    // When the service signals an auto-shift (field entry caps, sentence end), activate SHIFT_ONCE.
    LaunchedEffect(autoShiftSignal) {
        if (autoShiftSignal > 0L && shiftState == ShiftState.OFF) {
            shiftState = ShiftState.SHIFT_ONCE
        }
    }

    val onShiftTap = {
        val (newState, newMs) = nextShiftState(shiftState, System.currentTimeMillis(), lastShiftTapMs)
        shiftState = newState
        lastShiftTapMs = newMs
    }

    // Auto-releases shift-once after any printable key; caps lock persists.
    // Also clears the alternatives bar on every key press.
    val shiftOnceKeyPress: (String) -> Unit = { key ->
        activeAlternatives = null
        onKeyPress(key)
        if (!isSymbols && shiftState == ShiftState.SHIFT_ONCE) {
            shiftState = ShiftState.OFF
        }
    }

    val showAlternatives: (String, Boolean, List<String>) -> Unit = { label, caps, alts ->
        activeAlternatives = Triple(label, caps, alts)
    }

    if (isNav) {
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
                canUndo = canUndo,
                isEmoji = false,
                isNav = true,
                onFixGrammar = onFixGrammar,
                onErrorDismiss = onErrorDismiss,
                onUndoGrammarFix = onUndoGrammarFix,
                onEmojiToggle = { isEmoji = true },
                onNavToggle = { isNav = false },
                onOpenSettings = onOpenSettings,
            )
            CursorNavPanel(
                onLeft = onMoveCursorLeft,
                onRight = onMoveCursorRight,
                onUp = onMoveCursorUp,
                onDown = onMoveCursorDown,
                onWordLeft = onMoveCursorWordLeft,
                onWordRight = onMoveCursorWordRight,
                onHome = onCursorHome,
                onEnd = onCursorEnd,
                onSelectAll = onSelectAll,
                onCopy = onCopyText,
                onCut = onCutText,
                onPaste = onPasteText,
                colors = colors,
            )
        }
        return
    }

    if (isEmoji) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.keyboardBg)
                .padding(horizontal = 4.dp, vertical = 6.dp),
        ) {
            ToolbarRow(
                isFixingGrammar = isFixingGrammar,
                grammarError = grammarError,
                canUndo = canUndo,
                isEmoji = true,
                isNav = false,
                onFixGrammar = onFixGrammar,
                onErrorDismiss = onErrorDismiss,
                onUndoGrammarFix = onUndoGrammarFix,
                onEmojiToggle = { isEmoji = false },
                onNavToggle = { isNav = true },
                onOpenSettings = onOpenSettings,
            )
            EmojiKeyboard(
                recents = emojiRecents,
                onEmojiClick = onEmojiPress,
                onDelete = onDelete,
                onBack = { isEmoji = false },
                colors = colors,
            )
        }
        return
    }

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
            canUndo = canUndo,
            isEmoji = false,
            isNav = false,
            onFixGrammar = onFixGrammar,
            onErrorDismiss = onErrorDismiss,
            onUndoGrammarFix = onUndoGrammarFix,
            onEmojiToggle = { isEmoji = true },
            onNavToggle = { isNav = true },
            onOpenSettings = onOpenSettings,
        )

        SuggestionStrip(
            state = suggestionState,
            onAccept = onAcceptSuggestion,
            onDismiss = onDismissSuggestion,
        )

        activeAlternatives?.let { (label, _, alts) ->
            AlternativesBar(
                originalKey = label,
                alternatives = alts,
                colors = colors,
                onSelectAlternative = { char -> shiftOnceKeyPress(char) },
                onDismiss = { activeAlternatives = null },
            )
        }

        NumberRow(keys = NUM_ROW, onKeyPress = onKeyPress, colors = colors)

        if (isSymbols) {
            KeyRow(SYM_ROW1, isCaps = false, onKeyPress = shiftOnceKeyPress, colors = colors)
            KeyRow(SYM_ROW2, isCaps = false, onKeyPress = shiftOnceKeyPress, colors = colors)
            SymbolRow3(SYM_ROW3, onKeyPress = shiftOnceKeyPress, onDelete = onDelete, onDeleteWord = onDeleteWord, colors = colors)
        } else {
            KeyRow(ROW1, isCaps = isCaps, onKeyPress = shiftOnceKeyPress, colors = colors, onShowAlternatives = showAlternatives)
            KeyRow(ROW2, isCaps = isCaps, onKeyPress = shiftOnceKeyPress, colors = colors, onShowAlternatives = showAlternatives)
            AlphaRow3(
                keys = ROW3,
                shiftState = shiftState,
                onShiftTap = onShiftTap,
                onKeyPress = shiftOnceKeyPress,
                onDelete = onDelete,
                onDeleteWord = onDeleteWord,
                onShowAlternatives = showAlternatives,
                colors = colors,
            )
        }

        BottomRow(
            isSymbols = isSymbols,
            onSymbolToggle = { isSymbols = !isSymbols },
            onKeyPress = shiftOnceKeyPress,
            onSpacePress = onSpacePress,
            onReturn = onReturn,
            returnKeyDescription = returnKeyDescription,
            colors = colors,
        )
    }
}

@Composable
private fun ToolbarRow(
    isFixingGrammar: Boolean,
    grammarError: String?,
    canUndo: Boolean,
    isEmoji: Boolean,
    isNav: Boolean,
    onFixGrammar: () -> Unit,
    onErrorDismiss: () -> Unit,
    onUndoGrammarFix: () -> Unit,
    onEmojiToggle: () -> Unit,
    onNavToggle: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when {
            grammarError != null -> SuggestionChip(
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
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Error: $grammarError. Tap to dismiss." },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
            )
            canUndo -> SuggestionChip(
                onClick = onUndoGrammarFix,
                label = {
                    Text(
                        text = "↩ Undo fix",
                        maxLines = 1,
                        fontSize = 12.sp,
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Undo grammar fix. Tap to restore original text." },
            )
            else -> Spacer(Modifier.weight(1f))
        }

        val fixButtonDesc = if (isFixingGrammar) "Fixing grammar, please wait" else "Fix grammar"
        Button(
            onClick = onFixGrammar,
            enabled = !isFixingGrammar,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
            modifier = Modifier
                .height(36.dp)
                .semantics { contentDescription = fixButtonDesc },
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

        val emojiDesc = if (isEmoji) "Switch to keyboard" else "Open emoji keyboard"
        IconButton(
            onClick = onEmojiToggle,
            modifier = Modifier
                .size(36.dp)
                .semantics { contentDescription = emojiDesc },
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEmotions,
                contentDescription = null,
                tint = if (isEmoji) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }

        val navDesc = if (isNav) "Switch to keyboard" else "Open cursor navigation"
        IconButton(
            onClick = onNavToggle,
            modifier = Modifier
                .size(36.dp)
                .semantics { contentDescription = navDesc },
        ) {
            Text(
                text = "↕",
                fontSize = 16.sp,
                color = if (isNav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .size(36.dp)
                .semantics { contentDescription = "Open settings" },
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null, // described by parent semantics
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun NumberRow(
    keys: List<String>,
    onKeyPress: (String) -> Unit,
    colors: com.typlx.keyboard.ui.theme.KeyboardColors,
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
                height = 38.dp,
                bgColor = colors.keyBg,
                textColor = colors.keyText,
                onClick = { onKeyPress(key) },
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
    onShowAlternatives: ((String, Boolean, List<String>) -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
    ) {
        keys.forEach { key ->
            val label = if (isCaps) key.uppercase() else key
            val alts = onShowAlternatives?.let { getAlternatives(key, isCaps) }
            KeyButton(
                label = label,
                contentDescription = "Letter ${label.uppercase()}",
                modifier = Modifier.weight(1f),
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
private fun AlphaRow3(
    keys: List<String>,
    shiftState: ShiftState,
    onShiftTap: () -> Unit,
    onKeyPress: (String) -> Unit,
    onDelete: () -> Unit,
    onDeleteWord: () -> Unit,
    colors: com.typlx.keyboard.ui.theme.KeyboardColors,
    onShowAlternatives: ((String, Boolean, List<String>) -> Unit)? = null,
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
            bgColor = if (isCaps) MaterialTheme.colorScheme.primary else colors.keyActionBg,
            textColor = if (isCaps) MaterialTheme.colorScheme.onPrimary else colors.keyText,
            onClick = onShiftTap,
        )
        keys.forEach { key ->
            val label = if (isCaps) key.uppercase() else key
            val alts = onShowAlternatives?.let { getAlternatives(key, isCaps) }
            KeyButton(
                label = label,
                contentDescription = "Letter ${label.uppercase()}",
                modifier = Modifier.weight(1f),
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
            onDelete = onDelete,
            onDeleteWord = onDeleteWord,
        )
    }
}

@Composable
private fun SymbolRow3(
    keys: List<String>,
    onKeyPress: (String) -> Unit,
    onDelete: () -> Unit,
    onDeleteWord: () -> Unit,
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
                contentDescription = key,
                modifier = Modifier.weight(1f),
                bgColor = colors.keyBg,
                textColor = colors.keyText,
                onClick = { onKeyPress(key) },
            )
        }
        DeleteButton(
            modifier = Modifier.weight(1.5f),
            colors = colors,
            onDelete = onDelete,
            onDeleteWord = onDeleteWord,
        )
    }
}

@Composable
private fun BottomRow(
    isSymbols: Boolean,
    onSymbolToggle: () -> Unit,
    onKeyPress: (String) -> Unit,
    onSpacePress: () -> Unit,
    onReturn: () -> Unit,
    returnKeyDescription: String,
    colors: com.typlx.keyboard.ui.theme.KeyboardColors,
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
            bgColor = colors.keyActionBg,
            textColor = colors.keyText,
            onClick = onSymbolToggle,
        )
        KeyButton(
            label = ",",
            contentDescription = "Comma",
            modifier = Modifier.weight(1f),
            bgColor = colors.keyBg,
            textColor = colors.keyText,
            onClick = { onKeyPress(",") },
        )
        KeyButton(
            label = " ",
            contentDescription = "Space",
            modifier = Modifier.weight(4f),
            bgColor = colors.keyBg,
            textColor = colors.keyText,
            onClick = onSpacePress,
        )
        KeyButton(
            label = ".",
            contentDescription = "Period",
            modifier = Modifier.weight(1f),
            bgColor = colors.keyBg,
            textColor = colors.keyText,
            onClick = { onKeyPress(".") },
        )
        KeyButton(
            label = "↵",
            contentDescription = returnKeyDescription,
            modifier = Modifier.weight(1.5f),
            bgColor = MaterialTheme.colorScheme.primary,
            textColor = MaterialTheme.colorScheme.onPrimary,
            onClick = onReturn,
        )
    }
}

/**
 * Backspace key with long-press repeat behavior:
 * - Tap: delete one character
 * - Hold 400ms: repeat character delete every 50ms
 * - Hold 1500ms: switch to word delete every 400ms with extra haptic
 */
@Composable
private fun DeleteButton(
    modifier: Modifier = Modifier,
    colors: com.typlx.keyboard.ui.theme.KeyboardColors,
    height: Dp = 46.dp,
    onDelete: () -> Unit,
    onDeleteWord: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(6.dp))
            .background(colors.keyActionBg)
            .semantics {
                contentDescription = "Delete, hold to delete word"
                role = Role.Button
            }
            .pointerInput(onDelete, onDeleteWord) {
                val pointerScope = this
                awaitPointerEventScope {
                    while (true) {
                        awaitFirstDown(requireUnconsumed = false)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        var repeatFired = false
                        val job = pointerScope.launch {
                            delay(400L)
                            repeatFired = true
                            var elapsed = 400L
                            var wordMode = false
                            while (true) {
                                if (elapsed >= 1500L) {
                                    if (!wordMode) {
                                        wordMode = true
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    onDeleteWord()
                                    delay(400L)
                                    elapsed += 400L
                                } else {
                                    onDelete()
                                    delay(50L)
                                    elapsed += 50L
                                }
                            }
                        }
                        do {
                            val event = awaitPointerEvent()
                        } while (event.changes.any { it.pressed })
                        job.cancel()
                        if (!repeatFired) {
                            onDelete()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "⌫",
            color = colors.keyText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
        )
    }
}

@Composable
private fun CursorNavPanel(
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onWordLeft: () -> Unit,
    onWordRight: () -> Unit,
    onHome: () -> Unit,
    onEnd: () -> Unit,
    onSelectAll: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onPaste: () -> Unit,
    colors: com.typlx.keyboard.ui.theme.KeyboardColors,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Arrow row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            NavKeyButton(
                label = "⇤",
                contentDescription = "Move to start of line",
                modifier = Modifier.weight(1f),
                colors = colors,
                onClick = onHome,
            )
            NavKeyButton(
                icon = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Move cursor left, long-press for word",
                modifier = Modifier.weight(1f),
                colors = colors,
                onClick = onLeft,
                onLongClick = onWordLeft,
            )
            NavKeyButton(
                icon = Icons.Default.KeyboardArrowUp,
                contentDescription = "Move cursor up",
                modifier = Modifier.weight(1f),
                colors = colors,
                onClick = onUp,
            )
            NavKeyButton(
                icon = Icons.Default.KeyboardArrowDown,
                contentDescription = "Move cursor down",
                modifier = Modifier.weight(1f),
                colors = colors,
                onClick = onDown,
            )
            NavKeyButton(
                icon = Icons.Default.KeyboardArrowRight,
                contentDescription = "Move cursor right, long-press for word",
                modifier = Modifier.weight(1f),
                colors = colors,
                onClick = onRight,
                onLongClick = onWordRight,
            )
            NavKeyButton(
                label = "⇥",
                contentDescription = "Move to end of line",
                modifier = Modifier.weight(1f),
                colors = colors,
                onClick = onEnd,
            )
        }

        // Edit action row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            NavKeyButton(
                label = "Select\nAll",
                contentDescription = "Select all text",
                modifier = Modifier.weight(1f),
                colors = colors,
                onClick = onSelectAll,
            )
            NavKeyButton(
                label = "Cut",
                contentDescription = "Cut selected text",
                modifier = Modifier.weight(1f),
                colors = colors,
                onClick = onCut,
            )
            NavKeyButton(
                label = "Copy",
                contentDescription = "Copy selected text",
                modifier = Modifier.weight(1f),
                colors = colors,
                onClick = onCopy,
            )
            NavKeyButton(
                label = "Paste",
                contentDescription = "Paste from clipboard",
                modifier = Modifier.weight(1f),
                colors = colors,
                onClick = onPaste,
            )
        }
    }
}

@Composable
private fun NavKeyButton(
    modifier: Modifier = Modifier,
    label: String? = null,
    icon: ImageVector? = null,
    contentDescription: String,
    colors: com.typlx.keyboard.ui.theme.KeyboardColors,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(colors.keyActionBg)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            }
            .then(
                if (onLongClick != null) {
                    Modifier.pointerInput(onClick, onLongClick) {
                        val scope = this
                        awaitPointerEventScope {
                            while (true) {
                                awaitFirstDown(requireUnconsumed = false)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                var longPressed = false
                                val job = scope.launch {
                                    delay(500L)
                                    longPressed = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onLongClick()
                                }
                                do {
                                    val event = awaitPointerEvent()
                                } while (event.changes.any { it.pressed })
                                job.cancel()
                                if (!longPressed) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onClick()
                                }
                            }
                        }
                    }
                } else {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onClick()
                        },
                    )
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.keyText,
                modifier = Modifier.size(22.dp),
            )
        } else if (label != null) {
            Text(
                text = label,
                color = colors.keyText,
                fontSize = if (label.length <= 2) 18.sp else 11.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 2,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SuggestionStrip(
    state: SuggestionState,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (state) {
        SuggestionState.Idle -> return
        SuggestionState.Loading -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 1.5.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Checking grammar…",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        is SuggestionState.Available -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SuggestionChip(
                onClick = onAccept,
                label = {
                    Text(
                        text = state.corrected,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Grammar suggestion: ${state.corrected}. Tap to accept." },
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(28.dp)
                    .semantics { contentDescription = "Dismiss grammar suggestion" },
            ) {
                Text(
                    text = "✕",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Horizontal bar showing accent/variant alternatives for a long-pressed key.
 * Horizontally scrollable so it accommodates keys with many alternatives (e.g. 'a' has 7).
 */
@Composable
private fun AlternativesBar(
    originalKey: String,
    alternatives: List<String>,
    colors: com.typlx.keyboard.ui.theme.KeyboardColors,
    onSelectAlternative: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.keyActionBg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Original key (highlighted to show source)
        Box(
            modifier = Modifier
                .width(42.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable { onSelectAlternative(originalKey) }
                .semantics { contentDescription = "$originalKey (original)" },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = originalKey,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        Spacer(Modifier.width(4.dp))
        // Scrollable alternatives
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            alternatives.forEach { alt ->
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.keyBg)
                        .clickable { onSelectAlternative(alt) }
                        .semantics { contentDescription = alt },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = alt,
                        color = colors.keyText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }
        }
        Spacer(Modifier.width(4.dp))
        // Dismiss button
        Box(
            modifier = Modifier
                .width(38.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(colors.keyBg)
                .clickable { onDismiss() }
                .semantics { contentDescription = "Dismiss alternatives" },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "✕",
                color = colors.keyText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun KeyButton(
    label: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    bgColor: Color,
    textColor: Color,
    height: Dp = 46.dp,
    onClick: () -> Unit,
    onLongPress: (() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            }
            .then(
                if (onLongPress != null) {
                    // Use pointerInput to detect both tap and 400ms long-press.
                    Modifier.pointerInput(onClick, onLongPress) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitFirstDown(requireUnconsumed = false)
                                var longFired = false
                                val job = launch {
                                    delay(400L)
                                    longFired = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onLongPress()
                                }
                                do {
                                    val event = awaitPointerEvent()
                                } while (event.changes.any { it.pressed })
                                job.cancel()
                                if (!longFired) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onClick()
                                }
                            }
                        }
                    }
                } else {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = rememberRipple(),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onClick()
                        },
                    )
                }
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
