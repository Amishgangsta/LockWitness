package com.lockwitness.app.monetization

class ProFeatureGate(
    private val freeHistoryLimit: Int = FREE_HISTORY_LIMIT
) {
    fun isAllowed(feature: ProFeature, state: MonetizationState): Boolean =
        when (feature) {
            ProFeature.UnlimitedHistory,
            ProFeature.VideoCapture,
            ProFeature.LocationSnapshot,
            ProFeature.ExportZip,
            ProFeature.NoAds -> state.isPro
        }

    fun shouldShowAds(state: MonetizationState): Boolean =
        !isAllowed(ProFeature.NoAds, state)

    fun <T> visibleHistory(items: List<T>, state: MonetizationState): List<T> =
        if (isAllowed(ProFeature.UnlimitedHistory, state)) {
            items
        } else {
            items.take(freeHistoryLimit)
        }

    companion object {
        const val FREE_HISTORY_LIMIT = 10
    }
}
