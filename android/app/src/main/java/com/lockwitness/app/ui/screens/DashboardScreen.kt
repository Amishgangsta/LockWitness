package com.lockwitness.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lockwitness.app.data.SettingsRepository
import com.lockwitness.app.data.SettingsState
import com.lockwitness.app.data.incident.LockWitnessDatabase
import com.lockwitness.app.data.incident.SecurityIncidentRepository
import com.lockwitness.app.monetization.BannerAdPlaceholder
import com.lockwitness.app.monetization.MonetizationRepository
import com.lockwitness.app.monetization.MonetizationState
import com.lockwitness.app.ui.components.ForensicCard
import com.lockwitness.app.ui.components.SectionEyebrow
import com.lockwitness.app.ui.components.StatusPill
import com.lockwitness.app.ui.theme.LWAccentRed
import com.lockwitness.app.ui.theme.LWChrome
import com.lockwitness.app.ui.theme.LWSuccessGreen
import com.lockwitness.app.ui.theme.LWTextPrimary
import com.lockwitness.app.ui.theme.LWTextSecondary
import com.lockwitness.app.ui.theme.LockWitnessBackground
import com.lockwitness.app.ui.theme.LockWitnessBorder
import com.lockwitness.app.ui.theme.LockWitnessPrimary
import com.lockwitness.app.ui.theme.LockWitnessPrimaryBright
import com.lockwitness.app.ui.theme.LockWitnessSurfaceRaised
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class EvidenceTileStatus { ACTIVE, LOCKED, EMPTY }
enum class ExportState { IDLE, EXPORTING, DONE }

data class DashboardUiState(
    val monitoringEnabled: Boolean = false,
    val incidentCount: Int = 0,
    val photoCount: Int = 0,
    val videoCount: Int = 0,
    val locationCount: Int = 0,
    val hashingEnabled: Boolean = false,
    val exportState: ExportState = ExportState.IDLE,
    val recentIncidentTimestamp: Long? = null,
    val recentIncidentCaptured: Boolean = false
)

@Composable
fun DashboardScreen(
    contentPadding: PaddingValues,
    onNavigateToUpgrade: () -> Unit,
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val settingsRepository = remember(context) { SettingsRepository.create(context) }
    val incidentRepository = remember(context) {
        SecurityIncidentRepository(LockWitnessDatabase.getInstance(context).securityIncidentDao())
    }
    val monetizationRepository = remember(context) { MonetizationRepository.create(context) }

    val settings by settingsRepository.settings.collectAsState(initial = SettingsState.Defaults)
    val incidents by incidentRepository.getAllOrderedByTimestampDesc().collectAsState(initial = emptyList())
    val monetizationState by monetizationRepository.state.collectAsState(initial = MonetizationState.Free)

    val recent = incidents.firstOrNull()
    val uiState = DashboardUiState(
        monitoringEnabled = settings.masterMonitoringEnabled,
        incidentCount = incidents.size,
        photoCount = incidents.count { it.photoStatus == "SUCCESS" },
        videoCount = incidents.count { it.videoStatus == "SUCCESS" },
        locationCount = incidents.count { it.locationStatus == "SUCCESS" },
        hashingEnabled = settings.evidenceHashingEnabled,
        recentIncidentTimestamp = recent?.timestamp,
        recentIncidentCaptured = recent?.let {
            it.photoStatus == "SUCCESS" || it.videoStatus == "SUCCESS"
        } ?: false
    )

    DashboardContent(
        contentPadding = contentPadding,
        state = uiState,
        monetizationState = monetizationState,
        onNavigateToUpgrade = onNavigateToUpgrade,
        onNavigateToSettings = onNavigateToSettings
    )
}

