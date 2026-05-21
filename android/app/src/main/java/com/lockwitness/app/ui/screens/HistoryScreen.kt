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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lockwitness.app.alert.AlertIncidentUpdater
import com.lockwitness.app.alert.AlertShareIntentBuilder
import com.lockwitness.app.data.incident.LockWitnessDatabase
import com.lockwitness.app.data.incident.SecurityIncident
import com.lockwitness.app.data.incident.SecurityIncidentRepository
import com.lockwitness.app.export.LocalIncidentExporter
import com.lockwitness.app.monetization.MonetizationRepository
import com.lockwitness.app.monetization.MonetizationState
import com.lockwitness.app.monetization.ProFeature
import com.lockwitness.app.monetization.ProFeatureGate
import com.lockwitness.app.ui.components.ForensicCard
import com.lockwitness.app.ui.components.SectionEyebrow
import com.lockwitness.app.ui.components.StatusPill
import com.lockwitness.app.ui.history.IncidentDetailUi
import com.lockwitness.app.ui.history.IncidentHistoryActions
import com.lockwitness.app.ui.history.IncidentHistoryMapper
import com.lockwitness.app.ui.history.IncidentSummaryUi
import com.lockwitness.app.ui.theme.LockWitnessBackground
import com.lockwitness.app.ui.theme.LockWitnessBorder
import com.lockwitness.app.ui.theme.LockWitnessPrimary
import com.lockwitness.app.ui.theme.LockWitnessSurfaceRaised
import com.lockwitness.app.ui.theme.LockWitnessSurfaceVariant
import com.lockwitness.app.ui.theme.LockWitnessTextSecondary
import kotlinx.coroutines.launch

internal enum class HistoryFilter { All, Photos, Videos, Location }

@Composable
fun HistoryScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    val repository = remember(context) {
        SecurityIncidentRepository(LockWitnessDatabase.getInstance(context).securityIncidentDao())
    }
    val mapper = remember { IncidentHistoryMapper() }
    val actions = remember(repository) { IncidentHistoryActions(repository) }
    val exporter = remember(context) { LocalIncidentExporter(context) }
    val shareIntentBuilder = remember(context) { AlertShareIntentBuilder(context) }
    val alertUpdater = remember(repository) { AlertIncidentUpdater(repository) }
    val monetizationRepository = remember(context) { MonetizationRepository.create(context) }
    val monetizationState by monetizationRepository.state.collectAsState(initial = MonetizationState.Free)
    val proFeatureGate = remember { ProFeatureGate() }
    val incidents by repository.getAllOrderedByTimestampDesc().collectAsState(initial = emptyList())
    val visibleIncidents = proFeatureGate.visibleHistory(incidents, monetizationState)
    var selectedIncidentId by remember { mutableLongStateOf(NO_SELECTION) }
    val selectedIncident = visibleIncidents.firstOrNull { it.id == selectedIncidentId }
    var exportStatus by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(HistoryFilter.All) }
    val scope = rememberCoroutineScope()

    val filteredIncidents = when (activeFilter) {
        HistoryFilter.All -> visibleIncidents
        HistoryFilter.Photos -> visibleIncidents.filter { it.photoStatus == "SUCCESS" }
        HistoryFilter.Videos -> visibleIncidents.filter { it.videoStatus == "SUCCESS" }
        HistoryFilter.Location -> visibleIncidents.filter { it.latitude != null }
    }

    HistoryContent(
        contentPadding = contentPadding,
        incidents = filteredIncidents,
        allIncidents = visibleIncidents,
        totalIncidentCount = incidents.size,
        selectedIncident = selectedIncident,
        mapper = mapper,
        monetizationState = monetizationState,
        canExport = proFeatureGate.isAllowed(ProFeature.ExportZip, monetizationState),
        activeFilter = activeFilter,
        onFilterChange = { activeFilter = it },
        onSelectIncident = { selectedIncidentId = it },
        onBackToTimeline = { selectedIncidentId = NO_SELECTION },
        onDeleteIncident = { id ->
            scope.launch {
                actions.deleteIncident(id)
                selectedIncidentId = NO_SELECTION
            }
        },
        onClearAll = {
            scope.launch {
                actions.clearIncidents()
                selectedIncidentId = NO_SELECTION
            }
        },
        exportStatus = exportStatus,
        onExportAll = {
            scope.launch {
                exportStatus = "Creating export…"
                val result = exporter.exportIncidents(visibleIncidents, filePrefix = "lockwitness_all_incidents")
                exportStatus = "Saved: ${result.file.name}"
            }
        },
        onExportIncident = { incident ->
            scope.launch {
                exportStatus = "Creating export…"
                val result = exporter.exportIncidents(listOf(incident), filePrefix = "lockwitness_incident_${incident.id}")
                exportStatus = "Saved: ${result.file.name}"
            }
        },
        onSendIncident = { incident ->
            scope.launch {
                exportStatus = "Preparing share…"
                val result = exporter.exportIncidents(listOf(incident), filePrefix = "lockwitness_incident_${incident.id}")
                runCatching {
                    context.startActivity(shareIntentBuilder.buildChooserIntent(result.file))
                }.onSuccess {
                    alertUpdater.markManualShareLaunched(incident.id)
                    exportStatus = "Chooser opened."
                }.onFailure { error ->
                    alertUpdater.markManualShareFailed(incident.id, error.message ?: "No compatible chooser.")
                    exportStatus = "Saved locally: ${result.file.name}"
                }
            }
        }
    )
}

