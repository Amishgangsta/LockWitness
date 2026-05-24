package com.lockwitness.app.monetization

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

enum class ProFeature {
    UnlimitedHistory,
    VideoCapture,
    LocationSnapshot,
    ExportZip
}

data class MonetizationState(
    val isPro: Boolean = false,
    val billingAvailable: Boolean = false,
    val trialDaysRemaining: Int? = null
) {
    val isInTrial: Boolean get() = !isPro && trialDaysRemaining != null && trialDaysRemaining > 0
    val trialExpired: Boolean get() = !isPro && trialDaysRemaining != null && trialDaysRemaining <= 0

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
            message = "Billing unavailable; trial mode active."
        )

    override val purchaseState: Flow<MonetizationState>
        get() = flowOf(MonetizationState.Free)
}
