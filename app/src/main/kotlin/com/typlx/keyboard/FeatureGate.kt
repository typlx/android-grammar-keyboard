package com.typlx.keyboard

/**
 * Central feature-gating point for premium features.
 *
 * Currently returns true for all features (passthrough mode) because
 * no subscription infrastructure is wired yet. Once TYP-124 is approved
 * and RevenueCat/Supabase are configured, replace [isEnabled] with a real
 * entitlement check via [PurchaseManager].
 *
 * Usage:
 *   if (!FeatureGate.isEnabled(Feature.ADVANCED_GRAMMAR)) {
 *       showUpsell(); return
 *   }
 */
object FeatureGate {

    enum class Feature {
        /** Grammar correction using external LLM API */
        GRAMMAR_FIX,
        /** Multi-language grammar support */
        MULTI_LANGUAGE,
        /** Tone and style suggestions */
        TONE_SUGGESTIONS,
        /** Extended document length beyond 5 000 characters */
        EXTENDED_DOCUMENT_LENGTH,
        /** Custom user dictionary */
        CUSTOM_DICTIONARY,
    }

    /**
     * The set of features that are free-tier (available to all users).
     * All other [Feature] values require a premium entitlement.
     *
     * [Feature.GRAMMAR_FIX] is in the free set so existing users see no
     * regression before monetization is live.
     */
    private val freeTierFeatures = setOf(Feature.GRAMMAR_FIX)

    /**
     * Returns whether [feature] is accessible to the current user.
     *
     * Passthrough phase: always true — no entitlement check yet.
     * Post-TYP-124: delegate to [PurchaseManager.hasEntitlement].
     */
    @Suppress("UNUSED_PARAMETER")
    fun isEnabled(feature: Feature): Boolean {
        // TODO(TYP-124): replace with PurchaseManager.hasEntitlement(feature)
        //   once RevenueCat SDK and Supabase Auth are configured.
        return true
    }

    /** True if [feature] is available on the free tier regardless of subscription. */
    fun isFreeTier(feature: Feature): Boolean = feature in freeTierFeatures
}
