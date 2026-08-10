package com.typlx.keyboard

import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * API client that sends text to an OpenAI-compatible chat completions endpoint
 * for grammar and spelling correction.
 */
class GrammarService(
    private val httpClient: OkHttpClient = sharedClient,
    val maxRetries: Int = MAX_RETRIES,
) {

    companion object {
        const val SYSTEM_PROMPT =
            "Fix grammar and spelling in the following text. Return only the corrected text, nothing else. Preserve the original language, tone, and formatting."

        fun buildSystemPrompt(language: InputLanguage? = null, suffix: String = "", enabledRules: Set<GrammarRule> = GrammarRule.ALL): String {
            val base = when {
                enabledRules != GrammarRule.ALL && enabledRules.isNotEmpty() -> {
                    val items = GrammarRule.entries
                        .filter { it in enabledRules }
                        .map { it.description }
                    val ruleList = when (items.size) {
                        1 -> items[0]
                        2 -> "${items[0]} and ${items[1]}"
                        else -> "${items.dropLast(1).joinToString(", ")}, and ${items.last()}"
                    }
                    val langPart = if (language != null) "the following ${language.displayName} " else "the following "
                    "Fix only $ruleList in ${langPart}text. Do not change anything else. Return only the corrected text, nothing else. Preserve the original tone and formatting."
                }
                language != null ->
                    "Fix grammar and spelling in the following ${language.displayName} text. Return only the corrected text, nothing else. Preserve the original tone and formatting."
                else -> SYSTEM_PROMPT
            }
            return if (suffix.isBlank()) base else "$base $suffix"
        }

        fun buildSystemPrompt(suffix: String): String = buildSystemPrompt(null, suffix)
        private const val TEMPERATURE = 0.3
        private const val TIMEOUT_SECONDS = 30L
        private const val MAX_RETRIES = 2
        private const val RETRY_BASE_DELAY_MS = 1000L
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private val sharedClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build()
        }

        /** Forces OkHttpClient initialization; call on a background thread during service creation. */
        internal fun prewarm() { sharedClient }
    }

    /**
     * Sends a minimal request to verify the API configuration is reachable.
     * Returns elapsed time in milliseconds on success, or throws [GrammarServiceException].
     * Does not retry — a single attempt is enough for a connectivity check.
     */
    suspend fun testConnection(apiUrl: String, model: String, token: String): Long {
        val start = System.currentTimeMillis()
        withContext(Dispatchers.IO) {
            doAttempt(apiUrl, model, token, "Hello.", SYSTEM_PROMPT)
        }
        return System.currentTimeMillis() - start
    }

    /**
     * Sends text to the API and returns the rewritten text. Retries up to [maxRetries] times
     * on transient failures (network timeout, connection drop, HTTP 429/503/504) using
     * exponential backoff starting at 1 s.
     *
     * @param apiUrl Base API URL (e.g. "https://api.openai.com/v1")
     * @param model Model identifier (e.g. "gpt-4o-mini")
     * @param token Bearer token for authorization
     * @param text The text to process
     * @param systemPrompt Instruction sent as the system message; defaults to the grammar-fix prompt
     * @return The rewritten text from the API
     * @throws GrammarServiceException on any failure (after exhausting retries for transient errors)
     */
    suspend fun fixGrammar(
        apiUrl: String,
        model: String,
        token: String,
        text: String,
        systemPrompt: String = SYSTEM_PROMPT,
    ): String {
        var lastException: GrammarServiceException? = null
        for (attempt in 0..maxRetries) {
            try {
                return withContext(Dispatchers.IO) { doAttempt(apiUrl, model, token, text, systemPrompt) }
            } catch (e: GrammarServiceException) {
                lastException = e
                if (!e.isRetryable || attempt == maxRetries) throw e
                delay(RETRY_BASE_DELAY_MS shl attempt)  // 1 s, 2 s
            }
        }
        throw lastException!!
    }

    private fun doAttempt(
        apiUrl: String,
        model: String,
        token: String,
        text: String,
        systemPrompt: String,
    ): String {
        // Normalise: strip a trailing /v1 if present, then always add /v1/chat/completions
        // so both "https://api.openai.com" and "https://api.openai.com/v1" work.
        val normalised = apiUrl.trimEnd('/').removeSuffix("/v1")
        val url = "$normalised/v1/chat/completions"

        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", text)
            })
        }

        val body = JSONObject().apply {
            put("model", model)
            put("messages", messages)
            put("temperature", TEMPERATURE)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        return try {
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string()
                ?: throw GrammarServiceException("Empty response body")

            if (!response.isSuccessful) {
                throw GrammarServiceException(
                    message = httpErrorMessage(response.code),
                    isRetryable = isRetryableHttpCode(response.code),
                )
            }

            val json = JSONObject(responseBody)
            val choices = json.getJSONArray("choices")
            if (choices.length() == 0) {
                throw GrammarServiceException("No choices in API response")
            }

            choices.getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
        } catch (e: GrammarServiceException) {
            throw e
        } catch (e: java.net.UnknownHostException) {
            throw GrammarServiceException("No internet connection", e)
        } catch (e: java.net.ConnectException) {
            throw GrammarServiceException("Could not connect to server", e, isRetryable = true)
        } catch (e: java.net.SocketTimeoutException) {
            throw GrammarServiceException("Request timed out — check your connection", e, isRetryable = true)
        } catch (e: Exception) {
            throw GrammarServiceException("Request failed: ${e.message}", e)
        }
    }
}

class GrammarServiceException(
    message: String,
    cause: Throwable? = null,
    val isRetryable: Boolean = false,
) : Exception(message, cause)

internal fun httpErrorMessage(code: Int): String = when (code) {
    401, 403 -> "Invalid API token ($code)"
    429 -> "Rate limit reached — try again later"
    in 500..599 -> "Server error ($code) — try again later"
    else -> "API error $code"
}

internal fun isRetryableHttpCode(code: Int): Boolean = code == 429 || code == 503 || code == 504
