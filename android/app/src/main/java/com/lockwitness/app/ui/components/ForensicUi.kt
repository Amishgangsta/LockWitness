package com.lockwitness.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lockwitness.app.ui.theme.LockWitnessBorder
import com.lockwitness.app.ui.theme.LockWitnessPrimary
import com.lockwitness.app.ui.theme.LockWitnessSurface
import com.lockwitness.app.ui.theme.LockWitnessSurfaceRaised
import com.lockwitness.app.ui.theme.LockWitnessTextSecondary

// LockWitness Orange / Amber Forensic Variant
// Version: 1.0.0
// Build: LOCKWITNESS-UI-ORANGE-1
// Created: 2026-05-20
// Author: Randy D. Vickers / ChatGPT
// Platform: Android Jetpack Compose
// Hardened: Reusable UI-only components

@Composable
fun ForensicCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (elevated) LockWitnessSurfaceRaised else LockWitnessSurface
        ),
        border = BorderStroke(1.dp, LockWitnessBorder)
    ) {
        content()
    }
}

@Composable
fun StatusPill(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = LockWitnessPrimary.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, LockWitnessPrimary.copy(alpha = 0.75f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = LockWitnessPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SectionEyebrow(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = LockWitnessPrimary,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    )
}

@Composable
fun MutedText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = LockWitnessTextSecondary
    )
}
