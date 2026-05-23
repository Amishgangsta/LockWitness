package com.lockwitness.app.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lockwitness.app.data.incident.LockWitnessDatabase
import com.lockwitness.app.data.incident.SecurityIncident
import com.lockwitness.app.data.incident.SecurityIncidentRepository
import com.lockwitness.app.monetization.MonetizationRepository
import com.lockwitness.app.monetization.MonetizationState
import com.lockwitness.app.monetization.ProFeatureGate
import com.lockwitness.app.ui.components.ForensicCard
import com.lockwitness.app.ui.components.SectionEyebrow
import com.lockwitness.app.ui.components.StatusPill
import com.lockwitness.app.ui.history.IncidentHistoryActions
import com.lockwitness.app.ui.theme.CardSurface
import com.lockwitness.app.ui.theme.CautionAmber
import com.lockwitness.app.ui.theme.GraphiteBg
import com.lockwitness.app.ui.theme.MutedChip
import com.lockwitness.app.ui.theme.ProOrange
import com.lockwitness.app.ui.theme.StrokeSubtle
import com.lockwitness.app.ui.theme.TextPrimary
import com.lockwitness.app.ui.theme.TextSecondary
import com.lockwitness.app.ui.theme.VerifiedGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HistoryScreen(
    contentPadding: PaddingValues,
    onNavigateToDetail: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember(context) {
        SecurityIncidentRepository(LockWitnessDatabase.getInstance(context).securityIncidentDao())
    }
    val actions = remember(repository) { IncidentHistoryActions(repository) }
    val monetizationRepository = remember(context) { MonetizationRepository.create(context) }
    val monetizationState by monetizationRepository.state.collectAsState(initial = MonetizationState(isPro = true, billingAvailable = false))
    val proFeatureGate = remember { ProFeatureGate() }
    val incidents by repository.getAllOrderedByTimestampDesc().collectAsState(initial = emptyList())
    val visibleIncidents = proFeatureGate.visibleHistory(incidents, monetizationState)
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GraphiteBg)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Evidence Timeline",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = if (monetizationState.isPro || incidents.size == visibleIncidents.size) {
                    "${visibleIncidents.size} incident${if (visibleIncidents.size != 1) "s" else ""}"
                } else {
                    "Showing ${visibleIncidents.size} of ${incidents.size}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // Free upgrade banner
        if (!monetizationState.isPro && incidents.size > visibleIncidents.size) {
            ForensicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.WorkspacePremium, contentDescription = null, tint = ProOrange, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Free history is limited. Upgrade for full ledger retention.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Incident list or empty state
        if (visibleIncidents.isEmpty()) {
            EmptyTimelineCard()
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                visibleIncidents.forEach { incident ->
                    IncidentLedgerCard(
                        incident = incident,
                        onOpen = { onNavigateToDetail(incident.id) },
                        onDelete = { scope.launch { actions.deleteIncident(incident.id) } }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun EmptyTimelineCard() {
    ForensicCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
            Text("No incidents recorded", style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text(
                "Lock Witness will list failed unlock evidence here once captured.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun IncidentLedgerCard(
    incident: SecurityIncident,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val hasPhoto = incident.photoStatus == "SUCCESS" && !incident.photoPath.isNullOrBlank()
    val hasVideo = incident.videoStatus == "SUCCESS"
    val hasLocation = incident.locationStatus == "SUCCESS"
    val isHashed = !incident.imageSha256.isNullOrBlank() || !incident.videoSha256.isNullOrBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardSurface)
            .border(1.dp, StrokeSubtle, RoundedCornerShape(14.dp))
            .clickable(onClick = onOpen)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PhotoThumbnailSmall(
            photoPath = incident.photoPath,
            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
        )

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatIncidentTimestamp(incident.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                if (isHashed) StatusPill(text = "Hashed", color = VerifiedGreen)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LockOpen, contentDescription = null, tint = CautionAmber, modifier = Modifier.size(13.dp))
                Text("Failed Unlock", style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                if (hasPhoto) EvidenceChip(icon = Icons.Outlined.CameraAlt, label = "Photo")
                if (hasVideo) EvidenceChip(icon = Icons.Outlined.Videocam, label = "Video")
                if (hasLocation) EvidenceChip(icon = Icons.Outlined.LocationOn, label = "GPS")
                if (incident.shareStatus == "SUCCESS") EvidenceChip(icon = Icons.Outlined.Share, label = "Shared")
            }

            Text(
                text = "Attempts: ${incident.failedAttemptCount}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun EvidenceChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MutedChip)
            .border(1.dp, StrokeSubtle, RoundedCornerShape(6.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(10.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
private fun PhotoThumbnailSmall(photoPath: String?, modifier: Modifier = Modifier) {
    var bitmap by remember(photoPath) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(photoPath) {
        if (!photoPath.isNullOrBlank()) {
            bitmap = withContext(Dispatchers.IO) {
                runCatching {
                    val opts = android.graphics.BitmapFactory.Options().apply { inSampleSize = 4 }
                    BitmapFactory.decodeFile(photoPath, opts)?.asImageBitmap()
                }.getOrNull()
            }
        }
    }
    Box(modifier = modifier.background(MutedChip), contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            Image(bitmap = bitmap!!, contentDescription = "Evidence photo", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
internal fun HistoryLoadingState(contentPadding: PaddingValues) {
    Column(
        modifier = Modifier.fillMaxSize().background(GraphiteBg).padding(contentPadding).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = TextSecondary)
        Text("Loading incidents", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
internal fun HistoryErrorState(contentPadding: PaddingValues, message: String) {
    Column(
        modifier = Modifier.fillMaxSize().background(GraphiteBg).padding(contentPadding).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("History unavailable", style = MaterialTheme.typography.headlineSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
        Text(message, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

internal fun formatIncidentTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 86_400_000 -> "Today, " + java.text.SimpleDateFormat("h:mm a", java.util.Locale.US).format(java.util.Date(timestamp))
        diff < 172_800_000 -> "Yesterday, " + java.text.SimpleDateFormat("h:mm a", java.util.Locale.US).format(java.util.Date(timestamp))
        else -> java.text.SimpleDateFormat("MMM d, h:mm a", java.util.Locale.US).format(java.util.Date(timestamp))
    }
}
