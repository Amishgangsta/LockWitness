package com.lockwitness.app.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ShieldMoon
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lockwitness.app.data.SettingsRepository
import com.lockwitness.app.data.SettingsState
import com.lockwitness.app.data.incident.LockWitnessDatabase
import com.lockwitness.app.data.incident.SecurityIncidentRepository
import com.lockwitness.app.monetization.MonetizationRepository
import com.lockwitness.app.monetization.MonetizationState
import com.lockwitness.app.ui.components.ForensicCard
import com.lockwitness.app.ui.components.ForensicDivider
import com.lockwitness.app.ui.components.SectionEyebrow
import com.lockwitness.app.ui.components.StatusPill
import com.lockwitness.app.ui.theme.CardSurface
import com.lockwitness.app.ui.theme.CautionAmber
import com.lockwitness.app.ui.theme.DestructiveRed
import com.lockwitness.app.ui.theme.GraphiteBg
import com.lockwitness.app.ui.theme.ProOrange
import com.lockwitness.app.ui.theme.SurfaceRaised
import com.lockwitness.app.ui.theme.TextPrimary
import com.lockwitness.app.ui.theme.TextSecondary
import com.lockwitness.app.ui.theme.VerifiedGreen
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
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSetup: () -> Unit = {}
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

    LaunchedEffect(Unit) {
        monetizationRepository.startTrialIfNotStarted()
    }

    val recent = incidents.firstOrNull()
    val uiState = DashboardUiState(
        monitoringEnabled = settings.masterMonitoringEnabled,
        incidentCount = incidents.size,
        photoCount = incidents.count { it.photoStatus == "SUCCESS" },
        videoCount = incidents.count { it.videoStatus == "SUCCESS" },
        locationCount = incidents.count { it.locationStatus == "SUCCESS" },
        hashingEnabled = settings.evidenceHashingEnabled,
        recentIncidentTimestamp = recent?.timestamp,
        recentIncidentCaptured = recent?.let { it.photoStatus == "SUCCESS" || it.videoStatus == "SUCCESS" } ?: false
    )

    DashboardContent(
        contentPadding = contentPadding,
        state = uiState,
        monetizationState = monetizationState,
        onNavigateToUpgrade = onNavigateToUpgrade,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToSetup = onNavigateToSetup
    )
}

@Composable
internal fun DashboardContent(
    contentPadding: PaddingValues,
    state: DashboardUiState,
    monetizationState: MonetizationState,
    onNavigateToUpgrade: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSetup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GraphiteBg)
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        DashboardTopBar()
        HeroCard(state = state)
        StatusCard(state = state)
        EvidenceModulesGrid(state = state, isPro = monetizationState.isPro)
        IntegrityCard(state = state)
        PlanCard(monetizationState = monetizationState, onNavigateToUpgrade = onNavigateToUpgrade)
    }
}

@Composable
private fun DashboardTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceRaised, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Outlined.ShieldMoon, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(20.dp))
            Text(
                text = "Lock Witness",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun HeroCard(state: DashboardUiState) {
    val armed = state.monitoringEnabled
    val pillColor = if (armed) VerifiedGreen else CautionAmber

    ForensicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    SectionEyebrow("Digital Witness")
                    Text(
                        text = "Owner-controlled failed-unlock\nevidence monitor",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                }
                Icon(Icons.Outlined.VerifiedUser, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(28.dp))
            }
            StatusPill(text = if (armed) "ARMED" else "PAUSED", color = pillColor, dotColor = pillColor)
        }
    }
}

@Composable
private fun StatusCard(state: DashboardUiState) {
    ForensicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            StatusMetaRow(
                icon = Icons.Outlined.ShieldMoon,
                label = "Monitoring",
                value = if (state.monitoringEnabled) "Armed" else "Paused",
                valueColor = if (state.monitoringEnabled) VerifiedGreen else CautionAmber
            )
            ForensicDivider(modifier = Modifier.padding(vertical = 8.dp))
            StatusMetaRow(
                icon = Icons.Outlined.Schedule,
                label = "Last incident",
                value = state.recentIncidentTimestamp?.let { relativeTime(it) } ?: "No incidents recorded"
            )
            ForensicDivider(modifier = Modifier.padding(vertical = 8.dp))
            StatusMetaRow(
                icon = Icons.Outlined.History,
                label = "Evidence count",
                value = "${state.incidentCount} incident${if (state.incidentCount != 1) "s" else ""}"
            )
        }
    }
}

