package com.typlx.keyboard

/** Tracks grammar correction usage counts. Pure class — no Android dependencies. */
class CorrectionStats {

    private var _totalCorrectionsApplied = 0
    private var _totalSessionsWithCorrections = 0
    private var correctionsThisSession = 0

    val totalCorrectionsApplied: Int get() = _totalCorrectionsApplied
    val totalSessionsWithCorrections: Int get() = _totalSessionsWithCorrections

    fun recordCorrectionAccepted() {
        _totalCorrectionsApplied++
        correctionsThisSession++
    }

    fun onSessionEnd() {
        if (correctionsThisSession > 0) {
            _totalSessionsWithCorrections++
            correctionsThisSession = 0
        }
    }

    fun loadFromJson(json: String) {
        val trimmed = json.trim()
        if (!trimmed.startsWith('{') || !trimmed.endsWith('}')) return
        _totalCorrectionsApplied = parseIntField(trimmed, "total") ?: 0
        _totalSessionsWithCorrections = parseIntField(trimmed, "sessions") ?: 0
    }

    fun toJson(): String =
        """{"total":$_totalCorrectionsApplied,"sessions":$_totalSessionsWithCorrections}"""

    private fun parseIntField(json: String, key: String): Int? {
        val pattern = Regex(""""$key"\s*:\s*(\d+)""")
        return pattern.find(json)?.groupValues?.get(1)?.toIntOrNull()
    }
}
