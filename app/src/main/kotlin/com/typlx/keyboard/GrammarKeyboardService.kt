package com.typlx.keyboard

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import android.inputmethodservice.InputMethodService
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.compose.ui.unit.dp
import com.typlx.keyboard.ui.KeyboardScreen
import com.typlx.keyboard.ui.theme.ThemePreset
import com.typlx.keyboard.ui.theme.TyplxKeyboardTheme
import kotlinx.coroutines.*

class GrammarKeyboardService : InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    // --- Lifecycle plumbing required to host Compose inside an IME ---
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val vmStore = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = vmStore
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    // --- State exposed to the Compose tree ---
    var isFixingGrammar by mutableStateOf(false)
        private set
    var grammarError by mutableStateOf<String?>(null)
        private set
    var canUndo by mutableStateOf(false)
        private set
    var returnKeyDescription by mutableStateOf("Return")
        private set
    var emojiRecents by mutableStateOf<List<String>>(emptyList())
        private set
    var suggestionState by mutableStateOf<SuggestionState>(SuggestionState.Idle)
        private set
    var isTonePanel by mutableStateOf(false)
        private set
    var isApplyingTone by mutableStateOf(false)
        private set
    var toneError by mutableStateOf<String?>(null)
        private set
    var isTranslatePanel by mutableStateOf(false)
        private set
    var isApplyingTranslation by mutableStateOf(false)
        private set
    var translateError by mutableStateOf<String?>(null)
        private set
    var isVoiceListening by mutableStateOf(false)
        private set
    var voicePartialText by mutableStateOf("")
        private set
    var voiceError by mutableStateOf<String?>(null)
        private set
    var clipboardItems by mutableStateOf<List<String>>(emptyList())
        private set
    var shortcuts by mutableStateOf<List<TextShortcut>>(emptyList())
        private set
    var themePreset by mutableStateOf(ThemePreset.SYSTEM)
        private set
    var cornerRadiusDp by mutableStateOf(6)
        private set
    var keyAlphaPercent by mutableStateOf(100)
        private set
    var activeNumPadConfig by mutableStateOf(NumPadConfig(false, false, false, false))
        private set
    var keyboardLayout by mutableStateOf(LAYOUT_QWERTY)
        private set
    var keySizePreset by mutableStateOf(KeySizePreset.NORMAL)
        private set
    var keyHeightDp by mutableIntStateOf(PreferencesManager.KEY_HEIGHT_DP_DEFAULT)
        private set
    var showNumberRow by mutableStateOf(true)
        private set
    var keyPressPreviewEnabled by mutableStateOf(true)
        private set
    var swipeTypingEnabled by mutableStateOf(true)
        private set
    var isSmartComposing by mutableStateOf(false)
        private set
    var hasSelection by mutableStateOf(false)
        private set
    // Incremented each time the service wants KeyboardScreen to activate SHIFT_ONCE.
    private val _autoShiftSignal = mutableStateOf(0L)
    val autoShiftSignal: Long by _autoShiftSignal

    private val undoState = GrammarUndoState()
    private val emojiRecentsMgr = EmojiRecents()
    private val clipboardHistory = ClipboardHistory()
    private val personalWordList = PersonalWordList()
    private val textShortcutsManager = TextShortcutsManager()
    private val voiceInputManager = VoiceInputManager()
    private val wordPredictor by lazy { WordPredictor.fromContext(this) }
    private val autoCorrectManager = AutoCorrectManager()
    private val emojiSuggestionHelper = EmojiSuggestionHelper()
    private val swipeTypingDecoder = SwipeTypingDecoder()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var prefs: PreferencesManager
    private lateinit var grammarService: GrammarService
    private lateinit var grammarFixController: GrammarFixController
    private lateinit var autoSuggestController: AutoSuggestController
    private lateinit var toneRewriteController: ToneRewriteController
    private lateinit var translationController: TranslationController
    private lateinit var smartComposeController: SmartComposeController
    private lateinit var hapticHelper: HapticHelper
    private var keyboardView: View? = null

    // Epoch-ms of the last committed space (for double-space-to-period detection).
    private var lastSpacePressMs = 0L

    private var suggestionDebounceJob: Job? = null
    // Counts how many upcoming onUpdateSelection callbacks to suppress (caused by our own
    // deleteSurroundingText / commitText calls when applying a suggestion).
    private var suppressSuggestionTriggerCount = 0

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        prefs = PreferencesManager(applicationContext)
        CrashReporter.configure(prefs.crashReportingEnabled)
        grammarService = GrammarService()
        grammarFixController = GrammarFixController(grammarService, personalWordList)
        autoSuggestController = AutoSuggestController(grammarService, personalWordList)
        toneRewriteController = ToneRewriteController(grammarService)
        translationController = TranslationController(grammarService)
        smartComposeController = SmartComposeController(grammarService)
        hapticHelper = HapticHelper { prefs.hapticFeedbackEnabled }
        voiceInputManager.onResult = ::onVoiceResult
        voiceInputManager.onStateChange = { state ->
            isVoiceListening = state is VoiceInputState.Listening
            voicePartialText = (state as? VoiceInputState.Partial)?.text ?: ""
        }
        voiceInputManager.onError = { msg -> voiceError = msg }
        // Load persisted data and pre-warm the HTTP client on an IO thread so the
        // main thread is not blocked during IME service creation.
        serviceScope.launch(Dispatchers.IO) {
            val emojiJson = getSharedPreferences(EMOJI_PREFS, Context.MODE_PRIVATE)
                .getString(EMOJI_RECENTS_KEY, null)
            val clipJson = getSharedPreferences(CLIPBOARD_PREFS, Context.MODE_PRIVATE)
                .getString(CLIPBOARD_HISTORY_KEY, null)
            val wordJson = getSharedPreferences(WORD_LIST_PREFS, Context.MODE_PRIVATE)
                .getString(WORD_LIST_KEY, null)
            val shortcutsJson = getSharedPreferences(SHORTCUTS_PREFS, Context.MODE_PRIVATE)
                .getString(SHORTCUTS_KEY, null)
            GrammarService.prewarm()
            withContext(Dispatchers.Main) {
                emojiJson?.let { emojiRecentsMgr.loadFromJson(it); emojiRecents = emojiRecentsMgr.recents }
                clipJson?.let { clipboardHistory.loadFromJson(it); clipboardItems = clipboardHistory.items }
                if (wordJson != null) personalWordList.loadFromJson(wordJson)
                if (shortcutsJson != null) {
                    textShortcutsManager.loadFromJson(shortcutsJson)
                } else {
                    TextShortcutsManager.defaults().forEach { textShortcutsManager.add(it.shortcut, it.expansion) }
                }
                shortcuts = textShortcutsManager.getAll()
            }
        }
    }

    override fun onCreateInputView(): View {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        return ComposeView(this).also { keyboardView = it }.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setViewTreeLifecycleOwner(this@GrammarKeyboardService)
            setViewTreeViewModelStoreOwner(this@GrammarKeyboardService)
            setViewTreeSavedStateRegistryOwner(this@GrammarKeyboardService)

            setContent {
                val keyHeight = keyHeightDp.dp
                TyplxKeyboardTheme(
                    preset = themePreset,
                    cornerRadiusDp = cornerRadiusDp,
                    keyAlphaPercent = keyAlphaPercent,
                ) {
                    KeyboardScreen(
                        isFixingGrammar = isFixingGrammar,
                        grammarError = grammarError,
                        layout = keyboardLayout,
                        canUndo = canUndo,
                        returnKeyDescription = returnKeyDescription,
                        emojiRecents = emojiRecents,
                        suggestionState = suggestionState,
                        autoShiftSignal = autoShiftSignal,
                        isTonePanel = isTonePanel,
                        isApplyingTone = isApplyingTone,
                        toneError = toneError,
                        isTranslatePanel = isTranslatePanel,
                        isApplyingTranslation = isApplyingTranslation,
                        translateError = translateError,
                        clipboardItems = clipboardItems,
                        shortcuts = shortcuts,
                        onShortcutInsert = ::insertShortcutExpansion,
                        onOpenShortcutsManager = ::openShortcutsManager,
                        isVoiceListening = isVoiceListening,
                        voicePartialText = voicePartialText,
                        voiceError = voiceError,
                        isSmartComposing = isSmartComposing,
                        onKeyPress = ::commitText,
                        onSpacePress = ::onSpacePress,
                        onDelete = ::deleteChar,
                        onDeleteWord = ::deleteWord,
                        onFixGrammar = ::launchGrammarFix,
                        onReturn = ::commitReturn,
                        onErrorDismiss = { grammarError = null },
                        onUndoGrammarFix = ::undoGrammarFix,
                        onEmojiPress = ::commitEmoji,
                        onAcceptSuggestion = ::acceptSuggestion,
                        onDismissSuggestion = ::dismissSuggestion,
                        onWordSuggestionAccepted = ::acceptWordSuggestion,
                        onEmojiSuggestionTapped = ::acceptEmojiSuggestion,
                        onSmartCompose = ::triggerSmartCompose,
                        onToneToggle = { if (isTonePanel) dismissTonePanel() else openTonePanel() },
                        onToneDismiss = ::dismissTonePanel,
                        onToneSelect = ::launchToneRewrite,
                        onToneErrorDismiss = { toneError = null },
                        onTranslateToggle = { if (isTranslatePanel) dismissTranslatePanel() else openTranslatePanel() },
                        onTranslateDismiss = ::dismissTranslatePanel,
                        onTranslateSelect = ::launchTranslation,
                        onTranslateErrorDismiss = { translateError = null },
                        onClipboardPaste = ::pasteClipboardItem,
                        onClipboardClear = ::clearClipboardHistory,
                        onMoveCursorLeft = { moveCursor(KeyEvent.KEYCODE_DPAD_LEFT) },
                        onMoveCursorRight = { moveCursor(KeyEvent.KEYCODE_DPAD_RIGHT) },
                        onMoveCursorUp = { moveCursor(KeyEvent.KEYCODE_DPAD_UP) },
                        onMoveCursorDown = { moveCursor(KeyEvent.KEYCODE_DPAD_DOWN) },
                        onMoveCursorWordLeft = { moveCursorWord(forward = false) },
                        onMoveCursorWordRight = { moveCursorWord(forward = true) },
                        onCursorHome = { moveCursor(KeyEvent.KEYCODE_MOVE_HOME) },
                        onCursorEnd = { moveCursor(KeyEvent.KEYCODE_MOVE_END) },
                        onSelectAll = { currentInputConnection?.performContextMenuAction(android.R.id.selectAll) },
                        onCopyText = { currentInputConnection?.performContextMenuAction(android.R.id.copy) },
                        onCutText = { currentInputConnection?.performContextMenuAction(android.R.id.cut) },
                        onPasteText = { currentInputConnection?.performContextMenuAction(android.R.id.paste) },
                        hasSelection = hasSelection,
                        showNumberRow = showNumberRow,
                        onOpenSettings = ::openSettings,
                        onVoiceToggle = ::toggleVoiceInput,
                        onVoiceErrorDismiss = { voiceError = null },
                        isNumPad = activeNumPadConfig.isNumPad,
                        isNumPadPhoneMode = activeNumPadConfig.isPhoneMode,
                        isNumPadDecimal = activeNumPadConfig.isDecimalAllowed,
                        isNumPadSigned = activeNumPadConfig.isSignedAllowed,
                        keyHeight = keyHeight,
                        keyPressPreviewEnabled = keyPressPreviewEnabled,
                        swipeTypingEnabled = swipeTypingEnabled,
                        onSwipePath = ::onSwipePath,
                    )
                }
            }
        }
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int,
        newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int,
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        hasSelection = newSelStart >= 0 && newSelEnd > newSelStart
        if (suppressSuggestionTriggerCount > 0) {
            suppressSuggestionTriggerCount--
            return
        }
        updateWordPredictions()
        scheduleAutoSuggest()
    }

    private fun isPrivateField(): Boolean =
        isPrivateInputType(currentInputEditorInfo?.inputType ?: 0)

    private fun getCurrentWordPrefix(ic: android.view.inputmethod.InputConnection): String {
        val before = ic.getTextBeforeCursor(50, 0)?.toString() ?: return ""
        val lastBoundary = before.lastIndexOfAny(charArrayOf(' ', '\n', '\t', '\r', '.', ',', '!', '?', ';', ':'))
        return if (lastBoundary == -1) before else before.substring(lastBoundary + 1)
    }

    private fun getLastCompletedWord(ic: android.view.inputmethod.InputConnection): String {
        val before = ic.getTextBeforeCursor(100, 0)?.toString() ?: return ""
        val trimmed = before.trimEnd(' ', '\t', '\n')
        val lastBoundary = trimmed.lastIndexOfAny(charArrayOf(' ', '\n', '\t', '.', ',', '!', '?', ';', ':'))
        return if (lastBoundary == -1) trimmed else trimmed.substring(lastBoundary + 1)
    }

    private fun updateWordPredictions() {
        if (!prefs.wordPredictionEnabled) return
        if (suggestionState is SuggestionState.Available || suggestionState == SuggestionState.Loading) return
        if (isPrivateField()) return
        val ic = currentInputConnection ?: return
        suggestionState = buildWordSuggestionState(ic)
    }

    private fun buildWordSuggestionState(ic: InputConnection): SuggestionState {
        if (!prefs.wordPredictionEnabled) return SuggestionState.Idle
        val prefix = getCurrentWordPrefix(ic)
        val predictions = wordPredictor.predict(prefix, personalWordList.getAll())
        val emojis = if (prefs.emojiSuggestionsEnabled && prefix.isEmpty()) {
            emojiSuggestionHelper.suggest(getLastCompletedWord(ic))
        } else emptyList()
        return when {
            predictions.isNotEmpty() || emojis.isNotEmpty() -> SuggestionState.WordSuggestions(predictions, emojis)
            else -> SuggestionState.Idle
        }
    }

    private fun scheduleAutoSuggest() {
        if (!prefs.autoSuggestEnabled || !prefs.isConfigured || isFixingGrammar || isPrivateField()) return
        suggestionDebounceJob?.cancel()
        suggestionDebounceJob = serviceScope.launch {
            delay(1500L)
            runAutoSuggest()
        }
    }

    private suspend fun runAutoSuggest() {
        val ic = currentInputConnection ?: return
        suggestionState = SuggestionState.Loading
        val result = autoSuggestController.suggest(ic, prefs.apiUrl, prefs.model, prefs.apiToken,
            systemPromptSuffix = prefs.grammarInstructionSuffix)
        if (result == SuggestionState.Idle) {
            suggestionState = buildWordSuggestionState(ic)
        } else {
            suggestionState = result
        }
    }

    fun acceptSuggestion() {
        val state = suggestionState as? SuggestionState.Available ?: return
        val ic = currentInputConnection ?: return
        suggestionState = SuggestionState.Idle
        suppressSuggestionTriggerCount = 2
        ic.deleteSurroundingText(state.original.length, 0)
        ic.commitText(state.corrected, 1)
        undoState.recordFix(original = state.original, fixed = state.corrected)
        canUndo = true
    }

    fun dismissSuggestion() {
        suggestionDebounceJob?.cancel()
        suggestionState = SuggestionState.Idle
        updateWordPredictions()
    }

    fun acceptWordSuggestion(word: String) {
        val ic = currentInputConnection ?: return
        val prefix = getCurrentWordPrefix(ic)
        suppressSuggestionTriggerCount = 2
        if (prefix.isNotEmpty()) ic.deleteSurroundingText(prefix.length, 0)
        ic.commitText("$word ", 1)
        suggestionState = SuggestionState.Idle
        clearUndoState()
    }

    fun onSwipePath(path: List<String>) {
        val wordList = wordPredictor.wordList + personalWordList.getAll()
        val words = swipeTypingDecoder.decode(path, wordList)
        if (words.isNotEmpty()) {
            suggestionState = SuggestionState.WordSuggestions(words)
        }
    }

    fun acceptEmojiSuggestion(emoji: String) {
        val ic = currentInputConnection ?: return
        suppressSuggestionTriggerCount = 1
        ic.commitText(emoji, 1)
        suggestionState = SuggestionState.Idle
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        clearUndoState()
        toneError = null
        lastSpacePressMs = 0L
        // Theme prefs are cached in memory after first access — reading them here is fast.
        reloadThemePrefs()
        activeNumPadConfig = numPadConfig(info?.inputType ?: 0)
        returnKeyDescription = when (info?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)) {
            EditorInfo.IME_ACTION_SEARCH -> "Search"
            EditorInfo.IME_ACTION_SEND -> "Send"
            EditorInfo.IME_ACTION_GO -> "Go"
            EditorInfo.IME_ACTION_DONE -> "Done"
            EditorInfo.IME_ACTION_NEXT -> "Next field"
            EditorInfo.IME_ACTION_PREVIOUS -> "Previous field"
            else -> "Return"
        }
        // Auto-capitalise first character when the field caps flags say to do so.
        if (prefs.autoCapEnabled && shouldAutoCapOnFieldFocus(info?.inputType ?: 0)) {
            requestAutoShift()
        }
        // Reload user data off the main thread so first-frame rendering is not blocked
        // by SharedPreferences access or JSON parsing. Clipboard snapshot runs on main
        // thread afterwards (ClipboardManager access is UI-thread-bound on API 29+).
        serviceScope.launch(Dispatchers.IO) {
            val wordJson = getSharedPreferences(WORD_LIST_PREFS, Context.MODE_PRIVATE)
                .getString(WORD_LIST_KEY, null)
            val shortcutsJson = getSharedPreferences(SHORTCUTS_PREFS, Context.MODE_PRIVATE)
                .getString(SHORTCUTS_KEY, null)
            withContext(Dispatchers.Main) {
                if (wordJson != null) personalWordList.loadFromJson(wordJson)
                if (shortcutsJson != null) {
                    textShortcutsManager.loadFromJson(shortcutsJson)
                } else {
                    textShortcutsManager.loadFromJson("[]")
                    TextShortcutsManager.defaults().forEach { textShortcutsManager.add(it.shortcut, it.expansion) }
                }
                shortcuts = textShortcutsManager.getAll()
                snapshotClipboard()
            }
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        suggestionDebounceJob?.cancel()
        suggestionState = SuggestionState.Idle
        isTonePanel = false
        toneError = null
        isTranslatePanel = false
        translateError = null
        voiceInputManager.stop()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        super.onFinishInputView(finishingInput)
    }

    override fun onDestroy() {
        keyboardView = null
        voiceInputManager.destroy()
        serviceScope.cancel()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        vmStore.clear()
        super.onDestroy()
    }

    // --- Theme ---

    private fun reloadThemePrefs() {
        themePreset = prefs.themePreset
        cornerRadiusDp = prefs.cornerRadiusDp
        keyAlphaPercent = prefs.keyAlphaPercent
        keyboardLayout = layoutById(prefs.keyboardLayoutId)
        keySizePreset = prefs.keySizePreset
        keyHeightDp = prefs.keyHeightDp
        showNumberRow = prefs.showNumberRow
        keyPressPreviewEnabled = prefs.keyPressPreviewEnabled
        swipeTypingEnabled = prefs.swipeTypingEnabled
    }

    // --- Auto-cap ---

    private fun requestAutoShift() {
        _autoShiftSignal.value++
    }

    // --- Input helpers ---

    private fun commitText(text: String) {
        hapticHelper.tap(keyboardView)
        clearUndoState()
        currentInputConnection?.commitText(text, 1)
        if (text != " ") lastSpacePressMs = 0L
    }

    fun onSpacePress() {
        val ic = currentInputConnection
        val now = System.currentTimeMillis()
        val inputType = currentInputEditorInfo?.inputType ?: 0
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        val skipDoublespace =
            variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_URI

        // Text shortcut expansion: check the last word before committing space.
        if (ic != null && !skipDoublespace) {
            val before = ic.getTextBeforeCursor(100, 0)?.toString() ?: ""
            val lastWord = before.trimEnd().substringAfterLast(' ').substringAfterLast('\n')
            if (lastWord.isNotEmpty()) {
                val expansion = textShortcutsManager.expand(lastWord)
                if (expansion != null) {
                    hapticHelper.tap(keyboardView)
                    clearUndoState()
                    suppressSuggestionTriggerCount += 2
                    ic.deleteSurroundingText(lastWord.length, 0)
                    ic.commitText("$expansion ", 1)
                    lastSpacePressMs = 0L
                    return
                }
            }
        }

        // Local autocorrect: silently fix common typos on space press.
        if (ic != null && prefs.autocorrectEnabled && !isPrivateField()) {
            val before = ic.getTextBeforeCursor(100, 0)?.toString() ?: ""
            val lastWord = before.trimEnd().substringAfterLast(' ').substringAfterLast('\n')
            if (lastWord.isNotEmpty()) {
                val corrected = autoCorrectManager.correct(lastWord)
                if (corrected != null && corrected != lastWord) {
                    hapticHelper.tap(keyboardView)
                    undoState.recordFix(original = lastWord, fixed = corrected)
                    canUndo = true
                    suppressSuggestionTriggerCount += 2
                    ic.deleteSurroundingText(lastWord.length, 0)
                    ic.commitText("$corrected ", 1)
                    lastSpacePressMs = 0L
                    suggestionState = SuggestionState.AutoCorrected(lastWord, corrected)
                    return
                }
            }
        }

        if (prefs.doubleSpacePeriodEnabled && !skipDoublespace && ic != null && now - lastSpacePressMs < 500L) {
            val before = ic.getTextBeforeCursor(2, 0)?.toString() ?: ""
            // Replace the previous space with ". " when non-whitespace precedes it.
            if (before.length >= 2 && !before[before.length - 2].isWhitespace()) {
                hapticHelper.tap(keyboardView)
                clearUndoState()
                suppressSuggestionTriggerCount += 2
                ic.deleteSurroundingText(1, 0)
                ic.commitText(". ", 1)
                if (prefs.autoCapEnabled) requestAutoShift()
                lastSpacePressMs = 0L
                return
            }
        }

        commitText(" ")
        lastSpacePressMs = now

        // Auto-cap after sentence-ending punctuation followed by the space we just committed.
        if (prefs.autoCapEnabled) {
            val before = currentInputConnection?.getTextBeforeCursor(3, 0)?.toString() ?: return
            if (shouldShiftAfterSpace(before)) requestAutoShift()
        }
    }

    private fun deleteChar() {
        hapticHelper.tap(keyboardView)
        clearUndoState()
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    private fun deleteWord() {
        hapticHelper.tap(keyboardView)
        clearUndoState()
        val ic = currentInputConnection ?: return
        val text = ic.getTextBeforeCursor(200, 0)?.toString() ?: return
        val count = wordDeleteCount(text)
        if (count > 0) ic.deleteSurroundingText(count, 0)
    }

    private fun commitReturn() {
        hapticHelper.tap(keyboardView)
        clearUndoState()
        val ic = currentInputConnection ?: return
        val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
        if (action != null &&
            action != EditorInfo.IME_ACTION_NONE &&
            action != EditorInfo.IME_ACTION_UNSPECIFIED
        ) {
            ic.performEditorAction(action)
        } else {
            ic.commitText("\n", 1)
        }
    }

    private fun commitEmoji(emoji: String) {
        hapticHelper.tap(keyboardView)
        clearUndoState()
        currentInputConnection?.commitText(emoji, 1)
        emojiRecentsMgr.add(emoji)
        emojiRecents = emojiRecentsMgr.recents
        saveEmojiRecents()
    }

    private fun saveEmojiRecents() {
        getSharedPreferences(EMOJI_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(EMOJI_RECENTS_KEY, emojiRecentsMgr.toJson())
            .apply()
    }

    // --- Clipboard history ---

    private fun snapshotClipboard() {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val text = cm.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.coerceToText(applicationContext)
            ?.toString()
            ?: return
        if (text.isBlank()) return
        clipboardHistory.add(text)
        clipboardItems = clipboardHistory.items
        saveClipboardHistory()
    }

    fun pasteClipboardItem(text: String) {
        hapticHelper.tap(keyboardView)
        clearUndoState()
        currentInputConnection?.commitText(text, 1)
    }

    fun clearClipboardHistory() {
        clipboardHistory.clear()
        clipboardItems = emptyList()
        getSharedPreferences(CLIPBOARD_PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(CLIPBOARD_HISTORY_KEY)
            .apply()
    }

    private fun saveClipboardHistory() {
        getSharedPreferences(CLIPBOARD_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(CLIPBOARD_HISTORY_KEY, clipboardHistory.toJson())
            .apply()
    }

    private fun clearUndoState() {
        undoState.clear()
        canUndo = false
        if (suggestionState is SuggestionState.AutoCorrected) suggestionState = SuggestionState.Idle
    }

    fun undoGrammarFix() {
        val (original, fixed) = undoState.consume() ?: return
        canUndo = false
        if (suggestionState is SuggestionState.AutoCorrected) suggestionState = SuggestionState.Idle
        val ic = currentInputConnection ?: return
        ic.deleteSurroundingText(fixed.length, 0)
        ic.commitText(original, 1)
    }

    // --- Settings ---

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    private fun moveCursor(keyCode: Int) {
        val ic = currentInputConnection ?: return
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }

    private fun moveCursorWord(forward: Boolean) {
        val ic = currentInputConnection ?: return
        val metaState = KeyEvent.META_CTRL_ON
        val keyCode = if (forward) KeyEvent.KEYCODE_DPAD_RIGHT else KeyEvent.KEYCODE_DPAD_LEFT
        ic.sendKeyEvent(KeyEvent(0L, 0L, KeyEvent.ACTION_DOWN, keyCode, 0, metaState))
        ic.sendKeyEvent(KeyEvent(0L, 0L, KeyEvent.ACTION_UP, keyCode, 0, metaState))
    }

    companion object {
        private const val EMOJI_PREFS = "emoji_prefs"
        private const val EMOJI_RECENTS_KEY = "emoji_recents"
        private const val CLIPBOARD_PREFS = "clipboard_prefs"
        private const val CLIPBOARD_HISTORY_KEY = "clipboard_history"
        const val WORD_LIST_PREFS = "personal_word_list_prefs"
        const val WORD_LIST_KEY = "words_json"
        const val SHORTCUTS_PREFS = "text_shortcuts_prefs"
        const val SHORTCUTS_KEY = "shortcuts_json"
    }

    // --- Tone rewriter ---

    fun openTonePanel() {
        isTonePanel = true
        toneError = null
    }

    fun dismissTonePanel() {
        isTonePanel = false
        toneError = null
    }

    fun launchToneRewrite(tone: ToneOption) {
        if (isApplyingTone || isFixingGrammar || isPrivateField()) return
        val ic = currentInputConnection ?: return

        if (!FeatureGate.isEnabled(FeatureGate.Feature.TONE_SUGGESTIONS)) {
            toneError = getString(R.string.error_premium_required)
            return
        }

        if (!prefs.isConfigured) {
            toneError = getString(R.string.error_not_configured)
            return
        }

        val selectedText = if (hasSelection) ic.getSelectedText(0)?.toString() else null
        val isRealSelection = !selectedText.isNullOrBlank()
        val textBefore = if (isRealSelection) selectedText!! else ic.getTextBeforeCursor(5000, 0)?.toString()
        if (textBefore.isNullOrBlank()) {
            toneError = getString(R.string.error_no_text)
            return
        }

        isApplyingTone = true
        toneError = null
        clearUndoState()
        dismissSuggestion()

        serviceScope.launch {
            toneRewriteController.rewriteText(textBefore, tone, prefs.apiUrl, prefs.model, prefs.apiToken)
                .fold(
                    onSuccess = { rewritten ->
                        suppressSuggestionTriggerCount += 2
                        if (isRealSelection) {
                            ic.commitText(rewritten, 1)
                        } else {
                            ic.deleteSurroundingText(textBefore.length, 0)
                            ic.commitText(rewritten, 1)
                        }
                        undoState.recordFix(original = textBefore, fixed = rewritten)
                        canUndo = true
                        isTonePanel = false
                    },
                    onFailure = { e ->
                        val gse = e as? GrammarServiceException ?: GrammarServiceException(e.message ?: getString(R.string.grammar_error), e)
                        CrashReporter.recordApiError(gse, CrashReporter.Operation.TONE_REWRITE, prefs.apiUrl)
                        toneError = e.message ?: getString(R.string.grammar_error)
                    },
                )
            isApplyingTone = false
        }
    }

    // --- Translation ---

    fun openTranslatePanel() {
        isTranslatePanel = true
        translateError = null
    }

    fun dismissTranslatePanel() {
        isTranslatePanel = false
        translateError = null
    }

    fun launchTranslation(language: TranslationLanguage) {
        if (isApplyingTranslation || isFixingGrammar || isPrivateField()) return
        val ic = currentInputConnection ?: return

        if (!prefs.isConfigured) {
            translateError = getString(R.string.error_not_configured)
            return
        }

        val selectedText = if (hasSelection) ic.getSelectedText(0)?.toString() else null
        val isRealSelection = !selectedText.isNullOrBlank()
        val textBefore = if (isRealSelection) selectedText!! else ic.getTextBeforeCursor(5000, 0)?.toString()
        if (textBefore.isNullOrBlank()) {
            translateError = getString(R.string.error_no_text)
            return
        }

        isApplyingTranslation = true
        translateError = null
        clearUndoState()
        dismissSuggestion()

        serviceScope.launch {
            translationController.translateText(textBefore, language, prefs.apiUrl, prefs.model, prefs.apiToken)
                .fold(
                    onSuccess = { translated ->
                        suppressSuggestionTriggerCount += 2
                        if (isRealSelection) {
                            ic.commitText(translated, 1)
                        } else {
                            ic.deleteSurroundingText(textBefore.length, 0)
                            ic.commitText(translated, 1)
                        }
                        undoState.recordFix(original = textBefore, fixed = translated)
                        canUndo = true
                        isTranslatePanel = false
                    },
                    onFailure = { e ->
                        val gse = e as? GrammarServiceException ?: GrammarServiceException(e.message ?: getString(R.string.grammar_error), e)
                        CrashReporter.recordApiError(gse, CrashReporter.Operation.TRANSLATION, prefs.apiUrl)
                        translateError = e.message ?: getString(R.string.grammar_error)
                    },
                )
            isApplyingTranslation = false
        }
    }

    // --- Smart Compose ---

    fun triggerSmartCompose() {
        if (!prefs.smartComposeEnabled) return
        if (isSmartComposing || isFixingGrammar || isPrivateField()) return
        val ic = currentInputConnection ?: return
        if (!prefs.isConfigured) return
        val textBefore = ic.getTextBeforeCursor(5000, 0)?.toString()
        if (textBefore.isNullOrBlank()) return

        dismissSuggestion()
        isSmartComposing = true

        serviceScope.launch {
            smartComposeController.compose(textBefore, prefs.apiUrl, prefs.model, prefs.apiToken)
                .fold(
                    onSuccess = { continuation ->
                        suppressSuggestionTriggerCount++
                        ic.commitText(continuation, 1)
                    },
                    onFailure = { /* silently ignore — user can try again */ },
                )
            isSmartComposing = false
        }
    }

    // --- Grammar fix ---

    private fun launchGrammarFix() {
        if (isFixingGrammar || isPrivateField()) return
        val ic = currentInputConnection ?: return

        if (!FeatureGate.isEnabled(FeatureGate.Feature.GRAMMAR_FIX)) {
            grammarError = getString(R.string.error_premium_required)
            return
        }

        if (!prefs.isConfigured) {
            grammarError = getString(R.string.error_not_configured)
            return
        }

        // Quick 1-char lookahead to give immediate error feedback without showing the loading state.
        if (ic.getTextBeforeCursor(1, 0).isNullOrEmpty()) {
            grammarError = getString(R.string.error_no_text)
            return
        }

        isFixingGrammar = true
        grammarError = null
        clearUndoState()
        dismissSuggestion()

        val fixingSelection = hasSelection
        serviceScope.launch {
            grammarFixController.fix(ic, prefs.apiUrl, prefs.model, prefs.apiToken,
                systemPromptSuffix = prefs.grammarInstructionSuffix,
                hasSelection = fixingSelection)
                .fold(
                    onSuccess = { fixResult ->
                        if (fixResult != null) {
                            suppressSuggestionTriggerCount += 2
                            undoState.recordFix(original = fixResult.original, fixed = fixResult.fixed)
                            canUndo = true
                        }
                    },
                    onFailure = { e ->
                        val gse = e as? GrammarServiceException
                            ?: GrammarServiceException(e.message ?: getString(R.string.grammar_error), e)
                        CrashReporter.recordApiError(gse, CrashReporter.Operation.GRAMMAR_FIX, prefs.apiUrl)
                        grammarError = e.message ?: getString(R.string.grammar_error)
                    },
                )
            isFixingGrammar = false
        }
    }

    // --- Voice input ---

    private fun toggleVoiceInput() {
        voiceError = null
        if (isVoiceListening) {
            voiceInputManager.stop()
            return
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            voiceError = "Microphone permission required. Grant it in Settings > Apps > Typlx Keyboard."
            return
        }
        voiceInputManager.start(applicationContext)
    }

    private fun onVoiceResult(text: String) {
        hapticHelper.tap(keyboardView)
        clearUndoState()
        dismissSuggestion()
        currentInputConnection?.commitText(text, 1)
        scheduleAutoSuggest()
    }

    // --- Personal word list persistence ---

    fun reloadPersonalWordList() {
        val json = getSharedPreferences(WORD_LIST_PREFS, Context.MODE_PRIVATE)
            .getString(WORD_LIST_KEY, null) ?: return
        personalWordList.loadFromJson(json)
    }

    // --- Text shortcuts persistence ---

    fun reloadTextShortcuts() {
        val prefs = getSharedPreferences(SHORTCUTS_PREFS, Context.MODE_PRIVATE)
        val stored = prefs.getString(SHORTCUTS_KEY, null)
        if (stored != null) {
            textShortcutsManager.loadFromJson(stored)
        } else {
            TextShortcutsManager.defaults().forEach { textShortcutsManager.add(it.shortcut, it.expansion) }
        }
        shortcuts = textShortcutsManager.getAll()
    }

    private fun insertShortcutExpansion(expansion: String) {
        currentInputConnection?.commitText(expansion, 1)
    }

    private fun openShortcutsManager() {
        val intent = Intent(this, ShortcutsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

}