@Composable
private fun StatusMetaRow(icon: ImageVector, label: String, value: String, valueColor: Color = TextPrimary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.weight(1f))
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = valueColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EvidenceModulesGrid(state: DashboardUiState, isPro: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionEyebrow("Evidence Modules")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EvidenceMiniCard(label = "Photo", icon = Icons.Outlined.CameraAlt, status = EvidenceTileStatus.ACTIVE, count = state.photoCount, modifier = Modifier.weight(1f))
            EvidenceMiniCard(label = "Video", icon = Icons.Outlined.Videocam, status = if (isPro) EvidenceTileStatus.ACTIVE else EvidenceTileStatus.LOCKED, count = if (isPro) state.videoCount else 0, modifier = Modifier.weight(1f))
            EvidenceMiniCard(label = "Location", icon = Icons.Outlined.LocationOn, status = if (isPro) EvidenceTileStatus.ACTIVE else EvidenceTileStatus.LOCKED, count = if (isPro) state.locationCount else 0, modifier = Modifier.weight(1f))
            EvidenceMiniCard(label = "Export", icon = Icons.Outlined.Archive, status = if (state.incidentCount > 0) EvidenceTileStatus.ACTIVE else EvidenceTileStatus.EMPTY, count = null, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun EvidenceMiniCard(label: String, icon: ImageVector, status: EvidenceTileStatus, count: Int?, modifier: Modifier = Modifier) {
    val isLocked = status == EvidenceTileStatus.LOCKED
    val iconTint = when (status) {
        EvidenceTileStatus.ACTIVE -> TextPrimary
        EvidenceTileStatus.LOCKED, EvidenceTileStatus.EMPTY -> TextSecondary.copy(alpha = 0.5f)
    }
    val statusColor = if (isLocked) ProOrange else VerifiedGreen
    val statusText = when (status) {
        EvidenceTileStatus.ACTIVE -> "Ready"
        EvidenceTileStatus.LOCKED -> "Pro"
        EvidenceTileStatus.EMPTY -> "Idle"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardSurface)
            .padding(horizontal = 6.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1)
            Text(
                text = if (count != null && count > 0) "$count" else statusText,
                style = MaterialTheme.typography.labelSmall,
                color = if (count != null && count > 0) TextPrimary else statusColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun IntegrityCard(state: DashboardUiState) {
    ForensicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            SectionEyebrow("Evidence Integrity")
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.Fingerprint, contentDescription = null, tint = if (state.hashingEnabled) VerifiedGreen else TextSecondary, modifier = Modifier.size(18.dp))
                Text("SHA-256 hashing", style = MaterialTheme.typography.bodySmall, color = TextPrimary, modifier = Modifier.weight(1f))
                Text(if (state.hashingEnabled) "Active" else "Disabled", style = MaterialTheme.typography.labelSmall, color = if (state.hashingEnabled) VerifiedGreen else CautionAmber, fontWeight = FontWeight.SemiBold)
            }
            ForensicDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(18.dp))
                Text("Local-only storage", style = MaterialTheme.typography.bodySmall, color = TextPrimary, modifier = Modifier.weight(1f))
                Text("Secure", style = MaterialTheme.typography.labelSmall, color = VerifiedGreen, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PlanCard(monetizationState: MonetizationState, onNavigateToUpgrade: () -> Unit) {
    ForensicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when {
                monetizationState.isPro -> {
                    Icon(Icons.Outlined.VerifiedUser, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(22.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pro Active", style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Full forensic evidence suite enabled", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    StatusPill(text = "Pro", color = ProOrange)
                }
                monetizationState.trialExpired -> {
                    Icon(Icons.Outlined.WorkspacePremium, contentDescription = null, tint = DestructiveRed, modifier = Modifier.size(24.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Trial ended", style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Purchase to unlock video, GPS, and full history", style = MaterialTheme.typography.labelSmall, color = DestructiveRed)
                    }
                    Button(
                        onClick = onNavigateToUpgrade,
                        colors = ButtonDefaults.buttonColors(containerColor = ProOrange, contentColor = TextPrimary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Upgrade", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
                monetizationState.isInTrial -> {
                    Icon(Icons.Outlined.WorkspacePremium, contentDescription = null, tint = ProOrange, modifier = Modifier.size(24.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Trial — ${monetizationState.trialDaysRemaining} days left", style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Upgrade for video, GPS, and full history", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Button(
                        onClick = onNavigateToUpgrade,
                        colors = ButtonDefaults.buttonColors(containerColor = ProOrange, contentColor = TextPrimary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Upgrade", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    Icon(Icons.Outlined.WorkspacePremium, contentDescription = null, tint = ProOrange, modifier = Modifier.size(24.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Pro: video + full history", style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("GPS snapshots, full history, advanced export", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Button(
                        onClick = onNavigateToUpgrade,
                        colors = ButtonDefaults.buttonColors(containerColor = ProOrange, contentColor = TextPrimary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Upgrade", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
internal fun PlaceholderScreen(title: String, body: String, contentPadding: PaddingValues) {
    Column(
        modifier = Modifier.fillMaxSize().padding(contentPadding).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        Text(body, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
    }
}

private fun relativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 172_800_000 -> "Yesterday"
        else -> "${diff / 86_400_000}d ago"
    }
}

private fun formatTimestamp(timestamp: Long): String =
    SimpleDateFormat("MMM d 'at' h:mm a", Locale.US).format(Date(timestamp))

@Preview(showBackground = true, backgroundColor = 0xFF080A0D)
@Composable
private fun PreviewDashboardArmed() {
    DashboardContent(
        contentPadding = PaddingValues(0.dp),
        state = DashboardUiState(monitoringEnabled = true, incidentCount = 14, photoCount = 14, hashingEnabled = true, recentIncidentTimestamp = System.currentTimeMillis() - 7_200_000L, recentIncidentCaptured = true),
        monetizationState = MonetizationState.Pro,
        onNavigateToUpgrade = {}, onNavigateToSettings = {}, onNavigateToSetup = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF080A0D)
@Composable
private fun PreviewDashboardFree() {
    DashboardContent(
        contentPadding = PaddingValues(0.dp),
        state = DashboardUiState(monitoringEnabled = false, incidentCount = 3, photoCount = 3),
        monetizationState = MonetizationState.Free,
        onNavigateToUpgrade = {}, onNavigateToSettings = {}, onNavigateToSetup = {}
    )
}
