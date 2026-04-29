# Typlx Grammar Keyboard

Android custom keyboard (IME) with a grammar-fix button. Uses configurable OpenAI-compatible LLM APIs for grammar and spelling correction. Part of the [Typlx](https://typlx.com) open-source grammar-checking suite.

## Features

- **Grammar Fix Button** -- tap to send the current input field text to an LLM for grammar and spelling correction
- **Configurable API** -- works with any OpenAI-compatible chat completions endpoint
- **Secure Storage** -- API tokens stored using Android EncryptedSharedPreferences
- **Material3 Settings** -- clean Jetpack Compose settings screen for API configuration

## Architecture

```
app/src/main/kotlin/com/typlx/keyboard/
├── GrammarKeyboardService.kt  — InputMethodService with grammar-fix toolbar
├── GrammarService.kt          — OkHttp client for /chat/completions
├── SettingsActivity.kt        — Jetpack Compose settings screen (launcher activity)
└── PreferencesManager.kt      — SharedPreferences + EncryptedSharedPreferences wrapper
```

### How it works

1. User enables the Typlx keyboard in Android Settings.
2. The keyboard displays a **Fix Grammar** toolbar button.
3. On tap, `GrammarKeyboardService` reads the full text from the focused input field via `InputConnection`.
4. `GrammarService` POSTs to `{apiUrl}/chat/completions` with `temperature=0.3` and a 30 s timeout.
5. The original text is selected and replaced with the corrected version.

### API Contract

```
POST {apiUrl}/chat/completions
Authorization: Bearer {token}
Content-Type: application/json

{
  "model": "{model}",
  "messages": [
    {"role": "system", "content": "Fix grammar and spelling in the following text. Return only the corrected text, nothing else. Preserve the original language, tone, and formatting."},
    {"role": "user", "content": "{text}"}
  ],
  "temperature": 0.3
}
```

Compatible with OpenAI, Ollama (`http://10.0.2.2:11434`), LM Studio, and any `/v1/chat/completions` server.

## Setup

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 11+
- Android SDK 34

### Build

```bash
# First time: generate the Gradle wrapper binary
gradle wrapper --gradle-version 8.7

./gradlew assembleDebug
```

### Install

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Enable the Keyboard

1. Launch the **Typlx Keyboard** app — it opens the settings screen directly.
2. Tap **Enable Typlx Keyboard in System Settings** and toggle it on.
3. Open any text field, tap the globe/keyboard-switcher icon, and select *Typlx Keyboard*.

### Configure API

In the settings screen (or via Settings → Language & Input → Typlx Keyboard):

| Field | Default |
|-------|---------|
| API URL | `https://api.openai.com` |
| Model | `gpt-4o-mini` |
| API Token | *(empty — required)* |

Tokens are stored with `EncryptedSharedPreferences` backed by the Android Keystore.

## Tech Stack

- Kotlin 1.9 · minSdk 24 · targetSdk 34
- OkHttp 4.12 for HTTP
- Jetpack Compose + Material 3 for all UI
- AndroidX Security Crypto for credential encryption
- Kotlin Coroutines + `SupervisorJob` scoped to the IME service

## License

See [LICENSE](LICENSE) for details.
