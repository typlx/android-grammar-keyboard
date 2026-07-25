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
Grammar checking that stays on your device. Private. Offline. Open source.
```

*(74 chars)*

---

## Full Description (max 4000 chars)

```
Typlx is the grammar keyboard that never uploads your words.

While most grammar tools send your text to a cloud server, Typlx runs entirely on your device. Type a message, draft an email, post on social media — your words stay on your phone.

🔒 PRIVATE BY DESIGN

Typlx's grammar engine processes text locally. No internet connection required. No data sent to servers. No account needed. You can verify this: Typlx is fully open source.

✏️ GRAMMAR CHECKING EVERYWHERE

Install Typlx as your default keyboard and grammar checking follows you across all apps:
- WhatsApp, Telegram, Signal
- Gmail, Outlook, ProtonMail
- Twitter/X, Reddit, LinkedIn
- Google Docs (in-browser editing)
- Notes, reminders, any app where you type

📖 WHAT TYPLX CATCHES

- Subject-verb agreement errors
- Wrong verb tense
- Article errors (a/an/the)
- Common spelling mistakes
- Comma splices and run-on sentences
- Incorrect word choice (affect/effect, their/they're)

📚 PERSONAL DICTIONARY

Add words that Typlx should always accept — technical terms, names, abbreviations unique to your work. Your dictionary stays local, never uploaded.

⚡ WORKS OFFLINE

No Wi-Fi? No problem. Typlx grammar checking works without any internet connection. The engine runs locally on your device hardware.

🌍 OPEN SOURCE

Typlx is 100% open source. Review the grammar engine, keyboard code, and data handling at github.com/typlx. Trust the code, not just the promise.

🆓 FREE FOREVER

Core grammar checking is free with no time limit. Advanced features (style suggestions, document-level analysis) coming in a future Pro tier.

INSTALLATION

1. Download Typlx
2. Open Settings → System → Language & Input → On-screen keyboard → Manage keyboards
3. Enable Typlx
4. Select Typlx as your default keyboard (optional) or switch when needed
5. Start typing — corrections appear in the suggestion bar

No account. No subscription. No data collection.

---

Why developers built Typlx: We wanted grammar checking that worked without a privacy trade-off. The tools that exist either upload everything to a cloud or barely work offline. We built the local grammar engine we wanted to use ourselves.

Review the code. File issues. Contribute. It's yours.
```

*(Character count: approximately 1,900 — well within the 4,000-char limit)*

---

## Screenshot Overlay Copy (3–5 screens)

### Screen 1 — Core value proposition
- **Headline:** `Grammar that stays yours`
- **Sub:** `Corrects as you type. Never leaves your device.`
- *Visual:* Typlx keyboard visible with a correction suggestion in the suggestion bar

### Screen 2 — Privacy proof
- **Headline:** `Zero cloud. Zero upload.`
- **Sub:** `Everything runs locally — verified open source.`
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
