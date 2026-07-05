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
     * **Passthrough stub** — always returns true until TYP-124 (monetization) is approved.
     * This is intentional: all features are free during pre-launch.
     *
     * TODO(TYP-124): Replace this passthrough with a real entitlement check:
     *   `return feature in freeTierFeatures || PurchaseManager.hasEntitlement(ENTITLEMENT_PREMIUM)`
     *   once RevenueCat SDK and Supabase Auth are configured per [PurchaseManager].
     */
    @Suppress("UNUSED_PARAMETER")
    fun isEnabled(feature: Feature): Boolean {
        return true
    }

    /** True if [feature] is available on the free tier regardless of subscription. */
    fun isFreeTier(feature: Feature): Boolean = feature in freeTierFeatures
}
