package com.typlx.keyboard

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
class GrammarService {

    companion object {
        private const val SYSTEM_PROMPT =
            "Fix grammar and spelling in the following text. Return only the corrected text, nothing else. Preserve the original language, tone, and formatting."
        private const val TEMPERATURE = 0.3
        private const val TIMEOUT_SECONDS = 30L
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private val client: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build()
        }
    }

    /**
     * Sends text to the grammar-fix API and returns the corrected text.
     *
     * @param apiUrl Base API URL (e.g. "https://api.openai.com/v1")
     * @param model Model identifier (e.g. "gpt-4o-mini")
     * @param token Bearer token for authorization
     * @param text The text to correct
     * @return The corrected text from the API
     * @throws GrammarServiceException on any failure
     */
    suspend fun fixGrammar(
        apiUrl: String,
        model: String,
        token: String,
        text: String
    ): String = withContext(Dispatchers.IO) {
        // Normalise: strip a trailing /v1 if present, then always add /v1/chat/completions
        // so both "https://api.openai.com" and "https://api.openai.com/v1" work.
        val normalised = apiUrl.trimEnd('/').removeSuffix("/v1")
        val url = "$normalised/v1/chat/completions"

        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", SYSTEM_PROMPT)
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

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
                ?: throw GrammarServiceException("Empty response body")

            if (!response.isSuccessful) {
                throw GrammarServiceException(httpErrorMessage(response.code))
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
            throw GrammarServiceException("Could not connect to server", e)
        } catch (e: java.net.SocketTimeoutException) {
            throw GrammarServiceException("Request timed out — check your connection", e)
        } catch (e: Exception) {
            throw GrammarServiceException("Request failed: ${e.message}", e)
        }
    }
}

class GrammarServiceException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

internal fun httpErrorMessage(code: Int): String = when (code) {
    401, 403 -> "Invalid API token ($code)"
    429 -> "Rate limit reached — try again later"
    in 500..599 -> "Server error ($code) — try again later"
    else -> "API error $code"
}