@Composable
internal fun DashboardContent(
    contentPadding: PaddingValues,
    state: DashboardUiState,
    monetizationState: MonetizationState,
    onNavigateToUpgrade: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(LockWitnessBackground, Color(0xFF0F0F0F), LockWitnessBackground)
                )
            )
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HeaderCard(monitoringEnabled = state.monitoringEnabled)
        MonitoringStatusCard(enabled = state.monitoringEnabled)
        IncidentSummaryCard(state = state, isPro = monetizationState.isPro)
        EvidenceIntegrityCard(hashingEnabled = state.hashingEnabled, exportState = state.exportState)
        RecentIncidentCard(
            timestamp = state.recentIncidentTimestamp,
            captured = state.recentIncidentCaptured
        )
        if (monetizationState.isPro) {
            ProToolsCard(onNavigateToSettings = onNavigateToSettings)
        } else {
            UpgradePromptCard(onNavigateToUpgrade = onNavigateToUpgrade)
        }
        Spacer(modifier = Modifier.height(4.dp))
        BannerAdPlaceholder(state = monetizationState)
    }
}

@Composable
private fun HeaderCard(monitoringEnabled: Boolean) {
    ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = null,
                    tint = LockWitnessPrimary,
                    modifier = Modifier.padding(4.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lock Witness",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = LWTextPrimary
                    )
                    Text(
                        text = "Owner-controlled failed-unlock evidence monitor",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LWTextSecondary
                    )
                }
            }
            StatusPill(
                text = if (monitoringEnabled) "Monitoring Active" else "Monitoring Paused",
                dotColor = if (monitoringEnabled) LWSuccessGreen else null
            )
        }
    }
}

@Composable
private fun MonitoringStatusCard(enabled: Boolean) {
    ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = enabled) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.VerifiedUser,
                contentDescription = null,
                tint = if (enabled) LWSuccessGreen else LWTextSecondary
            )
            Column(modifier = Modifier.weight(1f)) {
                SectionEyebrow("Monitoring")
                Text(
                    text = if (enabled) "Failed-unlock surveillance is armed" else "Monitoring is currently paused",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LWTextPrimary
                )
            }
            StatusPill(
                text = if (enabled) "Active" else "Paused",
                dotColor = if (enabled) LWSuccessGreen else null
            )
        }
    }
}

@Composable
private fun IncidentSummaryCard(state: DashboardUiState, isPro: Boolean) {
    ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionEyebrow("Evidence Summary")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.incidentCount.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    color = LockWitnessPrimary,
                    fontWeight = FontWeight.Bold
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Recorded Incidents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = LWTextPrimary
                    )
                    Text(
                        text = "Total failed unlock attempts",
                        style = MaterialTheme.typography.bodySmall,
                        color = LWTextSecondary
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = LockWitnessPrimary.copy(alpha = 0.3f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EvidenceMiniCard(
                    label = "Photo Evidence",
                    value = state.photoCount.toString(),
                    subLabel = if (state.photoCount > 0) "Captured" else "None Yet",
                    icon = Icons.Outlined.CameraAlt,
                    status = if (state.photoCount > 0) EvidenceTileStatus.ACTIVE else EvidenceTileStatus.EMPTY,
                    modifier = Modifier.weight(1f)
                )
                EvidenceMiniCard(
                    label = "Video Evidence",
                    value = if (isPro) state.videoCount.toString() else "Pro",
                    subLabel = if (isPro) {
                        if (state.videoCount > 0) "Captured" else "None Yet"
                    } else {
                        "Upgrade for Video Evidence"
                    },
                    icon = Icons.Outlined.Videocam,
                    status = if (!isPro) EvidenceTileStatus.LOCKED
                    else if (state.videoCount > 0) EvidenceTileStatus.ACTIVE
                    else EvidenceTileStatus.EMPTY,
                    modifier = Modifier.weight(1f)
                )
                EvidenceMiniCard(
                    label = "Location",
                    value = if (isPro) state.locationCount.toString() else "Pro",
                    subLabel = if (isPro) {
                        if (state.locationCount > 0) "Snapshot" else "None Yet"
                    } else {
                        "Upgrade for Location Snapshots"
                    },
                    icon = Icons.Outlined.Place,
                    status = if (!isPro) EvidenceTileStatus.LOCKED
                    else if (state.locationCount > 0) EvidenceTileStatus.ACTIVE
                    else EvidenceTileStatus.EMPTY,
                    modifier = Modifier.weight(1f)
                )
            }

            if (!isPro && state.incidentCount > 0) {
                Text(
                    text = "Free plan shows last 10 — Pro unlocks full forensic history.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LWTextSecondary
                )
            }
        }
    }
}

