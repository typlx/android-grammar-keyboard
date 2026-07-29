# Typlx — Play Store Listing

> **Source:** [TYP-621 app-store-copy](https://github.com/typlx/android-grammar-keyboard) — CMO-approved copy (2026-07-25)

## App Details

- **Package name:** `com.typlx.keyboard`
- **Category:** Productivity / Tools
- **Content rating:** Everyone

---

## Title (max 50 chars)

```
Typlx — Privacy Grammar Keyboard
```

*(33 chars — leaves room for keyword indexing)*

---

## Short Description (max 80 chars)

```
Fix grammar and spelling instantly with any AI model, right from your keyboard.
```

*(79 chars)*

---

## Full Description (max 4000 chars)

```
Typlx Keyboard is an open-source Android keyboard that brings AI-powered grammar correction, tone rewriting, and translation directly to your fingertips — no app-switching required.

GRAMMAR CORRECTION
Tap the "Fix Grammar" button and Typlx silently corrects everything you've typed. Swipe away the suggestion or tap Undo to revert. An inline suggestion strip also proactively offers corrections while you type.

TONE & TRANSLATION
Rewrite your message in a different tone (formal, casual, concise, friendly) or translate it into another language — all without leaving the app you're in.

WORKS WITH ANY OPENAI-COMPATIBLE API
Typlx connects to any server that speaks the OpenAI chat completions format. That includes:
• OpenAI (GPT-4o, GPT-4o mini, etc.)
• Anthropic Claude via proxy
• Groq, Together AI, Mistral, and other fast inference providers
• Local models via Ollama or LM Studio

You bring your own API key and choose your model. No Typlx subscription required.

PRIVACY-FIRST DESIGN
• Your API token is stored with AES-256 encryption on your device.
• Text is sent only to the API endpoint you configure — never to Typlx servers.
• No analytics, no advertising, no user tracking.
• Anonymous crash reports are collected via Firebase Crashlytics to help us fix bugs. You can opt out in Settings.
• 100% open source under the Apache 2.0 license. Read the code yourself.

KEYBOARD FEATURES
• Full QWERTY layout with optional AZERTY, QWERTZ, and Dvorak layouts
• Numeric digit row above the main keyboard (toggleable)
• Number-pad layout activates automatically for number/phone fields
• Long-press keys for accent characters (é, ñ, ü, and more)
• Caps lock (double-tap Shift) and smart auto-capitalisation
• Double-space inserts a period and capitalises the next word
• Haptic feedback on keypress
• Three key-height presets: compact, normal, large
• Text expansion shortcuts — type "omw" and get "On my way!"
• Personal word list — teach the AI to ignore your custom terms

PRODUCTIVITY PANEL
• Clipboard history — paste from the last 10 clipboard entries
• Voice input — dictate text using on-device speech recognition
• Cursor navigation — move by character, word, or line; select, copy, cut, paste
• Emoji keyboard with category tabs and recently-used row
• Undo last grammar fix with a single tap

APPEARANCE
• Four themes: System, Light, Dark, AMOLED black
• Adjustable key corner radius and key opacity
• Full TalkBack / accessibility support

SETUP
1. Open the Typlx Keyboard app.
2. Enter your API URL, model name, and API token in Settings.
3. Tap "Enable Typlx Keyboard in System Settings" and follow the prompts.
4. Switch to Typlx Keyboard in any text field and tap the toolbar icon.

OPEN SOURCE
Source code and full build instructions are available at:
https://github.com/typlx/android-grammar-keyboard

Contributions, bug reports, and feature requests are welcome.
```

*(Character count: approximately 2,350 — well within the 4,000-char limit)*

---

## Screenshot Overlay Copy (3–5 screens)

### Screen 1 — Core value proposition
- **Headline:** `AI grammar, right from your keyboard`
- **Sub:** `Fix, rephrase, and translate without leaving the app.`
- *Visual:* Typlx keyboard visible with a correction suggestion in the suggestion bar

### Screen 2 — Privacy proof
- **Headline:** `Your API. Your data.`
- **Sub:** `Text goes to your endpoint — never Typlx servers. 100% open source.`
- *Visual:* Settings or privacy screen, or abstract shield/device visual

### Screen 3 — Personal dictionary
- **Headline:** `Teach it your vocabulary`
- **Sub:** `Add words once. Never flagged again.`
- *Visual:* Personal dictionary UI with words being added

### Screen 4 — Works everywhere
- **Headline:** `Grammar in every app`
- **Sub:** `Email, messages, docs — Typlx follows you.`
- *Visual:* App switcher or multi-app view with Typlx suggestion bar visible

### Screen 5 — Open source (developer-oriented audiences)
- **Headline:** `Open source. Inspect the code.`
- **Sub:** `github.com/typlx — nothing hidden.`
- *Visual:* GitHub repo screenshot or code snippet with star count

---

## Graphic Assets Required (before submission)

| Asset | Size | Notes |
|---|---|---|
| Hi-res icon | 512 × 512 px PNG | Export from vector at high density |
| Feature graphic | 1024 × 500 px | Required for Play Store listing header |
| Phone screenshots | min 2, up to 8 | 1080 × 1920 px or 9:16 ratio recommended |
| Tablet screenshots (optional) | — | Improves tablet store presence |

---

## Contact & Support

- **Website / Support URL:** https://typlx.com
- **Privacy Policy URL:** https://typlx.com/privacy *(required before submission)*
- **Email:** *(required for Play Store developer account)*

---

## Release Notes (v1.0.0)

See RELEASE_NOTES.md for the full changelog.

Key highlights for the initial release:
- Grammar correction powered by your chosen AI model
- Auto-suggest with inline diff display
- Tone rewriter and in-keyboard translation
- Voice input, clipboard history, emoji keyboard
- Multi-language keyboard layouts
- Full theme customisation
- Open-source, bring-your-own-API model

---

## How to Apply This to Google Play Console

**Manual (Play Console UI):**
1. Log in to [Google Play Console](https://play.google.com/console)
2. Select Typlx app → Store presence → Main store listing
3. Paste Title, Short description, Full description from the fields above
4. Upload screenshots with the overlay text from "Screenshot Overlay Copy"
5. Save and submit for review

**Automated (fastlane supply):**
Metadata files are in `fastlane/metadata/android/en-US/` for use with `fastlane supply`.
Requires `SUPPLY_JSON_KEY` or `SUPPLY_JSON_KEY_DATA` service account credential.

```bash
bundle exec fastlane supply --skip_upload_apk --skip_upload_aab --skip_upload_images
```
