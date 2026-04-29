package com.typlx.keyboard

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Custom InputMethodService that provides a grammar-fix toolbar.
 *
 * The keyboard view displays a "Fix Grammar" button. When tapped, it reads the full text
 * from the currently focused input field, sends it to a configurable LLM API for correction,
 * and replaces the field content with the corrected text.
 */
class GrammarKeyboardService : InputMethodService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val grammarService = GrammarService()
    private lateinit var prefsManager: PreferencesManager

    private var fixButton: Button? = null
    private var settingsButton: Button? = null
    private var statusText: TextView? = null
    private var isProcessing = false

    override fun onCreate() {
        super.onCreate()
        prefsManager = PreferencesManager(this)
    }

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.keyboard_view, null)

        fixButton = view.findViewById(R.id.fixGrammarButton)
        settingsButton = view.findViewById(R.id.settingsButton)
        statusText = view.findViewById(R.id.statusText)

        fixButton?.setOnClickListener { onFixGrammarClicked() }
        settingsButton?.setOnClickListener { openSettings() }

        return view
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun onFixGrammarClicked() {
        if (isProcessing) return

        if (!prefsManager.isConfigured) {
            showToast(getString(R.string.error_not_configured))
            openSettings()
            return
        }

        val ic = currentInputConnection ?: return
        val extracted = ic.getExtractedText(android.view.inputmethod.ExtractedTextRequest(), 0)
        val text = extracted?.text?.toString()

        if (text.isNullOrBlank()) {
            showToast(getString(R.string.error_no_text))
            return
        }

        setLoadingState(true)

        serviceScope.launch {
            try {
                val corrected = grammarService.fixGrammar(
                    apiUrl = prefsManager.apiUrl,
                    model = prefsManager.model,
                    token = prefsManager.apiToken,
                    text = text
                )

                // Replace the entire content of the input field
                ic.beginBatchEdit()
                ic.performContextMenuAction(android.R.id.selectAll)
                ic.commitText(corrected, 1)
                ic.endBatchEdit()

                statusText?.text = getString(R.string.grammar_fixed)
            } catch (e: GrammarServiceException) {
                val errorMsg = e.message ?: getString(R.string.grammar_error)
                statusText?.text = errorMsg
                showToast(errorMsg)
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun setLoadingState(loading: Boolean) {
        isProcessing = loading
        fixButton?.isEnabled = !loading
        fixButton?.text = getString(
            if (loading) R.string.fixing_grammar else R.string.fix_grammar
        )
        if (loading) {
            statusText?.text = getString(R.string.fixing_grammar)
        }
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
