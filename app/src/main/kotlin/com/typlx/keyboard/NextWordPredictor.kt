package com.typlx.keyboard

import android.content.Context
import org.json.JSONObject

/**
 * Offline next-word prediction using a curated English bigram map.
 *
 * Bigrams are stored in res/raw/bigrams.json so the data is editable without
 * recompiling and can be extended for additional languages later.
 *
 * Used when the user is between words (no current prefix): the keyboard passes
 * the last committed word and gets back up to [maxResults] likely next words.
 * All lookups are < 1 ms — no network involved.
 */
class NextWordPredictor(private val bigrams: Map<String, List<String>>) {

    fun predict(lastWord: String, maxResults: Int = 3): List<String> {
        if (lastWord.isBlank()) return emptyList()
        val key = lastWord.lowercase().trimEnd('.', '!', '?', ',', ';', ':', '"', '\'')
        return bigrams[key]?.take(maxResults) ?: emptyList()
    }

    companion object {
        fun fromContext(context: Context): NextWordPredictor {
            val json = context.resources.openRawResource(R.raw.bigrams)
                .bufferedReader()
                .use { it.readText() }
            val obj = JSONObject(json)
            val map = buildMap<String, List<String>> {
                for (key in obj.keys()) {
                    val arr = obj.getJSONArray(key)
                    val values = buildList { repeat(arr.length()) { add(arr.getString(it)) } }
                    put(key, values)
                }
            }
            return NextWordPredictor(map)
        }
    }
}
