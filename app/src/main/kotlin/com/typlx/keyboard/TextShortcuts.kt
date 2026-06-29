package com.typlx.keyboard

/** A single text shortcut mapping. Both fields are stored as-entered (case preserved for expansion). */
data class TextShortcut(val shortcut: String, val expansion: String)

/**
 * Manages user-defined text shortcuts. Shortcuts are matched case-insensitively; expansions are
 * inserted verbatim. Pure class — no Android dependencies; persistence handled by the caller.
 */
class TextShortcutsManager(val maxSize: Int = 200) {

    private val map = mutableMapOf<String, String>()

    val size: Int get() = map.size

    /**
     * Adds a shortcut. [shortcut] is stored lowercased; [expansion] is stored as-is.
     * Returns false if either field is blank, shortcut already exists, or at capacity.
     */
    fun add(shortcut: String, expansion: String): Boolean {
        val key = shortcut.trim().lowercase()
        val value = expansion.trim()
        if (key.isEmpty() || value.isEmpty() || map.containsKey(key) || map.size >= maxSize) return false
        map[key] = value
        return true
    }

    /** Removes a shortcut by key (case-insensitive). No-op if not present. */
    fun remove(shortcut: String) {
        map.remove(shortcut.trim().lowercase())
    }

    /** Returns the expansion for [word] (case-insensitive), or null if no match. */
    fun expand(word: String): String? = map[word.trim().lowercase()]

    /** Returns all shortcuts sorted by shortcut key. */
    fun getAll(): List<TextShortcut> = map.entries
        .sortedBy { it.key }
        .map { TextShortcut(it.key, it.value) }

    fun loadFromJson(json: String) {
        map.clear()
        val trimmed = json.trim()
        if (!trimmed.startsWith('[') || !trimmed.endsWith(']')) return
        val inner = trimmed.substring(1, trimmed.length - 1).trim()
        if (inner.isEmpty()) return
        // Parse array of {"s":"...","e":"..."} objects using simple regex (no org.json dependency).
        val objPattern = Regex("""\{[^}]*\}""")
        val strPattern = Regex(""""([se])":"((?:[^"\\]|\\.)*)"""")
        for (obj in objPattern.findAll(inner)) {
            if (map.size >= maxSize) break
            val fields = strPattern.findAll(obj.value).associate {
                it.groupValues[1] to it.groupValues[2]
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\/", "/")
            }
            val s = fields["s"]?.trim()?.lowercase()
            val e = fields["e"]?.trim()
            if (!s.isNullOrEmpty() && !e.isNullOrEmpty()) map[s] = e
        }
    }

    fun toJson(): String = buildString {
        append('[')
        val entries = getAll()
        entries.forEachIndexed { i, sc ->
            if (i > 0) append(',')
            append("{\"s\":\"")
            append(sc.shortcut.replace("\\", "\\\\").replace("\"", "\\\""))
            append("\",\"e\":\"")
            append(sc.expansion.replace("\\", "\\\\").replace("\"", "\\\""))
            append("\"}")
        }
        append(']')
    }

    companion object {
        /** Sensible defaults loaded when the user has never configured shortcuts. */
        fun defaults(): List<TextShortcut> = listOf(
            TextShortcut("omw", "On my way!"),
            TextShortcut("ttyl", "Talk to you later!"),
            TextShortcut("brb", "Be right back!"),
            TextShortcut("thx", "Thanks!"),
            TextShortcut("imo", "In my opinion,"),
            TextShortcut("afaik", "As far as I know,"),
            TextShortcut("fyi", "For your information,"),
        )
    }
}