@Composable
internal fun HistoryContent(
    contentPadding: PaddingValues,
    incidents: List<SecurityIncident>,
    allIncidents: List<SecurityIncident>,
    totalIncidentCount: Int,
    selectedIncident: SecurityIncident?,
    mapper: IncidentHistoryMapper,
    monetizationState: MonetizationState,
    canExport: Boolean,
    activeFilter: HistoryFilter,
    onFilterChange: (HistoryFilter) -> Unit,
    onSelectIncident: (Long) -> Unit,
    onBackToTimeline: () -> Unit,
    onDeleteIncident: (Long) -> Unit,
    onClearAll: () -> Unit,
    exportStatus: String,
    onExportAll: () -> Unit,
    onExportIncident: (SecurityIncident) -> Unit,
    onSendIncident: (SecurityIncident) -> Unit
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            selectedIncident != null -> {
                IncidentDetailView(
                    detail = mapper.toDetail(selectedIncident),
                    incident = selectedIncident,
                    onBack = onBackToTimeline,
                    onDelete = { onDeleteIncident(selectedIncident.id) },
                    onExport = { onExportIncident(selectedIncident) },
                    onSend = { onSendIncident(selectedIncident) },
                    canSend = selectedIncident.emailEnabled || selectedIncident.shareEnabled,
                    canExport = canExport,
                    exportStatus = exportStatus
                )
            }

            else -> {
                // Filter tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HistoryFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = activeFilter == filter,
                            onClick = { onFilterChange(filter) },
                            label = {
                                Text(
                                    text = filter.name,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LockWitnessPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = LockWitnessSurfaceRaised,
                                labelColor = LockWitnessTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = activeFilter == filter,
                                borderColor = LockWitnessBorder,
                                selectedBorderColor = LockWitnessPrimary
                            )
                        )
                    }
                }

                // Summary + actions bar
                if (allIncidents.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${allIncidents.size} incident${if (allIncidents.size != 1) "s" else ""}${
                                if (!monetizationState.isPro && totalIncidentCount > allIncidents.size)
                                    " (${totalIncidentCount} total — upgrade for full history)"
                                else ""
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = LockWitnessTextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        if (canExport) {
                            OutlinedButton(
                                onClick = onExportAll,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = LockWitnessPrimary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LockWitnessPrimary)
                            ) {
                                Text("Export All")
                            }
                        }
                        OutlinedButton(
                            onClick = onClearAll,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LockWitnessTextSecondary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LockWitnessBorder)
                        ) {
                            Text("Clear All")
                        }
                    }
                    if (exportStatus.isNotEmpty()) {
                        Text(exportStatus, style = MaterialTheme.typography.bodySmall, color = LockWitnessTextSecondary)
                    }
                }

                // Incident list
                if (incidents.isEmpty()) {
                    ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = false) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FolderOff,
                                contentDescription = null,
                                tint = LockWitnessTextSecondary,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = if (activeFilter == HistoryFilter.All) "No incidents recorded"
                                else "No ${activeFilter.name.lowercase()} captured",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Failed-unlock records appear here after monitoring captures evidence.",
                                style = MaterialTheme.typography.bodySmall,
                                color = LockWitnessTextSecondary
                            )
                        }
                    }
                } else {
                    incidents.forEach { incident ->
                        IncidentSummaryRow(
                            summary = mapper.toSummary(incident),
                            incident = incident,
                            onOpen = { onSelectIncident(incident.id) },
                            onDelete = { onDeleteIncident(incident.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun IncidentSummaryRow(
    summary: IncidentSummaryUi,
    incident: SecurityIncident,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val isCaptured = incident.photoStatus == "SUCCESS" || incident.videoStatus == "SUCCESS"

    ForensicCard(
        modifier = Modifier.fillMaxWidth(),
        elevated = false
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Thumbnail placeholder
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LockWitnessSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Error,
                    contentDescription = null,
                    tint = LockWitnessPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = summary.timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = LockWitnessTextSecondary
                    )
                    if (isCaptured) {
                        StatusPill(text = "Captured")
                    }
                }
                Text(
                    text = "Failed unlock attempt",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${summary.failedAttemptCount} attempt${if (summary.failedAttemptCount != "1") "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LockWitnessTextSecondary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (summary.hasPhoto) {
                        Icon(Icons.Outlined.CameraAlt, null, tint = LockWitnessPrimary, modifier = Modifier.size(16.dp))
                    }
                    if (summary.hasVideo) {
                        Icon(Icons.Outlined.Videocam, null, tint = LockWitnessPrimary, modifier = Modifier.size(16.dp))
                    }
                    if (summary.hasLocation) {
                        Icon(Icons.Outlined.Place, null, tint = LockWitnessPrimary, modifier = Modifier.size(16.dp))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = LockWitnessBorder)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onOpen,
                        colors = ButtonDefaults.buttonColors(containerColor = LockWitnessPrimary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("View", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LockWitnessTextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LockWitnessBorder),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Delete", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun IncidentDetailView(
    detail: IncidentDetailUi,
    incident: SecurityIncident,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onSend: () -> Unit,
    canSend: Boolean,
    canExport: Boolean,
    exportStatus: String
) {
    // Back row
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = LockWitnessPrimary)
        }
        Text(
            text = "Incident Detail",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        StatusPill(text = if (incident.photoStatus == "SUCCESS" || incident.videoStatus == "SUCCESS") "Captured" else "No Media")
    }

    // Detail card
    ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            SectionEyebrow("Incident")
            Spacer(modifier = Modifier.height(10.dp))
            DetailRow("Timestamp", detail.timestamp)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = LockWitnessBorder)
            DetailRow("Trigger", detail.triggerType)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = LockWitnessBorder)
            DetailRow("Failed attempts", detail.failedAttemptCount)
        }
    }

    if (detail.mediaFields.isNotEmpty()) {
        ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = false) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                SectionEyebrow("Media")
                Spacer(modifier = Modifier.height(10.dp))
                detail.mediaFields.forEachIndexed { i, (label, value) ->
                    DetailRow(label, value)
                    if (i < detail.mediaFields.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = LockWitnessBorder)
                    }
                }
            }
        }
    }

    if (detail.locationFields.isNotEmpty()) {
        ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = false) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                SectionEyebrow("Location")
                Spacer(modifier = Modifier.height(10.dp))
                detail.locationFields.forEachIndexed { i, (label, value) ->
                    DetailRow(label, value)
                    if (i < detail.locationFields.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = LockWitnessBorder)
                    }
                }
            }
        }
    }

    if (detail.hashFields.isNotEmpty()) {
        ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = false) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                SectionEyebrow("Evidence Hashes")
                Spacer(modifier = Modifier.height(10.dp))
                detail.hashFields.forEachIndexed { i, (label, value) ->
                    DetailRow(label, value)
                    if (i < detail.hashFields.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = LockWitnessBorder)
                    }
                }
            }
        }
    }

    // Action buttons
    ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = false) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onExport,
                enabled = canExport,
                colors = ButtonDefaults.buttonColors(containerColor = LockWitnessPrimary),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.FileDownload, null, modifier = Modifier.size(16.dp))
                Text(" ${if (canExport) "Export" else "Export Pro"}", color = Color.White)
            }
            Button(
                onClick = onSend,
                enabled = canSend && canExport,
                colors = ButtonDefaults.buttonColors(containerColor = LockWitnessPrimary),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.Share, null, modifier = Modifier.size(16.dp))
                Text(" Share", color = Color.White)
            }
            OutlinedButton(
                onClick = onDelete,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LockWitnessTextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, LockWitnessBorder),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(16.dp))
                Text(" Delete")
            }
        }
    }

    if (exportStatus.isNotEmpty()) {
        Text(exportStatus, style = MaterialTheme.typography.bodySmall, color = LockWitnessTextSecondary)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = LockWitnessTextSecondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
internal fun HistoryLoadingState(contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = LockWitnessPrimary)
        Text("Loading incidents", color = LockWitnessTextSecondary)
    }
}

@Composable
internal fun HistoryErrorState(contentPadding: PaddingValues, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("History unavailable", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(message, color = LockWitnessTextSecondary)
    }
}

private const val NO_SELECTION = -1L
