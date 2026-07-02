package com.typlx.keyboard

import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.net.URI

/**
 * Privacy-safe wrapper around FirebaseCrashlytics for non-fatal error reporting.
 *
 * PRIVACY CONTRACT: user-typed text is NEVER included in any key or log call.
 * Only structural error metadata is captured: operation type, API domain (no path or params),
 * and the exception message from GrammarServiceException (which contains no user text).
 */
object CrashReporter {

    enum class Operation { GRAMMAR_FIX, TONE_REWRITE, TRANSLATION, AUTO_SUGGEST }

    fun recordApiError(error: GrammarServiceException, operation: Operation, apiUrl: String) {
        try {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.setCustomKey("operation", operation.name)
            crashlytics.setCustomKey("api_domain", extractDomain(apiUrl))
            crashlytics.recordException(error)
        } catch (_: Exception) {
            // Crashlytics unavailable in CI or when google-services.json is absent — safe to skip.
        }
    }

    private fun extractDomain(url: String): String = try {
        URI(url.trim()).host?.lowercase() ?: "unknown"
    } catch (_: Exception) {
        "invalid_url"
    }
}
