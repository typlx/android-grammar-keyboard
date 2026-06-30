package com.typlx.keyboard

enum class DiffKind { UNCHANGED, ADDED, REMOVED }

data class DiffSegment(val text: String, val kind: DiffKind)

/**
 * Computes a word-level diff between [original] and [corrected] using LCS.
 * Returns segments tagged as UNCHANGED, ADDED, or REMOVED so the UI can
 * render the corrections with colour-coded inline highlighting.
 */
internal fun diffWords(original: String, corrected: String): List<DiffSegment> {
    if (original == corrected) return listOf(DiffSegment(corrected, DiffKind.UNCHANGED))

    val origTokens = tokenize(original)
    val corrTokens = tokenize(corrected)

    if (origTokens.isEmpty()) return listOf(DiffSegment(corrected, DiffKind.ADDED))
    if (corrTokens.isEmpty()) return listOf(DiffSegment(original, DiffKind.REMOVED))

    val lcs = lcsTable(origTokens, corrTokens)
    return buildSegments(origTokens, corrTokens, lcs)
}

/** Splits text into alternating word and whitespace tokens, preserving spacing. */
private fun tokenize(text: String): List<String> {
    val tokens = mutableListOf<String>()
    val regex = Regex("""\S+|\s+""")
    regex.findAll(text).forEach { tokens.add(it.value) }
    return tokens
}

private fun lcsTable(a: List<String>, b: List<String>): Array<IntArray> {
    val m = a.size; val n = b.size
    val dp = Array(m + 1) { IntArray(n + 1) }
    for (i in 1..m) for (j in 1..n) {
        dp[i][j] = if (a[i - 1] == b[j - 1]) dp[i - 1][j - 1] + 1
                   else maxOf(dp[i - 1][j], dp[i][j - 1])
    }
    return dp
}

private fun buildSegments(
    orig: List<String>,
    corr: List<String>,
    dp: Array<IntArray>,
): List<DiffSegment> {
    val result = mutableListOf<DiffSegment>()
    var i = orig.size; var j = corr.size
    val raw = mutableListOf<DiffSegment>()

    while (i > 0 || j > 0) {
        when {
            i > 0 && j > 0 && orig[i - 1] == corr[j - 1] -> {
                raw.add(DiffSegment(orig[i - 1], DiffKind.UNCHANGED))
                i--; j--
            }
            j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j]) -> {
                raw.add(DiffSegment(corr[j - 1], DiffKind.ADDED))
                j--
            }
            else -> {
                raw.add(DiffSegment(orig[i - 1], DiffKind.REMOVED))
                i--
            }
        }
    }

    raw.reverse()
    // Merge adjacent segments of the same kind for cleaner output.
    for (seg in raw) {
        val last = result.lastOrNull()
        if (last != null && last.kind == seg.kind) {
            result[result.lastIndex] = last.copy(text = last.text + seg.text)
        } else {
            result.add(seg)
        }
    }
    return result
}
