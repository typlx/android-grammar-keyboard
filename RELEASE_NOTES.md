# Release Notes

## v1.0.0 — Initial Release

### What's New

**AI-powered writing assistance, right from your keyboard.**

Typlx Keyboard brings grammar correction, tone rewriting, and translation into a full-featured Android keyboard. Bring your own API key — works with OpenAI, Groq, Anthropic (via proxy), Mistral, Ollama, and any OpenAI-compatible server.

### Keyboard

- Full QWERTY layout with swipe/gesture typing
- Swipe trail animation drawn on keyboard while you gesture
- Optional AZERTY, QWERTZ, and Dvorak layouts
- Toggleable number row above the main keyboard
- Number-pad layout activates automatically for numeric and phone fields
- Long-press for accent characters (é, ñ, ü, ç, and more)
- Auto-capitalisation after sentence-ending punctuation
- Double-space inserts a period and capitalises the next word
- Caps lock via double-tap on Shift
- Long-press Backspace for fast delete and word delete
- Haptic feedback (on/off toggle)
- Key click sounds (on/off toggle)
- Continuous key-height slider (36–64 dp)
- Landscape mode uses reduced key height automatically
- Space bar long-press and drag to move the cursor
- One-handed mode — shift the keyboard left or right
- Full TalkBack / accessibility support

### AI Features

- **One-tap grammar fix** — corrects everything you've typed in the current field
- **Proactive auto-suggest** — the suggestion strip highlights grammar issues as you type; tap to accept, swipe to dismiss
- **Multi-language grammar checking** — corrections in English, Spanish, French, German, Portuguese, and Ukrainian; keyboard auto-detects the language you're writing in
- **Grammar rule preferences** — choose which grammar rules to apply (punctuation, capitalisation, verb agreement, etc.)
- **Smart Compose** — LLM-powered sentence continuation; get suggestions for finishing your current line
- **Tone rewriter** — rewrite in formal, casual, concise, enthusiastic, empathetic, rephrased, simplified, or expanded style
- **In-keyboard translation** — translate to Spanish, French, German, Japanese, Portuguese, Italian, Chinese, Arabic, Hindi, Korean, Indonesian, Vietnamese, Thai, or Ukrainian
- **Selection-aware actions** — select part of your text to fix grammar, rewrite tone, or translate only that selection
- **Voice input** — dictate with on-device speech recognition; grammar fix can run automatically on the result
- **Undo** — revert the last grammar fix, tone rewrite, or translation with a single tap
- **Retry logic** — transient API failures are retried with exponential backoff

### API & Configuration

- **One-tap API provider presets** — pre-configured base URLs for OpenAI, Groq, Mistral, Anthropic (via proxy), Together AI, and Ollama (local)
- **Custom writing style** — configure the system-prompt instruction text to guide the AI's grammar and style preferences
- **Test connection button** — verify your API key and endpoint before enabling
- **Password field protection** — AI features are automatically disabled in password, PIN, phone, and email fields

### Suggestions & Predictions

- **Word prediction** — next-word predictions in the suggestion strip from a common-word list plus your personal dictionary
- **Emoji suggestions** — relevant emoji chips appear in the suggestion strip as you type
- **Local autocorrect** — common typos corrected automatically on space press
- **Inline diff display** — grammar suggestions show the exact change before you accept

### Productivity

- **Clipboard history** — paste from the last 10 clipboard entries without switching apps
- **Clipboard smart-paste chip** — OTP codes and URLs detected and offered for instant paste
- **Text shortcuts** — type abbreviations and expand them automatically (e.g. "omw" → "On my way!")
- **Personal word list** — add words the AI should never "correct" (names, jargon, abbreviations); long-press any word to add it from within the keyboard
- **Custom dictionary management** — search, edit, export, and import your word list from Settings
- **Correction stats** — Settings shows how many grammar corrections you've accepted
- **Cursor navigation panel** — move by character, word, or line; select all, copy, cut, paste
- **App shortcuts** — long-press the Typlx launcher icon for quick shortcuts to Settings and the personal word list

### System Integration

- **Quick Settings tile** — toggle AI grammar assist on or off directly from the Android notification shade
- **IME switch button** — globe icon to switch between installed keyboards without leaving the app
- **Battery optimization exemption** — prompted during onboarding to keep the keyboard responsive in background
- **Auto Backup rules** — settings backed up; API token and clipboard history excluded for privacy

### Appearance

- Four themes: System default, Light, Dark, AMOLED black
- High Contrast theme for accessibility
- Custom color scheme (premium) with full primary/background/accent control
- Preview theme before applying
- Adjustable key corner radius (0–16 dp)
- Adjustable key opacity (10–100%)
- Emoji keyboard with category tabs and recently-used row

### Privacy & Security

- API token stored with AES-256-GCM encryption via Android Jetpack Security Crypto
- Text sent only to the API endpoint you configure — never to Typlx servers
- AI features disabled automatically in password, phone, number, datetime, and email fields
- No analytics, no advertising identifiers, no user tracking
- Anonymous crash reports collected via Firebase Crashlytics to help fix bugs (opt-out in Settings)
- 100% open source — Apache 2.0

### Build Info

- Min SDK: 26 (Android 8.0 Oreo)
- Target SDK: 35 (Android 15)
- License: Apache 2.0
- Source: https://github.com/typlx/android-grammar-keyboard
