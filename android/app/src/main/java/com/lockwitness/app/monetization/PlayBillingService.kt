package com.lockwitness.app.monetization

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayBillingService private constructor(private val context: Context) : ProBillingService {

    private val _purchaseState = MutableStateFlow(MonetizationState.Free)
    override val purchaseState: Flow<MonetizationState> = _purchaseState.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { purchase ->
                scope.launch { handlePurchase(purchase) }
            }
        }
    }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    init {
        connect()
    }

    private fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch { queryAndAcknowledgePurchases() }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Billing service disconnected. State will remain stale until next app session.
            }
        })
    }

    override suspend fun refreshStatus(): BillingStatus {
        if (!billingClient.isReady) {
            return BillingStatus(available = false, isPro = false, message = "Billing service not ready.")
        }
        val isPro = queryAndAcknowledgePurchases()
        return BillingStatus(
            available = true,
            isPro = isPro,
            message = if (isPro) "Pro active." else "Free mode."
        )
    }

    suspend fun queryProductDetails(): List<ProductDetails> {
        if (!billingClient.isReady) return emptyList()

        val results = mutableListOf<ProductDetails>()

        val subsResult = billingClient.queryProductDetails(
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(ProProducts.ID_MONTHLY, ProProducts.ID_ANNUAL).map { id ->
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(id)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    }
                )
                .build()
        )
        if (subsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            results.addAll(subsResult.productDetailsList.orEmpty())
        }

        val inappResult = billingClient.queryProductDetails(
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(ProProducts.ID_LIFETIME)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    )
                )
                .build()
        )
        if (inappResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            results.addAll(inappResult.productDetailsList.orEmpty())
        }

        return results
    }

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails): BillingResult {
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .apply { offerToken?.let { setOfferToken(it) } }
            .build()
        return billingClient.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()
        )
    }

    private suspend fun queryAndAcknowledgePurchases(): Boolean {
        var isPro = false

        val subsResult = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val activeSubs = subsResult.purchasesList.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        if (activeSubs.any { it.products.any { id -> id == ProProducts.ID_MONTHLY || id == ProProducts.ID_ANNUAL } }) {
            isPro = true
        }
        activeSubs.filter { !it.isAcknowledged }.forEach { acknowledgePurchase(it) }

        val inappResult = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val activeInapp = inappResult.purchasesList.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        if (activeInapp.any { it.products.contains(ProProducts.ID_LIFETIME) }) {
            isPro = true
        }
        activeInapp.filter { !it.isAcknowledged }.forEach { acknowledgePurchase(it) }

        _purchaseState.value = MonetizationState(isPro = isPro, billingAvailable = true)
        return isPro
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        if (!purchase.isAcknowledged) acknowledgePurchase(purchase)
        _purchaseState.value = MonetizationState(isPro = true, billingAvailable = true)
    }

    private suspend fun acknowledgePurchase(purchase: Purchase) {
        billingClient.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        )
    }

    companion object {
        @Volatile
        private var instance: PlayBillingService? = null

        fun getInstance(context: Context): PlayBillingService =
            instance ?: synchronized(this) {
                instance ?: PlayBillingService(context.applicationContext).also { instance = it }
            }
    }
}
