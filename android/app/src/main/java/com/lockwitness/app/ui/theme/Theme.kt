package com.lockwitness.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// LockWitness Orange / Amber Forensic Variant
// Version: 1.0.0
// Build: LOCKWITNESS-UI-ORANGE-1
// Created: 2026-05-20
// Author: Randy D. Vickers / ChatGPT
// Platform: Android Jetpack Compose
// Hardened: Dark Material 3 forensic theme

private val DarkForensicOrangeScheme: ColorScheme = darkColorScheme(
    primary = LockWitnessPrimary,
    onPrimary = LockWitnessOnPrimary,
    primaryContainer = LockWitnessPrimaryDark,
    onPrimaryContainer = LockWitnessTextPrimary,

    secondary = LockWitnessSecondary,
    onSecondary = LockWitnessOnDark,
    secondaryContainer = LockWitnessSurfaceVariant,
    onSecondaryContainer = LockWitnessTextPrimary,

    background = LWBackground,
    onBackground = LockWitnessTextPrimary,

    surface = LWPanel,
    onSurface = LockWitnessTextPrimary,

    surfaceVariant = LockWitnessSurfaceVariant,
    onSurfaceVariant = LockWitnessTextSecondary,

    error = LockWitnessDanger,
    onError = LockWitnessTextPrimary,

    outline = LockWitnessBorder,
    outlineVariant = LockWitnessDivider
)

@Composable
fun LockWitnessTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkForensicOrangeScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
