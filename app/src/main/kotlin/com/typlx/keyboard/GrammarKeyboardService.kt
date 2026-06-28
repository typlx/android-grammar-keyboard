package com.typlx.keyboard

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

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var prefs: PreferencesManager
    private lateinit var grammarService: GrammarService

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        prefs = PreferencesManager(applicationContext)
        grammarService = GrammarService()
    }

    override fun onCreateInputView(): View {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        return ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            ViewTreeLifecycleOwner.set(this, this@GrammarKeyboardService)
            ViewTreeViewModelStoreOwner.set(this, this@GrammarKeyboardService)
            ViewTreeSavedStateRegistryOwner.set(this, this@GrammarKeyboardService)

            setContent {
                TyplxKeyboardTheme {
                    KeyboardScreen(
                        isFixingGrammar = isFixingGrammar,
                        grammarError = grammarError,
                        onKeyPress = ::commitText,
                        onDelete = ::deleteChar,
                        onFixGrammar = ::launchGrammarFix,
                        onReturn = ::commitReturn,
                        onErrorDismiss = { grammarError = null },
                        onOpenSettings = ::openSettings,
                        onMoveCursorLeft = ::moveCursorLeft,
                        onMoveCursorRight = ::moveCursorRight,
                        onMoveCursorUp = ::moveCursorUp,
                        onMoveCursorDown = ::moveCursorDown,
                        onMoveCursorHome = ::moveCursorHome,
                        onMoveCursorEnd = ::moveCursorEnd,
                        onMoveCursorWordLeft = ::moveCursorWordLeft,
                        onMoveCursorWordRight = ::moveCursorWordRight,
                        onSelectAll = ::selectAll,
                        onCopy = ::copyText,
                        onCut = ::cutText,
                        onPaste = ::pasteText,
                    )
                }
            }
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
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
        currentInputConnection?.commitText(text, 1)
    }

    private fun deleteChar() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    private fun commitReturn() {
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

    // --- Cursor navigation ---

    private fun moveCursorLeft() = sendKey(KeyEvent.KEYCODE_DPAD_LEFT)
    private fun moveCursorRight() = sendKey(KeyEvent.KEYCODE_DPAD_RIGHT)
    private fun moveCursorUp() = sendKey(KeyEvent.KEYCODE_DPAD_UP)
    private fun moveCursorDown() = sendKey(KeyEvent.KEYCODE_DPAD_DOWN)
    private fun moveCursorHome() = sendKey(KeyEvent.KEYCODE_MOVE_HOME)
    private fun moveCursorEnd() = sendKey(KeyEvent.KEYCODE_MOVE_END)
    private fun moveCursorWordLeft() = sendKey(KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.META_CTRL_ON)
    private fun moveCursorWordRight() = sendKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.META_CTRL_ON)

    private fun selectAll() {
        currentInputConnection?.performContextMenuAction(android.R.id.selectAll)
    }

    private fun copyText() {
        currentInputConnection?.performContextMenuAction(android.R.id.copy)
    }

    private fun cutText() {
        currentInputConnection?.performContextMenuAction(android.R.id.cut)
    }

    private fun pasteText() {
        currentInputConnection?.performContextMenuAction(android.R.id.paste)
    }

    private fun sendKey(keyCode: Int, metaState: Int = 0) {
        val ic = currentInputConnection ?: return
        val now = System.currentTimeMillis()
        ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, metaState))
        ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, metaState))
    }

    // --- Settings ---

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
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
            } catch (e: GrammarServiceException) {
                grammarError = e.message ?: getString(R.string.grammar_error)
            } finally {
                isFixingGrammar = false
            }
        }
    }
}
