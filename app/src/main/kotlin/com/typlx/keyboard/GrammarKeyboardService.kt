package com.typlx.keyboard

import android.os.Bundle
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

    // --- Grammar fix ---

    private fun launchGrammarFix() {
        if (isFixingGrammar) return
        val ic = currentInputConnection ?: return

        val textBefore = ic.getTextBeforeCursor(5000, 0)?.toString()
        if (textBefore.isNullOrBlank()) return

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
