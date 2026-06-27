package com.typlx.keyboard

import android.app.Activity
import android.content.Context

/**
 * Stub for RevenueCat SDK integration.
 *
 * All methods are no-ops or return safe defaults until TYP-124 is approved
 * and the RevenueCat API key + Play Store product IDs are available.
 *
 * Post-approval wiring checklist:
 * 1. Add RevenueCat dependency to build.gradle.kts:
 *      implementation("com.revenuecat.purchases:purchases:7.x.x")
 * 2. Call [configure] in Application.onCreate() with the API key from RevenueCat dashboard.
 * 3. Replace stub bodies with real Purchases SDK calls.
 * 4. Map [FeatureGate.Feature] values to RevenueCat entitlement identifiers.
 */
object PurchaseManager {

    /** RevenueCat entitlement ID that grants premium access. */
    const val ENTITLEMENT_PREMIUM = "premium"

    private var configured = false

    /**
     * Initialize RevenueCat SDK. Call from Application.onCreate().
     *
     * @param context Application context
     * @param apiKey RevenueCat public SDK key (from RevenueCat dashboard)
     */
    fun configure(context: Context, apiKey: String) {
        if (configured) return
        // TODO(TYP-124): Purchases.configure(PurchasesConfiguration.Builder(context, apiKey).build())
        configured = true
    }

    /**
     * Returns whether the current user has an active premium entitlement.
     *
     * Stub: always returns false (no entitlement). FeatureGate.isEnabled() overrides
     * this with passthrough=true during the pre-launch phase.
     */
    suspend fun hasEntitlement(entitlementId: String = ENTITLEMENT_PREMIUM): Boolean {
        // TODO(TYP-124): query Purchases.sharedInstance.awaitCustomerInfo().entitlements[entitlementId]?.isActive
        return false
    }

    /**
     * Initiates a purchase flow for the given product.
     *
     * @param activity The calling Activity (required by RevenueCat)
     * @param productId Play Store subscription/in-app product ID
     * @return [PurchaseResult] indicating success or failure
     */
    suspend fun purchase(activity: Activity, productId: String): PurchaseResult {
        // TODO(TYP-124): implement via Purchases.sharedInstance.awaitPurchase(...)
        return PurchaseResult.NotConfigured
    }

    /**
     * Restores purchases for the current user (required by Play Store policy).
     *
     * @return true if any active entitlement was restored
     */
    suspend fun restorePurchases(): Boolean {
        // TODO(TYP-124): implement via Purchases.sharedInstance.awaitRestorePurchases()
        return false
    }
}

sealed class PurchaseResult {
    data class Success(val entitlementId: String) : PurchaseResult()
    data class Error(val message: String, val userCancelled: Boolean = false) : PurchaseResult()
    object NotConfigured : PurchaseResult()
}
