# Release Notes

## v1.0.0 — Initial Release

### What's New

**AI-powered writing assistance, right from your keyboard.**

Typlx Keyboard brings grammar correction, tone rewriting, and translation into a full-featured Android keyboard. Bring your own API key — works with OpenAI, Groq, Anthropic (via proxy), Mistral, Ollama, and any OpenAI-compatible server.

### Keyboard

- Full QWERTY layout
- Optional AZERTY, QWERTZ, and Dvorak layouts
- Toggleable number row above the main keyboard
- Number-pad layout activates automatically for numeric and phone fields
- Long-press for accent characters (é, ñ, ü, ç, and more)
- Auto-capitalisation after sentence-ending punctuation
- Double-space inserts a period and capitalises the next word
- Caps lock via double-tap on Shift
- Long-press Backspace for fast delete and word delete
- Haptic feedback (on/off toggle)
- Three key-height presets: compact, normal, large
- Full TalkBack / accessibility support

### AI Features

- **One-tap grammar fix** — corrects everything you've typed in the current field
- **Proactive auto-suggest** — the suggestion strip highlights grammar issues as you type; tap to accept, swipe to dismiss
- **Tone rewriter** — rewrite your text in a different style: formal, casual, concise, enthusiastic, or empathetic
- **In-keyboard translation** — translate to Spanish, French, German, Japanese, Portuguese, Italian, Chinese, Arabic, Hindi, or Korean
- **Voice input** — dictate with on-device speech recognition; grammar fix can run automatically on the result
- **Undo** — revert the last grammar fix, tone rewrite, or translation with a single tap

### Productivity

- **Clipboard history** — paste from the last 10 clipboard entries without switching apps
- **Text shortcuts** — type abbreviations and expand them automatically (e.g. "omw" → "On my way!")
- **Personal word list** — add words the AI should never "correct" (names, jargon, abbreviations)
- **Cursor navigation panel** — move by character, word, or line; select all, copy, cut, paste

### Appearance

- Four themes: System default, Light, Dark, AMOLED black
- Adjustable key corner radius (0–16 dp)
- Adjustable key opacity (10–100%)
- Emoji keyboard with category tabs and recently-used row

### Privacy & Security

- API token stored with AES-256-GCM encryption via Android Jetpack Security Crypto
- Text sent only to the API endpoint you configure — never to Typlx servers
- No analytics, no advertising identifiers, no user tracking
- Anonymous crash reports collected via Firebase Crashlytics to help fix bugs (opt-out in Settings)
- 100% open source — Apache 2.0

### Build Info

- Min SDK: 26 (Android 8.0 Oreo)
- Target SDK: 35 (Android 15)
- License: Apache 2.0
- Source: https://github.com/typlx/android-grammar-keyboard
