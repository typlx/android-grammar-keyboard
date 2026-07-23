package com.typlx.keyboard.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import com.typlx.keyboard.InputLanguage
import com.typlx.keyboard.KeyboardLayout
import com.typlx.keyboard.LAYOUT_QWERTY
import com.typlx.keyboard.OneHandedMode
import com.typlx.keyboard.SuggestionState
import com.typlx.keyboard.TextShortcut
import com.typlx.keyboard.ToneOption
import com.typlx.keyboard.TranslationLanguage
import com.typlx.keyboard.ui.theme.LocalKeyboardColors

private val NUM_ROW = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")

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
    layout: KeyboardLayout = LAYOUT_QWERTY,
    canUndo: Boolean = false,
    returnKeyDescription: String = "Return",
    emojiRecents: List<String> = emptyList(),
    suggestionState: SuggestionState = SuggestionState.Idle,
    autoShiftSignal: Long = 0L,
    isTonePanel: Boolean = false,
    isApplyingTone: Boolean = false,
    toneError: String? = null,
    isTranslatePanel: Boolean = false,
    isApplyingTranslation: Boolean = false,
    translateError: String? = null,
    clipboardItems: List<String> = emptyList(),
    shortcuts: List<TextShortcut> = emptyList(),
    onShortcutInsert: (String) -> Unit = {},
    onOpenShortcutsManager: () -> Unit = {},
    isVoiceListening: Boolean = false,
    voicePartialText: String = "",
    voiceError: String? = null,
    onKeyPress: (String) -> Unit,
    onSpacePress: () -> Unit = {},
    onDelete: () -> Unit,
    onDeleteWord: () -> Unit,
    onFixGrammar: () -> Unit,
    onReturn: () -> Unit,
    onErrorDismiss: () -> Unit,
    onUndoGrammarFix: () -> Unit = {},
    onEmojiPress: (String) -> Unit = {},
    isSmartComposing: Boolean = false,
    onAcceptSuggestion: () -> Unit = {},
    onDismissSuggestion: () -> Unit = {},
    onWordSuggestionAccepted: (String) -> Unit = {},
    onEmojiSuggestionTapped: (String) -> Unit = {},
    onSmartCompose: () -> Unit = {},
    onSmartClipboardPaste: () -> Unit = {},
    onSmartClipboardDismiss: () -> Unit = {},
    onToneToggle: () -> Unit = {},
    onToneDismiss: () -> Unit = {},
    onToneSelect: (ToneOption) -> Unit = {},
    onToneErrorDismiss: () -> Unit = {},
    onTranslateToggle: () -> Unit = {},
    onTranslateDismiss: () -> Unit = {},
    onTranslateSelect: (TranslationLanguage) -> Unit = {},
    onTranslateErrorDismiss: () -> Unit = {},
    onClipboardPaste: (String) -> Unit = {},
    onClipboardClear: () -> Unit = {},
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
    hasSelection: Boolean = false,
    showNumberRow: Boolean = true,
    activeInputLanguage: InputLanguage = InputLanguage.ENGLISH,
    enabledInputLanguages: List<InputLanguage> = listOf(InputLanguage.ENGLISH),
    onLanguageSwitch: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onVoiceToggle: () -> Unit = {},
    onVoiceErrorDismiss: () -> Unit = {},
    isNumPad: Boolean = false,
    isNumPadPhoneMode: Boolean = false,
    isNumPadDecimal: Boolean = false,
    isNumPadSigned: Boolean = false,
    keyHeight: Dp = 46.dp,
    keyPressPreviewEnabled: Boolean = true,
    swipeTypingEnabled: Boolean = true,
    landscapeSplitEnabled: Boolean = true,
    onSwipePath: (List<String>) -> Unit = {},
    oneHandedMode: OneHandedMode = OneHandedMode.OFF,
    onOneHandedModeChange: (OneHandedMode) -> Unit = {},
) {
    var shiftState by remember { mutableStateOf(ShiftState.OFF) }
    var lastShiftTapMs by remember { mutableLongStateOf(0L) }
    var isSymbols by remember { mutableStateOf(false) }
    var isEmoji by remember { mutableStateOf(false) }
    var isNav by remember { mutableStateOf(false) }
    var isClipboard by remember { mutableStateOf(false) }
    var isShortcuts by remember { mutableStateOf(false) }
    // Triple: (displayLabel, isCaps, alternatives) — non-null when the alternatives bar is visible.
    var activeAlternatives by remember { mutableStateOf<Triple<String, Boolean, List<String>>?>(null) }
    val colors = LocalKeyboardColors.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isSplitLayout = isLandscape && landscapeSplitEnabled
    val effectiveKeyHeight = if (isLandscape) (keyHeight.value * 0.70f).dp else keyHeight
    val onOneHandedToggle = {
        val next = when (oneHandedMode) {
            OneHandedMode.OFF -> OneHandedMode.LEFT
            OneHandedMode.LEFT -> OneHandedMode.RIGHT
            OneHandedMode.RIGHT -> OneHandedMode.OFF
        }
        onOneHandedModeChange(next)
    }
    var pressedKey by remember { mutableStateOf<KeyPressInfo?>(null) }
    var rootCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    // Shared registry that each character KeyButton populates with its window-coordinate bounds.
    val keyRegistry = remember { mutableMapOf<String, Rect>() }
    val swipePathReceiver: ((List<String>) -> Unit)? = if (swipeTypingEnabled) {
        { path -> onSwipePath(path) }
    } else null

    // Window-coordinate points accumulated during an active swipe gesture.
    var swipeTrailWindowPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    val swipeTrailUpdater: ((Offset?) -> Unit)? = if (swipeTypingEnabled) { point ->
        if (point == null) {
            swipeTrailWindowPoints = emptyList()
        } else {
            swipeTrailWindowPoints = swipeTrailWindowPoints + point
        }
    } else null

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

    if (isNumPad) {
        NumPadKeyboard(
            isPhoneMode = isNumPadPhoneMode,
            isDecimalAllowed = isNumPadDecimal,
            isSignedAllowed = isNumPadSigned,
            returnKeyDescription = returnKeyDescription,
            onKeyPress = onKeyPress,
            onDelete = onDelete,
            onDeleteWord = onDeleteWord,
            onReturn = onReturn,
            colors = colors,
        )
        return
    }

    if (isShortcuts) {
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
                isClipboard = false,
                isShortcuts = true,
                isTonePanel = false,
                isApplyingTone = isApplyingTone,
                isTranslatePanel = false,
                isApplyingTranslation = isApplyingTranslation,
                isVoiceListening = isVoiceListening,
                onFixGrammar = onFixGrammar,
                onErrorDismiss = onErrorDismiss,
                onUndoGrammarFix = onUndoGrammarFix,
                onEmojiToggle = { isEmoji = true; isShortcuts = false },
                onNavToggle = { isNav = true; isShortcuts = false },
                onClipboardToggle = { isClipboard = true; isShortcuts = false },
                onShortcutsToggle = { isShortcuts = false },
                onToneToggle = onToneToggle,
                onTranslateToggle = onTranslateToggle,
                onOpenSettings = onOpenSettings,
                onVoiceToggle = onVoiceToggle,
                oneHandedMode = oneHandedMode,
                onOneHandedModeToggle = onOneHandedToggle,
                activeInputLanguage = activeInputLanguage,
                enabledInputLanguages = enabledInputLanguages,
                onLanguageSwitch = onLanguageSwitch,
            )
            ShortcutsPanel(
                shortcuts = shortcuts,
                onInsert = onShortcutInsert,
                onManage = onOpenShortcutsManager,
                colors = colors,
            )
        }
        return
    }

    if (isClipboard) {
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
                isClipboard = true,
                isShortcuts = false,
                isTonePanel = false,
                isApplyingTone = isApplyingTone,
                isTranslatePanel = false,
                isApplyingTranslation = isApplyingTranslation,
                isVoiceListening = isVoiceListening,
                onFixGrammar = onFixGrammar,
                onErrorDismiss = onErrorDismiss,
                onUndoGrammarFix = onUndoGrammarFix,
                onEmojiToggle = { isEmoji = true },
                onNavToggle = { isNav = true },
                onClipboardToggle = { isClipboard = false },
                onShortcutsToggle = { isShortcuts = true; isClipboard = false },
                onToneToggle = onToneToggle,
                onTranslateToggle = onTranslateToggle,
                onOpenSettings = onOpenSettings,
                onVoiceToggle = onVoiceToggle,
                oneHandedMode = oneHandedMode,
                onOneHandedModeToggle = onOneHandedToggle,
                activeInputLanguage = activeInputLanguage,
                enabledInputLanguages = enabledInputLanguages,
                onLanguageSwitch = onLanguageSwitch,
            )
            ClipboardPanel(
                items = clipboardItems,
                onPaste = onClipboardPaste,
                onClear = onClipboardClear,
                colors = colors,
            )
        }
        return
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
                isClipboard = false,
                isShortcuts = false,
                isTonePanel = false,
                isApplyingTone = isApplyingTone,
                isTranslatePanel = false,
                isApplyingTranslation = isApplyingTranslation,
                isVoiceListening = isVoiceListening,
                onFixGrammar = onFixGrammar,
                onErrorDismiss = onErrorDismiss,
                onUndoGrammarFix = onUndoGrammarFix,
                onEmojiToggle = { isEmoji = true },
                onNavToggle = { isNav = false },
                onClipboardToggle = { isClipboard = true },
                onShortcutsToggle = { isShortcuts = true; isNav = false },
                onToneToggle = onToneToggle,
                onTranslateToggle = onTranslateToggle,
                onOpenSettings = onOpenSettings,
                onVoiceToggle = onVoiceToggle,
                oneHandedMode = oneHandedMode,
                onOneHandedModeToggle = onOneHandedToggle,
                activeInputLanguage = activeInputLanguage,
                enabledInputLanguages = enabledInputLanguages,
                onLanguageSwitch = onLanguageSwitch,
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
                isClipboard = false,
                isShortcuts = false,
                isTonePanel = false,
                isApplyingTone = isApplyingTone,
                isTranslatePanel = false,
                isApplyingTranslation = isApplyingTranslation,
                isVoiceListening = isVoiceListening,
                onFixGrammar = onFixGrammar,
                onErrorDismiss = onErrorDismiss,
                onUndoGrammarFix = onUndoGrammarFix,
                onEmojiToggle = { isEmoji = false },
                onNavToggle = { isNav = true },
                onClipboardToggle = { isClipboard = true },
                onShortcutsToggle = { isShortcuts = true; isEmoji = false },
                onToneToggle = onToneToggle,
                onTranslateToggle = onTranslateToggle,
                onOpenSettings = onOpenSettings,
                onVoiceToggle = onVoiceToggle,
                oneHandedMode = oneHandedMode,
                onOneHandedModeToggle = onOneHandedToggle,
                activeInputLanguage = activeInputLanguage,
                enabledInputLanguages = enabledInputLanguages,
                onLanguageSwitch = onLanguageSwitch,
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

    val notifier: ((KeyPressInfo?) -> Unit)? = if (keyPressPreviewEnabled) {
        { info -> pressedKey = info }
    } else null

    CompositionLocalProvider(
        LocalKeyPressNotifier provides notifier,
        LocalKeyPositionRegistry provides keyRegistry,
        LocalSwipePathReceiver provides swipePathReceiver,
        LocalSwipeTrailUpdater provides swipeTrailUpdater,
    ) {
        OneHandedWrapper(
            mode = oneHandedMode,
            onModeChange = onOneHandedModeChange,
            colors = colors,
            onPositioned = { rootCoords = it },
        ) {
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
                    isClipboard = false,
                    isShortcuts = false,
                    isTonePanel = isTonePanel,
                    isApplyingTone = isApplyingTone,
                    isTranslatePanel = isTranslatePanel,
                    isApplyingTranslation = isApplyingTranslation,
                    isVoiceListening = isVoiceListening,
                    hasSelection = hasSelection,
                    onFixGrammar = onFixGrammar,
                    onErrorDismiss = onErrorDismiss,
                    onUndoGrammarFix = onUndoGrammarFix,
                    onEmojiToggle = { isEmoji = true },
                    onNavToggle = { isNav = true },
                    onClipboardToggle = { isClipboard = true },
                    onShortcutsToggle = { isShortcuts = true },
                    onToneToggle = onToneToggle,
                    onTranslateToggle = onTranslateToggle,
                    onOpenSettings = onOpenSettings,
                    onVoiceToggle = onVoiceToggle,
                    oneHandedMode = oneHandedMode,
                    onOneHandedModeToggle = onOneHandedToggle,
                    activeInputLanguage = activeInputLanguage,
                    enabledInputLanguages = enabledInputLanguages,
                    onLanguageSwitch = onLanguageSwitch,
                )

                when {
                    isTonePanel -> TonePanel(
                        isApplying = isApplyingTone,
                        error = toneError,
                        onToneSelect = onToneSelect,
                        onDismiss = onToneDismiss,
                        onErrorDismiss = onToneErrorDismiss,
                        colors = colors,
                    )
                    isTranslatePanel -> TranslationPanel(
                        isApplying = isApplyingTranslation,
                        error = translateError,
                        onLanguageSelect = onTranslateSelect,
                        onDismiss = onTranslateDismiss,
                        onErrorDismiss = onTranslateErrorDismiss,
                        colors = colors,
                    )
                    isVoiceListening || voicePartialText.isNotEmpty() || voiceError != null -> VoiceStrip(
                        isListening = isVoiceListening,
                        partialText = voicePartialText,
                        error = voiceError,
                        onDismissError = onVoiceErrorDismiss,
                    )
                    else -> SuggestionStrip(
                        state = suggestionState,
                        isSmartComposing = isSmartComposing,
                        onAccept = onAcceptSuggestion,
                        onDismiss = onDismissSuggestion,
                        onWordSuggestionAccepted = onWordSuggestionAccepted,
                        onEmojiSuggestionTapped = onEmojiSuggestionTapped,
                        onSmartCompose = onSmartCompose,
                        onUndoAutocorrect = onUndoGrammarFix,
                        onSmartClipboardPaste = onSmartClipboardPaste,
                        onSmartClipboardDismiss = onSmartClipboardDismiss,
                    )
                }

                activeAlternatives?.let { (label, _, alts) ->
                    AlternativesBar(
                        originalKey = label,
                        alternatives = alts,
                        colors = colors,
                        onSelectAlternative = { char -> shiftOnceKeyPress(char) },
                        onDismiss = { activeAlternatives = null },
                    )
                }

                if (showNumberRow) {
                    val numRowHeight = (effectiveKeyHeight.value * 38f / 46f).dp
                    if (isSplitLayout) {
                        SplitNumberRow(keys = NUM_ROW, onKeyPress = onKeyPress, colors = colors, height = numRowHeight)
                    } else {
                        NumberRow(keys = NUM_ROW, onKeyPress = onKeyPress, colors = colors, height = numRowHeight)
                    }
                }

                if (isSymbols) {
                    KeyRow(SYM_ROW1, isCaps = false, onKeyPress = shiftOnceKeyPress, colors = colors, height = effectiveKeyHeight)
                    KeyRow(SYM_ROW2, isCaps = false, onKeyPress = shiftOnceKeyPress, colors = colors, height = effectiveKeyHeight)
                    SymbolRow3(SYM_ROW3, onKeyPress = shiftOnceKeyPress, onDelete = onDelete, onDeleteWord = onDeleteWord, colors = colors, height = effectiveKeyHeight)
                } else if (isSplitLayout) {
                    SplitKeyRow(layout.row1, isCaps = isCaps, onKeyPress = shiftOnceKeyPress, colors = colors, onShowAlternatives = showAlternatives, alternativesMap = layout.longPressAlternatives, height = effectiveKeyHeight)
                    SplitKeyRow(layout.row2, isCaps = isCaps, onKeyPress = shiftOnceKeyPress, colors = colors, onShowAlternatives = showAlternatives, alternativesMap = layout.longPressAlternatives, height = effectiveKeyHeight)
                    SplitAlphaRow3(
                        keys = layout.row3,
                        shiftState = shiftState,
                        onShiftTap = onShiftTap,
                        onKeyPress = shiftOnceKeyPress,
                        onDelete = onDelete,
                        onDeleteWord = onDeleteWord,
                        onShowAlternatives = showAlternatives,
                        alternativesMap = layout.longPressAlternatives,
                        colors = colors,
                        height = effectiveKeyHeight,
                    )
                } else {
                    KeyRow(layout.row1, isCaps = isCaps, onKeyPress = shiftOnceKeyPress, colors = colors, onShowAlternatives = showAlternatives, alternativesMap = layout.longPressAlternatives, height = effectiveKeyHeight)
                    KeyRow(layout.row2, isCaps = isCaps, onKeyPress = shiftOnceKeyPress, colors = colors, onShowAlternatives = showAlternatives, alternativesMap = layout.longPressAlternatives, height = effectiveKeyHeight)
                    AlphaRow3(
                        keys = layout.row3,
                        shiftState = shiftState,
                        onShiftTap = onShiftTap,
                        onKeyPress = shiftOnceKeyPress,
                        onDelete = onDelete,
                        onDeleteWord = onDeleteWord,
                        onShowAlternatives = showAlternatives,
                        alternativesMap = layout.longPressAlternatives,
                        colors = colors,
                        height = effectiveKeyHeight,
                    )
                }

                if (isSplitLayout) {
                    SplitBottomRow(
                        isSymbols = isSymbols,
                        onSymbolToggle = { isSymbols = !isSymbols },
                        onKeyPress = shiftOnceKeyPress,
                        onSpacePress = onSpacePress,
                        onReturn = onReturn,
                        returnKeyDescription = returnKeyDescription,
                        colors = colors,
                        height = effectiveKeyHeight,
                    )
                } else {
                    BottomRow(
                        isSymbols = isSymbols,
                        onSymbolToggle = { isSymbols = !isSymbols },
                        onKeyPress = shiftOnceKeyPress,
                        onSpacePress = onSpacePress,
                        onReturn = onReturn,
                        returnKeyDescription = returnKeyDescription,
                        colors = colors,
                        height = effectiveKeyHeight,
                    )
                }
            }

            // Swipe trail overlay
            val trailPoints = swipeTrailWindowPoints
            if (trailPoints.size >= 2) {
                rootCoords?.let { root ->
                    val rootPos = root.positionInWindow()
                    val localPoints = trailPoints.map { wp ->
                        Offset(wp.x - rootPos.x, wp.y - rootPos.y)
                    }
                    val trailColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(90f),
                    ) {
                        val path = Path().apply {
                            moveTo(localPoints[0].x, localPoints[0].y)
                            for (i in 1 until localPoints.size) {
                                lineTo(localPoints[i].x, localPoints[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = trailColor,
                            style = Stroke(
                                width = 10f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                            ),
                        )
                    }
                }
            }

            // Key press popup overlay
            if (keyPressPreviewEnabled) {
                val density = LocalDensity.current
                pressedKey?.let { info ->
                    rootCoords?.let { root ->
                        val rootPos = root.positionInWindow()
                        val localX = info.windowX - rootPos.x
                        val localY = info.windowY - rootPos.y
                        val popupWidthPx = (info.widthPx * 1.5f).coerceIn(
                            with(density) { 36.dp.toPx() },
                            with(density) { 72.dp.toPx() },
                        )
                        val popupHeightPx = with(density) { 44.dp.toPx() }
                        val popupX = (localX + info.widthPx / 2f - popupWidthPx / 2f)
                            .coerceIn(0f, root.size.width.toFloat() - popupWidthPx)
                        val popupY = (localY - popupHeightPx - with(density) { 2.dp.toPx() })
                            .coerceAtLeast(0f)
                        Box(
                            modifier = Modifier
                                .zIndex(100f)
                                .absoluteOffset { IntOffset(popupX.toInt(), popupY.toInt()) }
                                .width(with(density) { popupWidthPx.toDp() })
                                .height(44.dp)
                                .shadow(4.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.keyBg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = info.label,
                                color = colors.keyText,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolbarRow(
    isFixingGrammar: Boolean,
    grammarError: String?,
    canUndo: Boolean,
    isEmoji: Boolean,
    isNav: Boolean,
    isClipboard: Boolean,
    isShortcuts: Boolean,
    isTonePanel: Boolean,
    isApplyingTone: Boolean,
    isTranslatePanel: Boolean,
    isApplyingTranslation: Boolean,
    isVoiceListening: Boolean,
    hasSelection: Boolean = false,
    oneHandedMode: OneHandedMode = OneHandedMode.OFF,
    activeInputLanguage: InputLanguage = InputLanguage.ENGLISH,
    enabledInputLanguages: List<InputLanguage> = listOf(InputLanguage.ENGLISH),
    onFixGrammar: () -> Unit,
    onErrorDismiss: () -> Unit,
    onUndoGrammarFix: () -> Unit,
    onEmojiToggle: () -> Unit,
    onNavToggle: () -> Unit,
    onClipboardToggle: () -> Unit,
    onShortcutsToggle: () -> Unit,
    onToneToggle: () -> Unit,
    onTranslateToggle: () -> Unit,
    onOpenSettings: () -> Unit,
    onVoiceToggle: () -> Unit,
    onOneHandedModeToggle: () -> Unit = {},
    onLanguageSwitch: () -> Unit = {},
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

        val fixButtonLabel = when {
            isFixingGrammar -> "Fixing…"
            hasSelection -> "Fix Selected"
            else -> "Fix Grammar"
        }
        val fixButtonDesc = if (isFixingGrammar) "Fixing grammar, please wait"
            else if (hasSelection) "Fix grammar in selected text" else "Fix grammar"
        Button(
            onClick = onFixGrammar,
            enabled = !isFixingGrammar && !isApplyingTone,
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
                text = fixButtonLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        val toneButtonDesc = if (isTonePanel) "Close tone panel" else "Open tone rewriter"
        OutlinedButton(
            onClick = onToneToggle,
            enabled = !isFixingGrammar && !isApplyingTone,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
            modifier = Modifier
                .height(36.dp)
                .semantics { contentDescription = toneButtonDesc },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (isTonePanel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Text(
                text = "Tone",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        val translateButtonDesc = if (isTranslatePanel) "Close translation panel" else "Open translation panel"
        IconButton(
            onClick = onTranslateToggle,
            enabled = !isFixingGrammar && !isApplyingTone && !isApplyingTranslation,
            modifier = Modifier
                .size(36.dp)
                .semantics { contentDescription = translateButtonDesc },
        ) {
            Icon(
                imageVector = Icons.Default.Translate,
                contentDescription = null,
                tint = if (isTranslatePanel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
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

        val clipboardDesc = if (isClipboard) "Close clipboard" else "Open clipboard history"
        IconButton(
            onClick = onClipboardToggle,
            modifier = Modifier
                .size(36.dp)
                .semantics { contentDescription = clipboardDesc },
        ) {
            Icon(
                imageVector = Icons.Default.ContentPaste,
                contentDescription = null,
                tint = if (isClipboard) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }

        val shortcutsDesc = if (isShortcuts) "Close shortcuts panel" else "Open text shortcuts"
        IconButton(
            onClick = onShortcutsToggle,
            modifier = Modifier
                .size(36.dp)
                .semantics { contentDescription = shortcutsDesc },
        ) {
            Text(
                text = "AB",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isShortcuts) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        val voiceDesc = if (isVoiceListening) "Stop voice input" else "Start voice input"
        IconButton(
            onClick = onVoiceToggle,
            modifier = Modifier
                .size(36.dp)
                .semantics { contentDescription = voiceDesc },
        ) {
            Icon(
                imageVector = if (isVoiceListening) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = null,
                tint = if (isVoiceListening) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }

        val oneHandedDesc = when (oneHandedMode) {
            OneHandedMode.OFF -> "Enable one-handed mode (left)"
            OneHandedMode.LEFT -> "Switch to right one-handed mode"
            OneHandedMode.RIGHT -> "Disable one-handed mode"
        }
        IconButton(
            onClick = onOneHandedModeToggle,
            modifier = Modifier
                .size(36.dp)
                .semantics { contentDescription = oneHandedDesc },
        ) {
            Text(
                text = when (oneHandedMode) {
                    OneHandedMode.OFF -> "⊣"
                    OneHandedMode.LEFT -> "⊢"
                    OneHandedMode.RIGHT -> "⊣"
                },
                fontSize = 16.sp,
                color = if (oneHandedMode != OneHandedMode.OFF) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (enabledInputLanguages.size > 1) {
            TextButton(
                onClick = onLanguageSwitch,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                modifier = Modifier
                    .height(36.dp)
                    .semantics { contentDescription = "Switch keyboard language, current: ${activeInputLanguage.displayName}" },
            ) {
                Text(
                    text = activeInputLanguage.code,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .size(36.dp)
                .semantics { contentDescription = "Open settings" },
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun OneHandedWrapper(
    mode: OneHandedMode,
    onModeChange: (OneHandedMode) -> Unit,
    colors: com.typlx.keyboard.ui.theme.KeyboardColors,
    onPositioned: (LayoutCoordinates) -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    when (mode) {
        OneHandedMode.OFF -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned(onPositioned),
            content = content,
        )
        OneHandedMode.LEFT -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.keyboardBg),
            verticalAlignment = Alignment.Bottom,
        ) {
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .onGloballyPositioned(onPositioned),
                content = content,
            )
            OneHandedHandle(
                modifier = Modifier.weight(0.3f),
                arrowToLeft = false,
                onFlip = { onModeChange(OneHandedMode.RIGHT) },
                onExit = { onModeChange(OneHandedMode.OFF) },
                colors = colors,
            )
        }
        OneHandedMode.RIGHT -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.keyboardBg),
            verticalAlignment = Alignment.Bottom,
        ) {
            OneHandedHandle(
                modifier = Modifier.weight(0.3f),
                arrowToLeft = true,
                onFlip = { onModeChange(OneHandedMode.LEFT) },
                onExit = { onModeChange(OneHandedMode.OFF) },
                colors = colors,
            )
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .onGloballyPositioned(onPositioned),
                content = content,
            )
        }
    }
}

@Composable
private fun OneHandedHandle(
    modifier: Modifier,
    arrowToLeft: Boolean,
    onFlip: () -> Unit,
    onExit: () -> Unit,
    colors: com.typlx.keyboard.ui.theme.KeyboardColors,
) {
    Box(
        modifier = modifier
            .background(colors.keyboardBg)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onExit() },
                    onTap = { onFlip() },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (arrowToLeft) "◀" else "▶",
            color = colors.keyText.copy(alpha = 0.4f),
            fontSize = 20.sp,
        )
    }
}
