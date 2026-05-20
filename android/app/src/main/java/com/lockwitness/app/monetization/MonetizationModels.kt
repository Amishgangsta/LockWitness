package com.lockwitness.app.monetization

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

enum class ProFeature {
    UnlimitedHistory,
    VideoCapture,
    LocationSnapshot,
    ExportZip,
    NoAds
}

data class MonetizationState(
    val isPro: Boolean = false,
    val billingAvailable: Boolean = false
) {
    companion object {
        val Free = MonetizationState(isPro = false, billingAvailable = false)
        val Pro = MonetizationState(isPro = true, billingAvailable = true)
    }
}

data class BillingStatus(
    val available: Boolean,
    val isPro: Boolean = false,
    val message: String
)

interface ProBillingService {
    suspend fun refreshStatus(): BillingStatus
    val purchaseState: Flow<MonetizationState>
        get() = flowOf(MonetizationState.Free)
}

class SafeFallbackBillingService : ProBillingService {
    override suspend fun refreshStatus(): BillingStatus =
        BillingStatus(
            available = false,
            isPro = false,
            message = "Billing unavailable; Free mode remains active."
        )

    override val purchaseState: Flow<MonetizationState>
        get() = flowOf(MonetizationState.Free)
}
