package com.lockwitness.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme: ColorScheme = lightColorScheme(
    primary = LockWitnessPrimary,
    secondary = LockWitnessSecondary,
    background = LockWitnessBackground,
    surface = LockWitnessSurface
)

@Composable
fun LockWitnessTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
