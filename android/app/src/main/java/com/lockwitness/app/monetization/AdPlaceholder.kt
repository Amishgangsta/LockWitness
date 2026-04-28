package com.lockwitness.app.monetization

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

@Composable
fun BannerAdPlaceholder(
    state: MonetizationState,
    modifier: Modifier = Modifier
) {
    if (!ProFeatureGate().shouldShowAds(state)) {
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Ad placeholder",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "Test banner ID: $TEST_BANNER_AD_UNIT_ID",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
