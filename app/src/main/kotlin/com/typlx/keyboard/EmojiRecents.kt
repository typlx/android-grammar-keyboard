package com.typlx.keyboard

/** Manages a fixed-size MRU list of recently used emoji. Pure class — no Android dependencies. */
class EmojiRecents(val maxSize: Int = 32) {

    private val _recents = ArrayDeque<String>()

    val recents: List<String> get() = _recents.toList()

    fun add(emoji: String) {
        _recents.remove(emoji)
        _recents.addFirst(emoji)
        while (_recents.size > maxSize) _recents.removeLast()
    }

    fun loadFromJson(json: String) {
        _recents.clear()
        val trimmed = json.trim()
        if (!trimmed.startsWith('[') || !trimmed.endsWith(']')) return
        val inner = trimmed.substring(1, trimmed.length - 1)
        // Match "..." tokens, handling basic escape sequences
        val pattern = Regex(""""((?:[^"\\]|\\.)*)"""")
        for (match in pattern.findAll(inner)) {
            if (_recents.size >= maxSize) break
            val entry = match.groupValues[1]
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
            if (entry.isNotEmpty()) _recents.addLast(entry)
        }
    }

    fun toJson(): String = buildString {
        append('[')
        _recents.forEachIndexed { i, emoji ->
            if (i > 0) append(',')
            append('"')
            append(emoji.replace("\\", "\\\\").replace("\"", "\\\""))
            append('"')
        }
        append(']')
    }
}