@Composable
private fun EvidenceMiniCard(
    label: String,
    value: String,
    subLabel: String,
    icon: ImageVector,
    status: EvidenceTileStatus,
    modifier: Modifier = Modifier
) {
    val dotColor = when (status) {
        EvidenceTileStatus.ACTIVE -> LWSuccessGreen
        EvidenceTileStatus.LOCKED -> LockWitnessPrimary
        EvidenceTileStatus.EMPTY -> LWChrome.copy(alpha = 0.5f)
    }
    val subLabelColor = if (status == EvidenceTileStatus.LOCKED) LWTextSecondary else LockWitnessPrimary

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LockWitnessSurfaceRaised),
        border = BorderStroke(1.dp, LockWitnessBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = LockWitnessPrimary)
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = LockWitnessPrimaryBright,
                fontWeight = FontWeight.Bold
            )
            Text(text = subLabel, style = MaterialTheme.typography.bodySmall, color = subLabelColor)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = LWTextSecondary)
        }
    }
}

@Composable
private fun EvidenceIntegrityCard(hashingEnabled: Boolean, exportState: ExportState) {
    ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            SectionEyebrow("Evidence Integrity")
            Spacer(modifier = Modifier.height(12.dp))
            IntegrityRow(
                icon = Icons.Outlined.Shield,
                label = "SHA-256 Hashing",
                trailingText = if (hashingEnabled) "Enabled" else "Enable",
                trailingColor = if (hashingEnabled) LWSuccessGreen else LWAccentRed
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = LockWitnessBorder)
            IntegrityRow(
                icon = Icons.Outlined.Lock,
                label = "Local-Only Storage",
                sublabel = "All evidence stored on this device",
                trailingText = "Secure",
                trailingColor = LWSuccessGreen
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = LockWitnessBorder)
            IntegrityRow(
                icon = Icons.Outlined.Download,
                label = "Manual Export",
                sublabel = when (exportState) {
                    ExportState.IDLE -> "Export evidence when needed"
                    ExportState.EXPORTING -> "Export in progress…"
                    ExportState.DONE -> "Export complete"
                },
                trailingText = when (exportState) {
                    ExportState.IDLE -> null
                    ExportState.EXPORTING -> "Exporting"
                    ExportState.DONE -> "Done"
                },
                trailingColor = if (exportState == ExportState.DONE) LWSuccessGreen else LWChrome
            )
        }
    }
}

@Composable
private fun IntegrityRow(
    icon: ImageVector,
    label: String,
    sublabel: String? = null,
    trailingText: String? = null,
    trailingColor: Color = LockWitnessPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = LWTextSecondary)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = LWTextPrimary)
            if (sublabel != null) {
                Text(text = sublabel, style = MaterialTheme.typography.bodySmall, color = LWTextSecondary)
            }
        }
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.labelMedium,
                color = trailingColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun RecentIncidentCard(timestamp: Long?, captured: Boolean) {
    val hasIncident = timestamp != null
    val dateStr = remember(timestamp) {
        timestamp?.let {
            SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(it))
        }
    }

    ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = false) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (hasIncident) Icons.Outlined.Error else Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = if (hasIncident) LWAccentRed else LWSuccessGreen
            )
            Column(modifier = Modifier.weight(1f)) {
                SectionEyebrow(if (hasIncident) "Recent Incident" else "No Recent Incident")
                if (hasIncident && dateStr != null) {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = LWTextSecondary
                    )
                    Text(
                        text = "Failed unlock attempt detected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LWTextPrimary
                    )
                } else {
                    Text(
                        text = "No failed unlock attempts recorded",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LWTextSecondary
                    )
                }
            }
            if (hasIncident && captured) {
                StatusPill(text = "Captured")
            }
        }
    }
}

