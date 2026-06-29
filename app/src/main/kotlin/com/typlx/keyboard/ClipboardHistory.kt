package com.typlx.keyboard

/** Manages a fixed-size MRU list of clipboard items. Pure class — no Android dependencies. */
class ClipboardHistory(val maxSize: Int = 10) {

    private val _items = ArrayDeque<String>()

    val items: List<String> get() = _items.toList()

    fun add(text: String) {
        if (text.isBlank()) return
        val truncated = if (text.length > MAX_ITEM_CHARS) text.take(MAX_ITEM_CHARS) else text
        _items.remove(truncated)
        _items.addFirst(truncated)
        while (_items.size > maxSize) _items.removeLast()
    }

    fun clear() {
        _items.clear()
    }

    fun loadFromJson(json: String) {
        _items.clear()
        val trimmed = json.trim()
        if (!trimmed.startsWith('[') || !trimmed.endsWith(']')) return
        val inner = trimmed.substring(1, trimmed.length - 1)
        val pattern = Regex(""""((?:[^"\\]|\\.)*)"""")
        for (match in pattern.findAll(inner)) {
            if (_items.size >= maxSize) break
            val entry = match.groupValues[1]
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
            if (entry.isNotEmpty()) _items.addLast(entry)
        }
    }

    fun toJson(): String = buildString {
        append('[')
        _items.forEachIndexed { i, item ->
            if (i > 0) append(',')
            append('"')
            append(
                item
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t"),
            )
            append('"')
        }
        append(']')
    }

    companion object {
        const val MAX_ITEM_CHARS = 500
    }
}
