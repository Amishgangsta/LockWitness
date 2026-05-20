package com.lockwitness.app.monetization

enum class ProProductType { SUBSCRIPTION, ONE_TIME }

data class ProProduct(
    val productId: String,
    val type: ProProductType,
    val displayName: String,
    val displayPrice: String,
    val badge: String? = null,
    val note: String? = null
)

object ProProducts {
    const val ID_MONTHLY = "lockwitness_pro_monthly"
    const val ID_ANNUAL = "lockwitness_pro_annual"
    const val ID_LIFETIME = "lockwitness_pro_lifetime"

    val MONTHLY = ProProduct(
        productId = ID_MONTHLY,
        type = ProProductType.SUBSCRIPTION,
        displayName = "Pro Monthly",
        displayPrice = "$2.99 / month"
    )

    val ANNUAL = ProProduct(
        productId = ID_ANNUAL,
        type = ProProductType.SUBSCRIPTION,
        displayName = "Pro Annual",
        displayPrice = "$19.99 / year",
        badge = "Best Value — Save ~44%"
    )

    // Founder promo: $19.99. Standard price after promo period: $39.99.
    val LIFETIME = ProProduct(
        productId = ID_LIFETIME,
        type = ProProductType.ONE_TIME,
        displayName = "Lifetime Pro",
        displayPrice = "$19.99",
        note = "Founder price — increases to \$39.99 after launch promo"
    )

    val ALL = listOf(MONTHLY, ANNUAL, LIFETIME)
}