@Composable
private fun UpgradePromptCard(onNavigateToUpgrade: () -> Unit) {
    ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionEyebrow("Unlock Pro Evidence Tools")
            Text(
                text = "Unlock full forensic history, video evidence, GPS snapshots and advanced protections.",
                style = MaterialTheme.typography.bodyMedium,
                color = LWTextSecondary
            )
            Button(onClick = onNavigateToUpgrade, modifier = Modifier.fillMaxWidth()) {
                Text("Upgrade to Pro")
            }
        }
    }
}

@Composable
private fun ProToolsCard(onNavigateToSettings: () -> Unit) {
    ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.VerifiedUser,
                contentDescription = null,
                tint = LWSuccessGreen
            )
            Column(modifier = Modifier.weight(1f)) {
                SectionEyebrow("Pro Active")
                Text(
                    text = "Full forensic evidence suite enabled",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LWTextSecondary
                )
            }
            TextButton(onClick = onNavigateToSettings) {
                Text("Access Pro Tools Here", color = LockWitnessPrimary)
            }
        }
    }
}

@Composable
internal fun PlaceholderScreen(
    title: String,
    body: String,
    contentPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Text(text = body, style = MaterialTheme.typography.bodyLarge)
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "Free · No Incidents", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PreviewFreeNoIncidents() {
    DashboardContent(
        contentPadding = PaddingValues(0.dp),
        state = DashboardUiState(monitoringEnabled = true),
        monetizationState = MonetizationState.Free,
        onNavigateToUpgrade = {},
        onNavigateToSettings = {}
    )
}

@Preview(name = "Free · With Incidents", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PreviewFreeWithIncidents() {
    DashboardContent(
        contentPadding = PaddingValues(0.dp),
        state = DashboardUiState(
            monitoringEnabled = true,
            incidentCount = 7,
            photoCount = 7,
            recentIncidentTimestamp = 1_716_000_000_000L,
            recentIncidentCaptured = true
        ),
        monetizationState = MonetizationState.Free,
        onNavigateToUpgrade = {},
        onNavigateToSettings = {}
    )
}

@Preview(name = "Pro · With Incidents", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PreviewProWithIncidents() {
    DashboardContent(
        contentPadding = PaddingValues(0.dp),
        state = DashboardUiState(
            monitoringEnabled = true,
            incidentCount = 23,
            photoCount = 23,
            videoCount = 18,
            locationCount = 21,
            hashingEnabled = true,
            recentIncidentTimestamp = 1_716_060_000_000L,
            recentIncidentCaptured = true
        ),
        monetizationState = MonetizationState.Pro,
        onNavigateToUpgrade = {},
        onNavigateToSettings = {}
    )
}

@Preview(name = "Monitoring Disabled", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PreviewMonitoringDisabled() {
    DashboardContent(
        contentPadding = PaddingValues(0.dp),
        state = DashboardUiState(monitoringEnabled = false, incidentCount = 3, photoCount = 3),
        monetizationState = MonetizationState.Free,
        onNavigateToUpgrade = {},
        onNavigateToSettings = {}
    )
}

@Preview(name = "Export In Progress", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PreviewExportInProgress() {
    DashboardContent(
        contentPadding = PaddingValues(0.dp),
        state = DashboardUiState(
            monitoringEnabled = true,
            incidentCount = 5,
            photoCount = 5,
            videoCount = 4,
            locationCount = 5,
            hashingEnabled = true,
            exportState = ExportState.EXPORTING,
            recentIncidentTimestamp = 1_715_900_000_000L,
            recentIncidentCaptured = true
        ),
        monetizationState = MonetizationState.Pro,
        onNavigateToUpgrade = {},
        onNavigateToSettings = {}
    )
}
