package com.lockwitness.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import com.lockwitness.app.ui.components.ForensicCard
import com.lockwitness.app.ui.components.SectionEyebrow
import com.lockwitness.app.ui.theme.LWBackground
import com.lockwitness.app.ui.theme.LWTextPrimary
import com.lockwitness.app.ui.theme.LWTextSecondary
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lockwitness.app.monetization.BannerAdPlaceholder
import com.lockwitness.app.monetization.MonetizationRepository
import com.lockwitness.app.monetization.MonetizationState

@Composable
fun AboutScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    val monetizationRepository = remember(context) { MonetizationRepository.create(context) }
    val monetizationState by monetizationRepository.state.collectAsState(initial = MonetizationState.Free)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LWBackground)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "About",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = LWTextPrimary,
            modifier = androidx.compose.ui.Modifier.padding(vertical = 8.dp)
        )
        ForensicCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionEyebrow("Lock Witness")
                Text(
                    text = "Owner-controlled failed-unlock evidence recorder.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LWTextPrimary
                )
                Text(
                    text = if (monetizationState.isPro) "Plan: Pro" else "Plan: Free",
                    style = MaterialTheme.typography.bodySmall,
                    color = LWTextSecondary
                )
                Text(
                    text = if (monetizationState.billingAvailable) "Billing: Available" else "Billing: Unavailable — Free mode active",
                    style = MaterialTheme.typography.bodySmall,
                    color = LWTextSecondary
                )
            }
        }
        BannerAdPlaceholder(state = monetizationState)
    }
}
