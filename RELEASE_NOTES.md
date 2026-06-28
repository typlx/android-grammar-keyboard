# Release Notes

## v1.0.0 — 2026-06-15

Initial public release of Typlx Keyboard.

### Features

- **AI grammar & spelling correction** — tap the "Fix Grammar" button to instantly correct up to 5,000 characters of typed text using any OpenAI-compatible model.
- **Bring-your-own API** — works with OpenAI, Groq, Together AI, Mistral, Ollama, LM Studio, and any server supporting the `/v1/chat/completions` endpoint.
- **Full QWERTY keyboard** — letters, numbers, symbols, caps-lock, shift-once, and backspace.
- **Encrypted credential storage** — API token is stored with AES-256-GCM on-device via Android Jetpack Security Crypto. Never stored in plaintext.
- **Privacy-first** — no analytics, no telemetry, no Typlx servers. Text goes only to the API endpoint you configure.
- **Light & dark theme** — follows the system theme automatically.
- **Settings screen** — configure API URL, model name, and API token; launch the IME activation screen directly from the app.
- **Graceful error handling**:
  - "Not configured" prompt when API credentials are missing.
  - "No text found" prompt when the Fix Grammar button is tapped on an empty field.
  - Specific messages for no internet connection, request timeout, invalid API token (401/403), and rate limit errors (429).
- **About section** — version and license info visible in Settings.

### Known Limitations

- English keyboard only (QWERTY). Additional language layouts are planned.
- No swipe/gesture typing in this release.
- Requires Android 7.0 (API 24) or higher.
- Release APK/AAB requires a developer-supplied signing keystore (see `play-store-listing.md`).

### Build Info

- Min SDK: 24 (Android 7.0 Nougat)
- Target SDK: 34 (Android 14)
- Version code: 1
- Version name: 1.0.0
- License: Apache 2.0
