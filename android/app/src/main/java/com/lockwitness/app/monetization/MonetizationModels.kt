package com.lockwitness.app.monetization

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
    val message: String
)

interface ProBillingService {
    suspend fun refreshStatus(): BillingStatus
}

class SafeFallbackBillingService : ProBillingService {
    override suspend fun refreshStatus(): BillingStatus =
        BillingStatus(
            available = false,
            message = "Billing unavailable; Free mode remains active."
        )
}
