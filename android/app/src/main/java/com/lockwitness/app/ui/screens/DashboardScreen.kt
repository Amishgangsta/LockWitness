package com.lockwitness.app.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lockwitness.app.data.SettingsRepository
import com.lockwitness.app.data.SettingsState
import com.lockwitness.app.data.incident.LockWitnessDatabase
import com.lockwitness.app.data.incident.SecurityIncident
import com.lockwitness.app.data.incident.SecurityIncidentRepository
import com.lockwitness.app.monetization.BannerAdPlaceholder
import com.lockwitness.app.monetization.MonetizationRepository
import com.lockwitness.app.monetization.MonetizationState
import com.lockwitness.app.ui.components.ForensicCard
import com.lockwitness.app.ui.components.SectionEyebrow
import com.lockwitness.app.ui.components.StatusPill
import com.lockwitness.app.ui.theme.LockWitnessBackground
import com.lockwitness.app.ui.theme.LockWitnessBorder
import com.lockwitness.app.ui.theme.LockWitnessPrimary
import com.lockwitness.app.ui.theme.LockWitnessPrimaryBright
import com.lockwitness.app.ui.theme.LockWitnessSurfaceRaised
import com.lockwitness.app.ui.theme.LockWitnessTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    contentPadding: PaddingValues,
    onNavigateToUpgrade: () -> Unit
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        LockWitnessBackground,
                        Color(0xFF0F0F0F),
                        LockWitnessBackground
                    )
                )
            )
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        ForensicCard(
            modifier = Modifier.fillMaxWidth(),
            elevated = true
        ) {
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
                            text = "Digital Witness",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Owner-controlled failed-unlock evidence monitor",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LockWitnessTextSecondary
                        )
                    }
                }
                StatusPill(
                    text = if (settings.masterMonitoringEnabled) "Monitoring Active" else "Monitoring Paused"
                )
            }
        }

        MonitoringStatusCard(enabled = settings.masterMonitoringEnabled)

        IncidentSummaryCard(incidentCount = incidents.size, isPro = monetizationState.isPro)

        EvidenceIntegrityCard(hashingEnabled = settings.evidenceHashingEnabled)

        incidents.firstOrNull()?.let { recent ->
            RecentIncidentCard(incident = recent)
        }

        if (!monetizationState.isPro && incidents.isNotEmpty()) {
            UpgradePromptCard(onNavigateToUpgrade = onNavigateToUpgrade)
        }

        Spacer(modifier = Modifier.height(4.dp))

        BannerAdPlaceholder(state = monetizationState)
    }
}

@Composable
private fun MonitoringStatusCard(enabled: Boolean) {
    ForensicCard(
        modifier = Modifier.fillMaxWidth(),
        elevated = enabled
    ) {
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
                tint = LockWitnessPrimary
            )
            Column(modifier = Modifier.weight(1f)) {
                SectionEyebrow("Monitoring")
                Text(
                    text = if (enabled) "Failed-unlock surveillance is armed" else "Monitoring is currently paused",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            StatusPill(text = if (enabled) "Active" else "Paused")
        }
    }
}

@Composable
private fun IncidentSummaryCard(incidentCount: Int, isPro: Boolean) {
    ForensicCard(
        modifier = Modifier.fillMaxWidth(),
        elevated = true
    ) {
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
                    text = incidentCount.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    color = LockWitnessPrimary,
                    fontWeight = FontWeight.Bold
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Recorded Incidents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Total failed unlock attempts",
                        style = MaterialTheme.typography.bodySmall,
                        color = LockWitnessTextSecondary
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
                    value = incidentCount.toString(),
                    subLabel = "Captured",
                    icon = Icons.Outlined.CameraAlt,
                    modifier = Modifier.weight(1f)
                )
                EvidenceMiniCard(
                    label = "Video Evidence",
                    value = if (isPro) incidentCount.toString() else "Pro",
                    subLabel = if (isPro) "Captured" else "Locked",
                    icon = Icons.Outlined.Videocam,
                    modifier = Modifier.weight(1f)
                )
                EvidenceMiniCard(
                    label = "Location",
                    value = incidentCount.toString(),
                    subLabel = "Snapshot",
                    icon = Icons.Outlined.Place,
                    modifier = Modifier.weight(1f)
                )
            }

            if (!isPro && incidentCount > 0) {
                Text(
                    text = "Free plan shows last 10 — Pro unlocks full forensic history.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LockWitnessTextSecondary
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
    modifier: Modifier = Modifier
) {
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
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = LockWitnessPrimary
                )
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = LockWitnessPrimary,
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = LockWitnessPrimaryBright,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.bodySmall,
                color = LockWitnessPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = LockWitnessTextSecondary
            )
        }
    }
}

@Composable
private fun EvidenceIntegrityCard(hashingEnabled: Boolean) {
    ForensicCard(
        modifier = Modifier.fillMaxWidth(),
        elevated = false
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            SectionEyebrow("Evidence Integrity")
            Spacer(modifier = Modifier.height(12.dp))

            IntegrityRow(
                icon = Icons.Outlined.Shield,
                label = "SHA-256 Hashing",
                trailingText = if (hashingEnabled) "Enabled" else "Disabled",
                trailingColor = if (hashingEnabled) LockWitnessPrimary else LockWitnessTextSecondary
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = LockWitnessBorder
            )
            IntegrityRow(
                icon = Icons.Outlined.Lock,
                label = "Local-Only Storage",
                sublabel = "All evidence stored on this device"
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = LockWitnessBorder
            )
            IntegrityRow(
                icon = Icons.Outlined.Download,
                label = "Manual Export",
                sublabel = "Export evidence when needed"
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
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LockWitnessTextSecondary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (sublabel != null) {
                Text(
                    text = sublabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = LockWitnessTextSecondary
                )
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
private fun RecentIncidentCard(incident: SecurityIncident) {
    val dateStr = remember(incident.timestamp) {
        SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
            .format(Date(incident.timestamp))
    }
    val isCaptured = incident.photoStatus == "SUCCESS" || incident.videoStatus == "SUCCESS"

    ForensicCard(
        modifier = Modifier.fillMaxWidth(),
        elevated = false
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Error,
                contentDescription = null,
                tint = LockWitnessPrimary
            )
            Column(modifier = Modifier.weight(1f)) {
                SectionEyebrow("Recent Incident")
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = LockWitnessTextSecondary
                )
                Text(
                    text = "Failed unlock attempt detected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (isCaptured) {
                StatusPill(text = "Captured")
            }
        }
    }
}

@Composable
private fun UpgradePromptCard(onNavigateToUpgrade: () -> Unit) {
    ForensicCard(
        modifier = Modifier.fillMaxWidth(),
        elevated = true
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionEyebrow("Unlock Pro Evidence Tools")
            Text(
                text = "Unlock full forensic history, video evidence, GPS snapshots and advanced protections.",
                style = MaterialTheme.typography.bodyMedium,
                color = LockWitnessTextSecondary
            )
            Button(
                onClick = onNavigateToUpgrade,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upgrade")
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
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
