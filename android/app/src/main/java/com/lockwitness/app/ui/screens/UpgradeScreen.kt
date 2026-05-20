package com.lockwitness.app.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.lockwitness.app.monetization.MonetizationState
import com.lockwitness.app.monetization.PlayBillingService
import com.lockwitness.app.monetization.ProProduct
import com.lockwitness.app.monetization.ProProducts
import kotlinx.coroutines.launch

@Composable
fun UpgradeScreen(
    contentPadding: PaddingValues,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val billingService = remember(context) { PlayBillingService.getInstance(context) }
    val monetizationState by billingService.purchaseState.collectAsState(initial = MonetizationState.Free)
    var productDetailsList by remember { mutableStateOf<List<ProductDetails>>(emptyList()) }
    var purchaseMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        productDetailsList = billingService.queryProductDetails()
    }

    if (monetizationState.isPro) {
        ProAlreadyActiveScreen(contentPadding = contentPadding, onNavigateBack = onNavigateBack)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "LockWitness Pro",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Your device. Your evidence. Your control.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FeatureComparisonCard()

        Text(
            text = "Choose your plan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        ProProducts.ALL.forEach { product ->
            val details = productDetailsList.firstOrNull { it.productId == product.productId }
            PurchaseOptionCard(
                product = product,
                productDetails = details,
                onPurchase = {
                    val activity = context.findActivity()
                    if (activity != null && details != null) {
                        val result = billingService.launchBillingFlow(activity, details)
                        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                            purchaseMessage = "Could not start purchase: ${result.debugMessage}"
                        }
                    } else if (details == null) {
                        purchaseMessage = "Product not available. Ensure the app is connected to Google Play."
                    } else {
                        purchaseMessage = "Unable to launch purchase from this screen."
                    }
                }
            )
        }

        OutlinedButton(
            onClick = { scope.launch { billingService.refreshStatus() } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Restore Purchases")
        }

        TextButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue with Free")
        }

        purchaseMessage?.let { msg ->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ProAlreadyActiveScreen(
    contentPadding: PaddingValues,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "You're on Pro",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "All features are unlocked. Thank you for supporting LockWitness.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
private fun FeatureComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Feature",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    text = "Free",
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.75f)
                )
                Text(
                    text = "Pro",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.75f)
                )
            }

            val rows = listOf(
                Triple("Failed-unlock detection", true, true),
                Triple("Basic incident logging", true, true),
                Triple("Incident history", false, true),  // "Last 10" vs unlimited handled in label
                Triple("Unlimited photos per incident", false, true),
                Triple("Full video capture", false, true),
                Triple("GPS location snapshot", false, true),
                Triple("ZIP + SHA-256 export", false, true),
                Triple("Advanced diagnostics & sharing", false, true),
                Triple("Ad-free experience", false, true)
            )

            // Special row for history limit
            FeatureRow(
                label = "Incident history",
                freeLabel = "Last 10",
                proLabel = "Unlimited"
            )

            rows.drop(2).forEach { (label, free, pro) ->
                FeatureRow(label = label, freeValue = free, proValue = pro)
            }
        }
    }
}

@Composable
private fun FeatureRow(label: String, freeValue: Boolean, proValue: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1.5f)
        )
        Box(modifier = Modifier.weight(0.75f), contentAlignment = Alignment.Center) {
            if (freeValue) {
                Icon(Icons.Outlined.Check, contentDescription = "Included", tint = MaterialTheme.colorScheme.primary)
            } else {
                Icon(Icons.Outlined.Close, contentDescription = "Not included", tint = MaterialTheme.colorScheme.outline)
            }
        }
        Box(modifier = Modifier.weight(0.75f), contentAlignment = Alignment.Center) {
            if (proValue) {
                Icon(Icons.Outlined.Check, contentDescription = "Included", tint = MaterialTheme.colorScheme.primary)
            } else {
                Icon(Icons.Outlined.Close, contentDescription = "Not included", tint = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun FeatureRow(label: String, freeLabel: String, proLabel: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1.5f)
        )
        Text(
            text = freeLabel,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.75f)
        )
        Text(
            text = proLabel,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(0.75f)
        )
    }
}

@Composable
private fun PurchaseOptionCard(
    product: ProProduct,
    productDetails: ProductDetails?,
    onPurchase: () -> Unit
) {
    val isBestValue = product.badge != null
    val borderColor = if (isBestValue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val borderWidth = if (isBestValue) 2.dp else 1.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isBestValue)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                product.badge?.let { badge ->
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            val displayPrice = productDetails?.let { details ->
                when {
                    details.subscriptionOfferDetails != null ->
                        details.subscriptionOfferDetails!!.firstOrNull()
                            ?.pricingPhases?.pricingPhaseList?.firstOrNull()
                            ?.formattedPrice ?: product.displayPrice
                    details.oneTimePurchaseOfferDetails != null ->
                        details.oneTimePurchaseOfferDetails!!.formattedPrice
                    else -> product.displayPrice
                }
            } ?: product.displayPrice

            Text(
                text = displayPrice,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            product.note?.let { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onPurchase,
                modifier = Modifier.fillMaxWidth(),
                colors = if (isBestValue) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
            ) {
                Text(if (isBestValue) "Get Best Value" else "Choose ${product.displayName}")
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
