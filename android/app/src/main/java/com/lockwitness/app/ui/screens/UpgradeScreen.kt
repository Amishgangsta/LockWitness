package com.lockwitness.app.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.lockwitness.app.ui.components.ForensicCard
import com.lockwitness.app.ui.components.ForensicDivider
import com.lockwitness.app.ui.components.SectionEyebrow
import com.lockwitness.app.ui.components.SpecPill
import com.lockwitness.app.ui.components.PillType
import com.lockwitness.app.ui.theme.CardSurface
import com.lockwitness.app.ui.theme.CautionAmber
import com.lockwitness.app.ui.theme.DestructiveRed
import com.lockwitness.app.ui.theme.GraphiteBg
import com.lockwitness.app.ui.theme.MutedChip
import com.lockwitness.app.ui.theme.ProOrange
import com.lockwitness.app.ui.theme.StrokeSubtle
import com.lockwitness.app.ui.theme.TextPrimary
import com.lockwitness.app.ui.theme.TextSecondary
import com.lockwitness.app.ui.theme.VerifiedGreen
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
            .background(GraphiteBg)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Outlined.WorkspacePremium, contentDescription = null, tint = ProOrange, modifier = Modifier.size(28.dp))
            Column {
                Text(
                    text = "LockWitness Pro",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Your device. Your evidence. Your control.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Feature comparison card
        ForensicCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                SectionEyebrow("What You Unlock")
                Spacer(modifier = Modifier.height(10.dp))
                val features = listOf(
                    "Full incident history — unlimited retention",
                    "Video capture on failed unlock",
                    "GPS location snapshot",
                    "ZIP + SHA-256 forensic export",
                    "Advanced diagnostics and sharing",
                    "Ad-free experience"
                )
                features.forEachIndexed { i, feature ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(16.dp))
                        Text(feature, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    }
                    if (i < features.lastIndex) ForensicDivider()
                }
            }
        }

        SectionEyebrow("Choose Your Plan")

        // Plan cards
        ProProducts.ALL.forEach { product ->
            val details = productDetailsList.firstOrNull { it.productId == product.productId }
            PlanCard(
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

        // Restore
        OutlinedButton(
            onClick = { scope.launch { billingService.refreshStatus() } },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
            border = BorderStroke(1.dp, StrokeSubtle)
        ) {
            Text("Restore Purchases", style = MaterialTheme.typography.labelMedium)
        }

        TextButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue with Free", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
        }

        purchaseMessage?.let { msg ->
            Text(
                text = msg,
                color = DestructiveRed,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PlanCard(
    product: ProProduct,
    productDetails: ProductDetails?,
    onPurchase: () -> Unit
) {
    val isBestValue = product.badge?.contains("Best Value") == true
    val isFounder = product.badge == null && product.note != null
    val pillType = when {
        isBestValue -> PillType.BEST_VALUE
        isFounder -> PillType.FOUNDER
        else -> null
    }
    val borderColor = when {
        isBestValue -> ProOrange
        isFounder -> CautionAmber
        else -> StrokeSubtle
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardSurface)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(product.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            pillType?.let { SpecPill(type = it) }
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
            color = ProOrange
        )

        product.note?.let { note ->
            Text(note, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }

        Button(
            onClick = onPurchase,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ProOrange, contentColor = TextPrimary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                if (isBestValue) "Get Best Value" else "Choose ${product.displayName}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
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
            .background(GraphiteBg)
            .padding(contentPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Icon(Icons.Outlined.WorkspacePremium, contentDescription = null, tint = ProOrange, modifier = Modifier.size(48.dp))
        Text("You're on Pro", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(
            "All features are unlocked. Thank you for supporting LockWitness.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = TextSecondary
        )
        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen, contentColor = TextPrimary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Back", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
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
