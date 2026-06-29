package com.typlx.keyboard

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

sealed class VoiceInputState {
    object Idle : VoiceInputState()
    object Listening : VoiceInputState()
    data class Partial(val text: String) : VoiceInputState()
}

internal fun voiceErrorMessage(errorCode: Int): String? = when (errorCode) {
    SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out"
    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network error"
    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy"
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
    else -> null
}

class VoiceInputManager {

    var onResult: (String) -> Unit = {}
    var onStateChange: (VoiceInputState) -> Unit = {}
    var onError: (String) -> Unit = {}

    private var recognizer: SpeechRecognizer? = null
    private var _state: VoiceInputState = VoiceInputState.Idle

    val state: VoiceInputState get() = _state

    fun isAvailable(context: Context): Boolean =
        SpeechRecognizer.isRecognitionAvailable(context)

    fun start(context: Context) {
        if (_state is VoiceInputState.Listening) return

        if (!isAvailable(context)) {
            onError("Speech recognition not available on this device")
            return
        }

        val sr = buildRecognizer(context)
        recognizer?.destroy()
        recognizer = sr

        sr.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = setState(VoiceInputState.Listening)
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                setState(VoiceInputState.Idle)
                voiceErrorMessage(error)?.let { onError(it) }
            }

            override fun onResults(results: Bundle?) {
                setState(VoiceInputState.Idle)
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (!text.isNullOrBlank()) onResult(text)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val text = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (!text.isNullOrBlank()) setState(VoiceInputState.Partial(text))
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        setState(VoiceInputState.Listening)
        sr.startListening(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
        )
    }

    fun stop() {
        recognizer?.stopListening()
        setState(VoiceInputState.Idle)
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
        setState(VoiceInputState.Idle)
    }

    private fun setState(newState: VoiceInputState) {
        _state = newState
        onStateChange(newState)
    }

    private fun buildRecognizer(context: Context): SpeechRecognizer =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
        ) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }
}
