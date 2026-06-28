package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class FeatureGateTest {

    @Test
    fun `all features enabled in passthrough mode`() {
        FeatureGate.Feature.entries.forEach { feature ->
            assertTrue(
                "Expected $feature to be enabled in passthrough mode",
                FeatureGate.isEnabled(feature)
            )
        }
    }

    @Test
    fun `grammar fix is a free-tier feature`() {
        assertTrue(FeatureGate.isFreeTier(FeatureGate.Feature.GRAMMAR_FIX))
    }

    @Test
    fun `premium features are not free-tier`() {
        val premiumOnlyFeatures = listOf(
            FeatureGate.Feature.MULTI_LANGUAGE,
            FeatureGate.Feature.TONE_SUGGESTIONS,
            FeatureGate.Feature.EXTENDED_DOCUMENT_LENGTH,
            FeatureGate.Feature.CUSTOM_DICTIONARY,
        )
        premiumOnlyFeatures.forEach { feature ->
            assertFalse(
                "Expected $feature to NOT be free-tier",
                FeatureGate.isFreeTier(feature)
            )
        }
    }

    @Test
    fun `all Feature enum values are accounted for in free-tier check`() {
        // Ensures every Feature has an explicit free/premium classification —
        // adding a new Feature without updating freeTierFeatures is intentional,
        // but this test reminds the author to decide its tier.
        val allFeatures = FeatureGate.Feature.entries.toSet()
        val checkedFeatures = setOf(
            FeatureGate.Feature.GRAMMAR_FIX,
            FeatureGate.Feature.MULTI_LANGUAGE,
            FeatureGate.Feature.TONE_SUGGESTIONS,
            FeatureGate.Feature.EXTENDED_DOCUMENT_LENGTH,
            FeatureGate.Feature.CUSTOM_DICTIONARY,
        )
        assertEquals(
            "Some Feature values are not covered by FeatureGateTest — add them with an explicit tier assertion",
            allFeatures,
            checkedFeatures
        )
    }
}
