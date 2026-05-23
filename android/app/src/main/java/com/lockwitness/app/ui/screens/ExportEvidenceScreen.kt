package com.lockwitness.app.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lockwitness.app.data.incident.LockWitnessDatabase
import com.lockwitness.app.data.incident.SecurityIncidentRepository
import com.lockwitness.app.export.IncidentExportResult
import com.lockwitness.app.export.LocalIncidentExporter
import com.lockwitness.app.ui.components.ForensicCard
import com.lockwitness.app.ui.components.ForensicDivider
import com.lockwitness.app.ui.components.SectionEyebrow
import com.lockwitness.app.ui.components.StatusPill
import com.lockwitness.app.ui.theme.CautionAmber
import com.lockwitness.app.ui.theme.DestructiveRed
import com.lockwitness.app.ui.theme.GraphiteBg
import com.lockwitness.app.ui.theme.HashText
import com.lockwitness.app.ui.theme.StrokeSubtle
import com.lockwitness.app.ui.theme.TextPrimary
import com.lockwitness.app.ui.theme.TextSecondary
import com.lockwitness.app.ui.theme.VerifiedGreen
import kotlinx.coroutines.launch

private sealed class ExportUiState {
    object Idle : ExportUiState()
    object Building : ExportUiState()
    data class Success(val result: IncidentExportResult) : ExportUiState()
    data class Failure(val message: String) : ExportUiState()
}

@Composable
fun ExportEvidenceScreen(
    incidentId: Long,
    contentPadding: PaddingValues,
    onNavigateBack: () -> Unit = {},
    onNavigateToShare: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember(context) {
        SecurityIncidentRepository(LockWitnessDatabase.getInstance(context).securityIncidentDao())
    }
    val exporter = remember(context) { LocalIncidentExporter(context) }
    val incident by repository.getById(incidentId).collectAsState(initial = null)
    var exportState by remember { mutableStateOf<ExportUiState>(ExportUiState.Idle) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GraphiteBg)
            .padding(contentPadding)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "Export Evidence",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // What's included
            ForensicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    SectionEyebrow("Package Contents")
                    Spacer(modifier = Modifier.height(10.dp))
                    val items = listOf(
                        Icons.Outlined.Tag to "metadata.json — device, timestamp, app version",
                        Icons.Outlined.FileDownload to "incidents.csv — tabular evidence record",
                        Icons.Outlined.Shield to "hashes.txt — SHA-256 integrity manifest",
                        Icons.Outlined.Inventory2 to "Media files — photos and video if captured"
                    )
                    items.forEachIndexed { i, (icon, text) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icon, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(16.dp))
                            Text(text, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                        if (i < items.lastIndex) ForensicDivider()
                    }
                }
            }

            // Incident summary
            incident?.let { inc ->
                ForensicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionEyebrow("Incident Summary")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Timestamp", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(formatIncidentTimestamp(inc.timestamp), style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Photo", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            StatusPill(text = inc.photoStatus, color = if (inc.photoStatus == "SUCCESS") VerifiedGreen else TextSecondary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Video", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            StatusPill(text = inc.videoStatus, color = if (inc.videoStatus == "SUCCESS") VerifiedGreen else TextSecondary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Location", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            StatusPill(text = inc.locationStatus, color = if (inc.locationStatus == "SUCCESS") VerifiedGreen else TextSecondary)
                        }
                        val isHashed = !inc.imageSha256.isNullOrBlank() || !inc.videoSha256.isNullOrBlank()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Integrity", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            StatusPill(text = if (isHashed) "Hashed" else "Not hashed", color = if (isHashed) VerifiedGreen else CautionAmber)
                        }
                    }
                }
            }

            // Export action area
            when (val state = exportState) {
                is ExportUiState.Idle -> {
                    Button(
                        onClick = {
                            val inc = incident ?: return@Button
                            exportState = ExportUiState.Building
                            scope.launch {
                                exportState = try {
                                    val result = exporter.exportIncidents(
                                        incidents = listOf(inc),
                                        filePrefix = "lockwitness_incident_${inc.id}"
                                    )
                                    ExportUiState.Success(result)
                                } catch (e: Exception) {
                                    ExportUiState.Failure(e.message ?: "Unknown error")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = incident != null,
                        colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen, contentColor = TextPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(Icons.Outlined.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  Generate Export Package", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
                is ExportUiState.Building -> {
                    Button(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen, contentColor = TextPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = TextPrimary, strokeWidth = 2.dp)
                        Text("  Building…", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
                is ExportUiState.Success -> {
                    ForensicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(18.dp))
                                Text("Export Ready", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Text(state.result.file.name, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = HashText)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Incidents", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text("${state.result.incidentCount}", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Media files", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text("${state.result.mediaFilesIncluded}", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                            if (state.result.missingMediaFiles.isNotEmpty()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Error, contentDescription = null, tint = CautionAmber, modifier = Modifier.size(14.dp))
                                    Text("${state.result.missingMediaFiles.size} media file(s) missing from disk", style = MaterialTheme.typography.labelSmall, color = CautionAmber)
                                }
                            }
                        }
                    }
                    Button(
                        onClick = { onNavigateToShare(state.result.file.absolutePath) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen, contentColor = TextPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text("Share / Save Package", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { exportState = ExportUiState.Idle },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StrokeSubtle)
                    ) {
                        Text("Re-generate", style = MaterialTheme.typography.labelMedium)
                    }
                }
                is ExportUiState.Failure -> {
                    ForensicCard(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Error, contentDescription = null, tint = DestructiveRed, modifier = Modifier.size(20.dp))
                            Column {
                                Text("Export Failed", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text(state.message, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                        }
                    }
                    Button(
                        onClick = { exportState = ExportUiState.Idle },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen, contentColor = TextPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Try Again", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
