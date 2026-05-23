package com.lockwitness.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.GppGood
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NoPhotography
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lockwitness.app.admin.AndroidDeviceInfoProvider
import com.lockwitness.app.monetization.BannerAdPlaceholder
import com.lockwitness.app.monetization.MonetizationRepository
import com.lockwitness.app.monetization.MonetizationState
import com.lockwitness.app.ui.components.ForensicCard
import com.lockwitness.app.ui.components.ForensicDivider
import com.lockwitness.app.ui.components.SectionEyebrow
import com.lockwitness.app.ui.components.SpecPill
import com.lockwitness.app.ui.components.PillType
import com.lockwitness.app.ui.theme.GraphiteBg
import com.lockwitness.app.ui.theme.TextPrimary
import com.lockwitness.app.ui.theme.TextSecondary
import com.lockwitness.app.ui.theme.VerifiedGreen

@Composable
fun AboutScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    val monetizationRepository = remember(context) { MonetizationRepository.create(context) }
    val monetizationState by monetizationRepository.state.collectAsState(initial = MonetizationState(isPro = true, billingAvailable = false))
    val deviceInfo = remember(context) { AndroidDeviceInfoProvider(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GraphiteBg)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "About",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        // Identity card
        ForensicCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        SectionEyebrow("LockWitness")
                        Text(
                            text = "v${deviceInfo.appVersion}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    SpecPill(type = if (monetizationState.isPro) PillType.PRO else PillType.PAUSED)
                }
                ForensicDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Android", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(deviceInfo.androidVersion, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Device", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(deviceInfo.deviceModel, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Billing", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(
                        if (monetizationState.billingAvailable) "Available" else "Unavailable",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (monetizationState.billingAvailable) VerifiedGreen else TextSecondary
                    )
                }
            }
        }

        // Trust cards
        ForensicCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                SectionEyebrow("Privacy Commitments")
                Spacer(modifier = Modifier.height(10.dp))
                val commitments = listOf(
                    Icons.Outlined.Lock to "All evidence is stored locally on your device only.",
                    Icons.Outlined.NoPhotography to "No captured media is ever silently transmitted.",
                    Icons.Outlined.GppGood to "SHA-256 hashing provides tamper-evident integrity.",
                    Icons.Outlined.Shield to "Owner-controlled: you decide when evidence is exported.",
                    Icons.Outlined.CheckCircle to "No microphone, contacts, or call-log access."
                )
                commitments.forEachIndexed { i, (icon, text) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(icon, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(16.dp))
                        Text(text, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    }
                    if (i < commitments.lastIndex) ForensicDivider()
                }
            }
        }

        // Product description
        ForensicCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionEyebrow("What LockWitness Does")
                Text(
                    text = "LockWitness records tamper-evident evidence — photos, video, and GPS location — when someone fails to unlock your device. All evidence stays on your device until you choose to export it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Text(
                        "This is not spyware. It does not run hidden or without your knowledge.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        BannerAdPlaceholder(state = monetizationState)
    }
}
