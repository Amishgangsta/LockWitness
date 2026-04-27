package com.lockwitness.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lockwitness.app.data.SettingsRepository
import com.lockwitness.app.data.SettingsState
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    val repository = remember(context) { SettingsRepository.create(context) }
    val settings by repository.settings.collectAsState(initial = SettingsState.Defaults)
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Configure LockWitness feature defaults. Capture and alert actions are not implemented in this phase.",
            style = MaterialTheme.typography.bodyMedium
        )

        SettingsToggleRow(
            title = "Master monitoring",
            description = "Default: off",
            checked = settings.masterMonitoringEnabled,
            onCheckedChange = { enabled ->
                scope.launch { repository.setMasterMonitoringEnabled(enabled) }
            }
        )
        SettingsToggleRow(
            title = "Photo capture",
            description = "Default: on",
            checked = settings.photoCaptureEnabled,
            onCheckedChange = { enabled ->
                scope.launch { repository.setPhotoCaptureEnabled(enabled) }
            }
        )
        SettingsToggleRow(
            title = "Video capture",
            description = "Default: off",
            checked = settings.videoCaptureEnabled,
            onCheckedChange = { enabled ->
                scope.launch { repository.setVideoCaptureEnabled(enabled) }
            }
        )

        SettingsSectionCard {
            Text(
                text = "Video duration",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Default: 5 seconds",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingsState.AllowedVideoDurations.forEach { duration ->
                    FilterChip(
                        selected = settings.videoDurationSeconds == duration,
                        onClick = {
                            scope.launch { repository.setVideoDurationSeconds(duration) }
                        },
                        label = { Text("${duration}s") }
                    )
                }
            }
        }

        SettingsToggleRow(
            title = "GPS/location capture",
            description = "Permission status: not requested",
            checked = settings.locationCaptureEnabled,
            onCheckedChange = { enabled ->
                scope.launch { repository.setLocationCaptureEnabled(enabled) }
            }
        )
        SettingsToggleRow(
            title = "Local timeline",
            description = "Default: on",
            checked = settings.localTimelineEnabled,
            onCheckedChange = { enabled ->
                scope.launch { repository.setLocalTimelineEnabled(enabled) }
            }
        )
        SettingsToggleRow(
            title = "Email alert",
            description = "Permission/configuration status: not configured",
            checked = settings.emailAlertEnabled,
            onCheckedChange = { enabled ->
                scope.launch { repository.setEmailAlertEnabled(enabled) }
            }
        )
        SettingsToggleRow(
            title = "Share alert",
            description = "Permission status: not requested",
            checked = settings.shareAlertEnabled,
            onCheckedChange = { enabled ->
                scope.launch { repository.setShareAlertEnabled(enabled) }
            }
        )
        SettingsToggleRow(
            title = "Evidence hashing",
            description = "Default: on",
            checked = settings.evidenceHashingEnabled,
            onCheckedChange = { enabled ->
                scope.launch { repository.setEvidenceHashingEnabled(enabled) }
            }
        )

        SettingsSectionCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Manual export",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Placeholder only; export logic is not implemented.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Button(
                    onClick = {},
                    enabled = false
                ) {
                    Text("Export")
                }
            }
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text("Status: unavailable in Phase 2") }
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingsSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun SettingsSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}
