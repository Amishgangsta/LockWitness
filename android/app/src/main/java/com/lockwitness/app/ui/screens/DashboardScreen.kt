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
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.lockwitness.app.R
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
import com.lockwitness.app.ui.theme.LWBackground
import com.lockwitness.app.ui.theme.LWChrome
import com.lockwitness.app.ui.theme.LWPanel
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
            .background(LWBackground)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppHeader()
        HeroCard(monitoringEnabled = state.monitoringEnabled)
        IncidentSummaryCard(state = state, isPro = monetizationState.isPro)
        EvidenceIntegrityCard(
            hashingEnabled = state.hashingEnabled,
            exportState = state.exportState,
            isPro = monetizationState.isPro
        )
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
private fun AppHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.shield_logo),
            contentDescription = "LockWitness Shield",
            modifier = Modifier.size(36.dp)
        )
        Text(
            text = "Lock Witness",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = LWTextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Outlined.NotificationsNone,
            contentDescription = "Notifications",
            tint = LWTextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = "Menu",
            tint = LWTextSecondary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun HeroCard(monitoringEnabled: Boolean) {
    ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
        Column(
            modifier = Modifier.padding(start = 0.dp, top = 14.dp, end = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.padlock),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "DIGITAL WITNESS",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Normal,
                        color = LWTextPrimary
                    )
                    Text(
                        text = "Owner-controlled failed-unlock evidence monitor",
                        style = MaterialTheme.typography.bodySmall,
                        color = LWTextSecondary
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusPill(
                    text = if (monitoringEnabled) "Monitoring Active" else "Monitoring Paused",
                    dotColor = if (monitoringEnabled) LWSuccessGreen else LWAccentRed,
                    color = if (monitoringEnabled) LWSuccessGreen else LWChrome
                )
                EkgLine(
                    modifier = Modifier.weight(1f).height(40.dp),
                    color = if (monitoringEnabled) LWAccentRed else LWTextSecondary.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun EkgLine(modifier: Modifier = Modifier, color: Color) {
    val tracer = Color(0xFFFF5555)
    val active = color.alpha > 0.1f
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val mid = h / 2f
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        fun seg(vararg pts: Pair<Float, Float>) = Path().apply {
            moveTo(pts[0].first, pts[0].second)
            pts.drop(1).forEach { lineTo(it.first, it.second) }
        }
        fun glowSeg(path: Path, alpha: Float) {
            drawPath(path, tracer.copy(alpha = alpha * 0.08f), style = Stroke(16.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(path, tracer.copy(alpha = alpha * 0.20f), style = Stroke(7.dp.toPx(),  cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(path, tracer.copy(alpha = alpha * 0.45f), style = Stroke(3.dp.toPx(),  cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(path, tracer.copy(alpha = alpha),         style = stroke)
        }

        // dim baseline — the "old" trace
        val baseline = seg(
            0f to mid, w * 0.10f to mid,
            w * 0.18f to mid, w * 0.22f to mid,
            w * 0.35f to mid, w * 0.46f to mid,
            w * 0.54f to mid, w * 0.58f to mid,
            w * 0.71f to mid, w to mid
        )
        val baseAlpha = if (active) 0.18f else 0.08f
        drawPath(baseline, tracer.copy(alpha = baseAlpha), style = stroke)

        // small pre-bump 1
        glowSeg(seg(w * 0.10f to mid, w * 0.14f to mid - h * 0.20f, w * 0.18f to mid),
            if (active) 0.35f else 0.10f)

        // beat 1 spike (medium brightness — older beat)
        glowSeg(seg(
            w * 0.22f to mid,
            w * 0.25f to mid - h * 0.85f,
            w * 0.28f to mid + h * 0.55f,
            w * 0.31f to mid - h * 0.25f,
            w * 0.35f to mid
        ), if (active) 0.55f else 0.12f)

        // small pre-bump 2
        glowSeg(seg(w * 0.46f to mid, w * 0.50f to mid - h * 0.20f, w * 0.54f to mid),
            if (active) 0.45f else 0.12f)

        // beat 2 spike (brightest — current beat, most recent)
        glowSeg(seg(
            w * 0.58f to mid,
            w * 0.61f to mid - h * 0.85f,
            w * 0.64f to mid + h * 0.55f,
            w * 0.67f to mid - h * 0.25f,
            w * 0.71f to mid
        ), if (active) 0.95f else 0.15f)
    }
}

@Composable
private fun IncidentSummaryCard(state: DashboardUiState, isPro: Boolean) {
    ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    SectionEyebrow("Evidence Summary")
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
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.fingerprint_shield),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp)
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
                    } else "Pro",
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
                        if (state.locationCount > 0) "Captured" else "None Yet"
                    } else "Pro",
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
    val iconTint = if (status == EvidenceTileStatus.LOCKED) LWChrome.copy(alpha = 0.4f) else LWTextPrimary

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LWPanel),
        border = BorderStroke(1.dp, LockWitnessBorder)
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = LWTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (status == EvidenceTileStatus.LOCKED) LWTextSecondary else LockWitnessPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (status == EvidenceTileStatus.ACTIVE) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = LWAccentRed,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                )
            }
        }
    }
}

@Composable
private fun EvidenceIntegrityCard(hashingEnabled: Boolean, exportState: ExportState, isPro: Boolean) {
    val shieldTint = if (isPro) LWSuccessGreen else LWChrome
    val exportTint = if (exportState == ExportState.EXPORTING) LWAccentRed else shieldTint

    ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            SectionEyebrow("Evidence Integrity")
            Spacer(modifier = Modifier.height(12.dp))
            IntegrityRow(
                icon = Icons.Outlined.Shield,
                iconTint = shieldTint,
                label = "SHA-256 Hashing",
                trailingText = if (hashingEnabled) "Enabled" else "Enable",
                trailingColor = if (hashingEnabled) LWSuccessGreen else LWAccentRed
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = LockWitnessBorder)
            IntegrityRow(
                icon = Icons.Outlined.Lock,
                iconTint = LWSuccessGreen,
                label = "Local-Only Storage",
                sublabel = "All evidence stored on this device",
                trailingText = "Secure",
                trailingColor = LWSuccessGreen
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = LockWitnessBorder)
            IntegrityRow(
                icon = Icons.Outlined.Download,
                iconTint = exportTint,
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
    iconTint: Color = LWTextSecondary,
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
        Icon(imageVector = icon, contentDescription = null, tint = iconTint)
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
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = LWTextSecondary,
            modifier = Modifier.size(18.dp)
        )
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.WorkspacePremium,
                contentDescription = null,
                tint = LWAccentRed,
                modifier = Modifier.size(40.dp)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SectionEyebrow("Unlock Pro Evidence Tools")
                Text(
                    text = "Unlock full forensic history, video evidence, GPS snapshots and advanced protections.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LWTextSecondary
                )
            }
            Button(onClick = onNavigateToUpgrade) {
                Text("UPGRADE", fontWeight = FontWeight.Bold)
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
