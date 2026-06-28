package com.typlx.keyboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.inputmethodservice.InputMethodService
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import com.typlx.keyboard.ui.KeyboardScreen
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

    private val undoState = GrammarUndoState()
    private val emojiRecentsMgr = EmojiRecents()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var prefs: PreferencesManager
    private lateinit var grammarService: GrammarService
    private lateinit var hapticHelper: HapticHelper
    private var keyboardView: View? = null

    private var suggestionDebounceJob: Job? = null
    // Counts how many upcoming onUpdateSelection callbacks to suppress (caused by our own
    // deleteSurroundingText / commitText calls when applying a suggestion).
    private var suppressSuggestionTriggerCount = 0

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        prefs = PreferencesManager(applicationContext)
        grammarService = GrammarService()
        hapticHelper = HapticHelper { prefs.hapticFeedbackEnabled }
        loadEmojiRecents()
    }

    override fun onCreateInputView(): View {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        return ComposeView(this).also { keyboardView = it }.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            ViewTreeLifecycleOwner.set(this, this@GrammarKeyboardService)
            ViewTreeViewModelStoreOwner.set(this, this@GrammarKeyboardService)
            ViewTreeSavedStateRegistryOwner.set(this, this@GrammarKeyboardService)

            setContent {
                TyplxKeyboardTheme {
                    KeyboardScreen(
                        isFixingGrammar = isFixingGrammar,
                        grammarError = grammarError,
                        canUndo = canUndo,
                        returnKeyDescription = returnKeyDescription,
                        emojiRecents = emojiRecents,
                        suggestionState = suggestionState,
                        onKeyPress = ::commitText,
                        onDelete = ::deleteChar,
                        onDeleteWord = ::deleteWord,
                        onFixGrammar = ::launchGrammarFix,
                        onReturn = ::commitReturn,
                        onErrorDismiss = { grammarError = null },
                        onUndoGrammarFix = ::undoGrammarFix,
                        onEmojiPress = ::commitEmoji,
                        onAcceptSuggestion = ::acceptSuggestion,
                        onDismissSuggestion = ::dismissSuggestion,
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
                        onOpenSettings = ::openSettings,
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
        if (suppressSuggestionTriggerCount > 0) {
            suppressSuggestionTriggerCount--
            return
        }
        scheduleAutoSuggest()
    }

    private fun scheduleAutoSuggest() {
        if (!prefs.autoSuggestEnabled || !prefs.isConfigured || isFixingGrammar) return
        suggestionDebounceJob?.cancel()
        suggestionDebounceJob = serviceScope.launch {
            delay(1500L)
            runAutoSuggest()
        }
    }

    private suspend fun runAutoSuggest() {
        val ic = currentInputConnection ?: return
        val text = ic.getTextBeforeCursor(5000, 0)?.toString()
        if (text.isNullOrBlank()) {
            suggestionState = SuggestionState.Idle
            return
        }
        suggestionState = SuggestionState.Loading
        try {
            val fixed = grammarService.fixGrammar(
                apiUrl = prefs.apiUrl,
                model = prefs.model,
                token = prefs.apiToken,
                text = text,
            )
            suggestionState = if (fixed != text) SuggestionState.Available(text, fixed)
                             else SuggestionState.Idle
        } catch (_: GrammarServiceException) {
            suggestionState = SuggestionState.Idle
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
        suggestionState = SuggestionState.Idle
        suggestionDebounceJob?.cancel()
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        clearUndoState()
        returnKeyDescription = when (info?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)) {
            EditorInfo.IME_ACTION_SEARCH -> "Search"
            EditorInfo.IME_ACTION_SEND -> "Send"
            EditorInfo.IME_ACTION_GO -> "Go"
            EditorInfo.IME_ACTION_DONE -> "Done"
            EditorInfo.IME_ACTION_NEXT -> "Next field"
            EditorInfo.IME_ACTION_PREVIOUS -> "Previous field"
            else -> "Return"
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        keyboardView = null
        suggestionDebounceJob?.cancel()
        suggestionState = SuggestionState.Idle
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        super.onFinishInputView(finishingInput)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        vmStore.clear()
        super.onDestroy()
    }

    // --- Input helpers ---

    private fun commitText(text: String) {
        hapticHelper.tap(keyboardView)
        clearUndoState()
        currentInputConnection?.commitText(text, 1)
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

    private fun loadEmojiRecents() {
        val json = getSharedPreferences(EMOJI_PREFS, Context.MODE_PRIVATE)
            .getString(EMOJI_RECENTS_KEY, null) ?: return
        emojiRecentsMgr.loadFromJson(json)
        emojiRecents = emojiRecentsMgr.recents
    }

    private fun saveEmojiRecents() {
        getSharedPreferences(EMOJI_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(EMOJI_RECENTS_KEY, emojiRecentsMgr.toJson())
            .apply()
    }

    private fun clearUndoState() {
        undoState.clear()
        canUndo = false
    }

    fun undoGrammarFix() {
        val (original, fixed) = undoState.consume() ?: return
        canUndo = false
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
    }

    // --- Grammar fix ---

    private fun launchGrammarFix() {
        if (isFixingGrammar) return
        val ic = currentInputConnection ?: return

        if (!FeatureGate.isEnabled(FeatureGate.Feature.GRAMMAR_FIX)) {
            grammarError = getString(R.string.error_premium_required)
            return
        }

        if (!prefs.isConfigured) {
            grammarError = getString(R.string.error_not_configured)
            return
        }

        val textBefore = ic.getTextBeforeCursor(5000, 0)?.toString()
        if (textBefore.isNullOrBlank()) {
            grammarError = getString(R.string.error_no_text)
            return
        }

        isFixingGrammar = true
        grammarError = null
        clearUndoState()
        dismissSuggestion()

        serviceScope.launch {
            try {
                val fixed = grammarService.fixGrammar(
                    apiUrl = prefs.apiUrl,
                    model = prefs.model,
                    token = prefs.apiToken,
                    text = textBefore,
                )
                ic.deleteSurroundingText(textBefore.length, 0)
                ic.commitText(fixed, 1)
                undoState.recordFix(original = textBefore, fixed = fixed)
                canUndo = true
            } catch (e: GrammarServiceException) {
                grammarError = e.message ?: getString(R.string.grammar_error)
            } finally {
                isFixingGrammar = false
            }
        }
    }
}
