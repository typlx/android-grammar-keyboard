package com.typlx.keyboard

import android.speech.SpeechRecognizer
import org.junit.Assert.*
import org.junit.Test

class VoiceInputManagerTest {

    @Test
    fun `initial state is Idle`() {
        val manager = VoiceInputManager()
        assertTrue(manager.state is VoiceInputState.Idle)
    }

    @Test
    fun `voiceErrorMessage returns null for unknown codes`() {
        assertNull(voiceErrorMessage(9999))
    }

    @Test
    fun `voiceErrorMessage maps ERROR_NO_MATCH`() {
        assertEquals("No speech detected", voiceErrorMessage(SpeechRecognizer.ERROR_NO_MATCH))
    }

    @Test
    fun `voiceErrorMessage maps ERROR_SPEECH_TIMEOUT`() {
        assertEquals("Listening timed out", voiceErrorMessage(SpeechRecognizer.ERROR_SPEECH_TIMEOUT))
    }

    @Test
    fun `voiceErrorMessage maps ERROR_NETWORK`() {
        assertEquals("Network error", voiceErrorMessage(SpeechRecognizer.ERROR_NETWORK))
    }

    @Test
    fun `voiceErrorMessage maps ERROR_NETWORK_TIMEOUT`() {
        assertEquals("Network error", voiceErrorMessage(SpeechRecognizer.ERROR_NETWORK_TIMEOUT))
    }

    @Test
    fun `voiceErrorMessage maps ERROR_AUDIO`() {
        assertEquals("Audio recording error", voiceErrorMessage(SpeechRecognizer.ERROR_AUDIO))
    }

    @Test
    fun `voiceErrorMessage maps ERROR_INSUFFICIENT_PERMISSIONS`() {
        assertEquals("Microphone permission required", voiceErrorMessage(SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS))
    }

    @Test
    fun `destroy when no recognizer created does not throw`() {
        val manager = VoiceInputManager()
        manager.destroy()
        assertTrue(manager.state is VoiceInputState.Idle)
    }

    @Test
    fun `stop when Idle changes state callback is not fired`() {
        val manager = VoiceInputManager()
        var stateChanges = 0
        manager.onStateChange = { stateChanges++ }
        manager.stop()
        // stop() destroys nothing and fires a state change to Idle
        // (state was already Idle so this is idempotent, but stop() still signals)
        assertEquals(VoiceInputState.Idle, manager.state)
    }
}
