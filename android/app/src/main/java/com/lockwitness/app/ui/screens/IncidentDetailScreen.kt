package com.lockwitness.app.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lockwitness.app.data.incident.LockWitnessDatabase
import com.lockwitness.app.data.incident.SecurityIncident
import com.lockwitness.app.data.incident.SecurityIncidentRepository
import com.lockwitness.app.ui.components.ForensicCard
import com.lockwitness.app.ui.components.ForensicDivider
import com.lockwitness.app.ui.components.SectionEyebrow
import com.lockwitness.app.ui.components.StatusPill
import com.lockwitness.app.ui.theme.CardSurface
import com.lockwitness.app.ui.theme.CautionAmber
import com.lockwitness.app.ui.theme.DestructiveRed
import com.lockwitness.app.ui.theme.GraphiteBg
import com.lockwitness.app.ui.theme.HashText
import com.lockwitness.app.ui.theme.MutedChip
import com.lockwitness.app.ui.theme.StrokeSubtle
import com.lockwitness.app.ui.theme.TextPrimary
import com.lockwitness.app.ui.theme.TextSecondary
import com.lockwitness.app.ui.theme.VerifiedGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun IncidentDetailScreen(
    incidentId: Long,
    contentPadding: PaddingValues,
    onNavigateBack: () -> Unit = {},
    onNavigateToExport: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember(context) {
        SecurityIncidentRepository(LockWitnessDatabase.getInstance(context).securityIncidentDao())
    }
    val scope = rememberCoroutineScope()
    val incident by repository.getById(incidentId).collectAsState(initial = null)
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GraphiteBg)
            .padding(contentPadding)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "Incident Detail",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = DestructiveRed)
            }
        }

        val currentIncident = incident
        if (currentIncident == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading…", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        } else {
            IncidentDetailContent(
                incident = currentIncident,
                onExport = { onNavigateToExport(incidentId) }
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = CardSurface,
            title = { Text("Delete Incident?", color = TextPrimary) },
            text = { Text("This will permanently delete this incident and its media. This cannot be undone.", style = MaterialTheme.typography.bodySmall, color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.deleteById(incidentId)
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DestructiveRed, contentColor = TextPrimary)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun IncidentDetailContent(
    incident: SecurityIncident,
    onExport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header card
        ForensicCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LockOpen, contentDescription = null, tint = CautionAmber, modifier = Modifier.size(18.dp))
                        Text("Failed Unlock", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    val isHashed = !incident.imageSha256.isNullOrBlank() || !incident.videoSha256.isNullOrBlank()
                    if (isHashed) StatusPill(text = "Hashed", color = VerifiedGreen)
                }
                ForensicDivider()
                DetailField("Timestamp", formatIncidentTimestamp(incident.timestamp))
                DetailField("Device", incident.deviceModel)
                DetailField("Android", incident.androidVersion)
                DetailField("App Version", incident.appVersion)
                DetailField("Failed Attempts", incident.failedAttemptCount.toString())
                if (!incident.notes.isNullOrBlank()) {
                    DetailField("Notes", incident.notes)
                }
            }
        }

        // Photo
        if (incident.photoStatus == "SUCCESS" && !incident.photoPath.isNullOrBlank()) {
            ForensicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(16.dp))
                        SectionEyebrow("Photo Evidence")
                    }
                    PhotoFullView(photoPath = incident.photoPath)
                    if (!incident.imageSha256.isNullOrBlank()) {
                        HashRow(label = "SHA-256", hash = incident.imageSha256!!)
                    }
                }
            }
        } else if (incident.photoEnabled) {
            ForensicCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Column {
                        Text("Photo", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text(incident.photoStatus, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    StatusPill(
                        text = when (incident.photoStatus) {
                            "SUCCESS" -> "Captured"
                            "FAILED" -> "Failed"
                            else -> incident.photoStatus
                        },
                        color = when (incident.photoStatus) {
                            "SUCCESS" -> VerifiedGreen
                            "FAILED" -> DestructiveRed
                            else -> CautionAmber
                        }
                    )
                }
            }
        }

        // Video
        if (incident.videoEnabled) {
            ForensicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Videocam, contentDescription = null, tint = if (incident.videoStatus == "SUCCESS") VerifiedGreen else TextSecondary, modifier = Modifier.size(16.dp))
                        SectionEyebrow("Video Evidence")
                        StatusPill(
                            text = when (incident.videoStatus) {
                                "SUCCESS" -> "Captured"
                                "FAILED" -> "Failed"
                                else -> incident.videoStatus
                            },
                            color = when (incident.videoStatus) {
                                "SUCCESS" -> VerifiedGreen
                                "FAILED" -> DestructiveRed
                                else -> CautionAmber
                            }
                        )
                    }
                    if (!incident.videoPath.isNullOrBlank()) {
                        Text(incident.videoPath!!, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontFamily = FontFamily.Monospace)
                    }
                    if (!incident.videoSha256.isNullOrBlank()) {
                        HashRow(label = "SHA-256", hash = incident.videoSha256!!)
                    }
                }
            }
        }

        // Location
        if (incident.locationEnabled) {
            ForensicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = if (incident.locationStatus == "SUCCESS") VerifiedGreen else TextSecondary, modifier = Modifier.size(16.dp))
                        SectionEyebrow("Location")
                        StatusPill(
                            text = when (incident.locationStatus) {
                                "SUCCESS" -> "Captured"
                                "UNAVAILABLE" -> "Unavailable"
                                else -> incident.locationStatus
                            },
                            color = when (incident.locationStatus) {
                                "SUCCESS" -> VerifiedGreen
                                "UNAVAILABLE" -> CautionAmber
                                else -> DestructiveRed
                            }
                        )
                    }
                    if (incident.latitude != null && incident.longitude != null) {
                        DetailField("Coordinates", "${"%.6f".format(incident.latitude)}, ${"%.6f".format(incident.longitude)}")
                        if (incident.locationAccuracy != null) {
                            DetailField("Accuracy", "${"%.1f".format(incident.locationAccuracy)} m")
                        }
                        if (!incident.locationProvider.isNullOrBlank()) {
                            DetailField("Provider", incident.locationProvider!!)
                        }
                    }
                }
            }
        }

        // Export action
        Button(
            onClick = onExport,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen, contentColor = TextPrimary),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Icon(Icons.Outlined.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(" Export Evidence Package", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun PhotoFullView(photoPath: String?) {
    var bitmap by remember(photoPath) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(photoPath) {
        if (!photoPath.isNullOrBlank()) {
            bitmap = withContext(Dispatchers.IO) {
                runCatching {
                    BitmapFactory.decodeFile(photoPath)?.asImageBitmap()
                }.getOrNull()
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(10.dp))
            .background(MutedChip)
            .border(1.dp, StrokeSubtle, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(bitmap = bitmap!!, contentDescription = "Evidence photo", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun HashRow(label: String, hash: String) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(
            text = hash,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
            color = HashText,
            maxLines = 2
        )
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.weight(0.4f))
        Text(value, style = MaterialTheme.typography.bodySmall, color = TextPrimary, modifier = Modifier.weight(0.6f))
    }
}
