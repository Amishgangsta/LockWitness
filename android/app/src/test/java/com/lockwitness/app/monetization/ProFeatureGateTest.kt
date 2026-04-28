package com.lockwitness.app.monetization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProFeatureGateTest {
    private val gate = ProFeatureGate()

    @Test
    fun freeModeKeepsCoreFreeButBlocksProFeatures() {
        val state = MonetizationState.Free

        assertFalse(gate.isAllowed(ProFeature.UnlimitedHistory, state))
        assertFalse(gate.isAllowed(ProFeature.VideoCapture, state))
        assertFalse(gate.isAllowed(ProFeature.LocationSnapshot, state))
        assertFalse(gate.isAllowed(ProFeature.ExportZip, state))
        assertFalse(gate.isAllowed(ProFeature.NoAds, state))
    }

    @Test
    fun proModeAllowsProFeaturesAndRemovesAds() {
        val state = MonetizationState.Pro

        assertTrue(gate.isAllowed(ProFeature.UnlimitedHistory, state))
        assertTrue(gate.isAllowed(ProFeature.VideoCapture, state))
        assertTrue(gate.isAllowed(ProFeature.LocationSnapshot, state))
        assertTrue(gate.isAllowed(ProFeature.ExportZip, state))
        assertTrue(gate.isAllowed(ProFeature.NoAds, state))
        assertFalse(gate.shouldShowAds(state))
    }

    @Test
    fun freeHistoryIsLimitedAndProHistoryIsUnlimited() {
        val incidents = (1..15).toList()

        assertEquals((1..10).toList(), gate.visibleHistory(incidents, MonetizationState.Free))
        assertEquals(incidents, gate.visibleHistory(incidents, MonetizationState.Pro))
    }

    @Test
    fun freeUsersSeeAds() {
        assertTrue(gate.shouldShowAds(MonetizationState.Free))
    }
}
