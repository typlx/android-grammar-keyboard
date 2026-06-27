package com.typlx.keyboard

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock

class PurchaseManagerTest {

    @Test
    fun `hasEntitlement returns false for premium entitlement`() = runTest {
        assertFalse(PurchaseManager.hasEntitlement(PurchaseManager.ENTITLEMENT_PREMIUM))
    }

    @Test
    fun `hasEntitlement returns false with default parameter`() = runTest {
        assertFalse(PurchaseManager.hasEntitlement())
    }

    @Test
    fun `purchase returns NotConfigured`() = runTest {
        val activity = mock(Activity::class.java)
        val result = PurchaseManager.purchase(activity, "premium_monthly")
        assertTrue("purchase should return NotConfigured, got $result", result is PurchaseResult.NotConfigured)
    }

    @Test
    fun `restorePurchases returns false`() = runTest {
        assertFalse(PurchaseManager.restorePurchases())
    }

    @Test
    fun `configure is safe to call multiple times`() {
        val context = mock(Context::class.java)
        PurchaseManager.configure(context, "api-key-1")
        PurchaseManager.configure(context, "api-key-2")
    }
}
