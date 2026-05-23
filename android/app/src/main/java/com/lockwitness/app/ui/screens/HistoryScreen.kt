package com.lockwitness.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lockwitness.app.ui.theme.LWActionOrange
import com.lockwitness.app.ui.theme.LWBackground
import com.lockwitness.app.ui.theme.LWPanel
import com.lockwitness.app.ui.theme.LWTextPrimary
import com.lockwitness.app.ui.theme.LWTextSecondary
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
import com.lockwitness.app.ui.history.IncidentDetailUi
import com.lockwitness.app.ui.history.IncidentHistoryActions
import com.lockwitness.app.ui.history.IncidentHistoryMapper
import com.lockwitness.app.ui.history.IncidentSummaryUi
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    val repository = remember(context) {
        SecurityIncidentRepository(
            LockWitnessDatabase.getInstance(context).securityIncidentDao()
        )
    }
    val mapper = remember { IncidentHistoryMapper() }
    val actions = remember(repository) { IncidentHistoryActions(repository) }
    val exporter = remember(context) { LocalIncidentExporter(context) }
    val shareIntentBuilder = remember(context) { AlertShareIntentBuilder(context) }
    val alertUpdater = remember(repository) { AlertIncidentUpdater(repository) }
    val monetizationRepository = remember(context) { MonetizationRepository.create(context) }
    val monetizationState by monetizationRepository.state.collectAsState(initial = MonetizationState.Free)
    val proFeatureGate = remember { ProFeatureGate() }
    val incidents by repository
        .getAllOrderedByTimestampDesc()
        .collectAsState(initial = emptyList())
    val visibleIncidents = proFeatureGate.visibleHistory(incidents, monetizationState)
    var selectedIncidentId by remember { mutableLongStateOf(NO_SELECTION) }
    val selectedIncident = visibleIncidents.firstOrNull { it.id == selectedIncidentId }
    var exportStatus by remember { androidx.compose.runtime.mutableStateOf("No export created.") }
    val scope = rememberCoroutineScope()

    HistoryContent(
        contentPadding = contentPadding,
        incidents = visibleIncidents,
        totalIncidentCount = incidents.size,
        selectedIncident = selectedIncident,
        mapper = mapper,
        monetizationState = monetizationState,
        canExport = proFeatureGate.isAllowed(ProFeature.ExportZip, monetizationState),
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
                exportStatus = "Creating local ZIP export..."
                val result = exporter.exportIncidents(visibleIncidents, filePrefix = "lockwitness_all_incidents")
                exportStatus = "Export saved locally: ${result.file.absolutePath}"
            }
        },
        onExportIncident = { incident ->
            scope.launch {
                exportStatus = "Creating local ZIP export..."
                val result = exporter.exportIncidents(listOf(incident), filePrefix = "lockwitness_incident_${incident.id}")
                exportStatus = "Export saved locally: ${result.file.absolutePath}"
            }
        },
        onSendIncident = { incident ->
            scope.launch {
                exportStatus = "Creating local ZIP export for chooser..."
                val result = exporter.exportIncidents(listOf(incident), filePrefix = "lockwitness_incident_${incident.id}")
                runCatching {
                    context.startActivity(shareIntentBuilder.buildChooserIntent(result.file))
                }.onSuccess {
                    alertUpdater.markManualShareLaunched(incident.id)
                    exportStatus = "Chooser opened for local export: ${result.file.absolutePath}"
                }.onFailure { error ->
                    alertUpdater.markManualShareFailed(
                        incidentId = incident.id,
                        reason = error.message ?: "No compatible chooser activity."
                    )
                    exportStatus = "Chooser unavailable; local export saved: ${result.file.absolutePath}"
                }
            }
        }
    )
}

@Composable
internal fun HistoryContent(
    contentPadding: PaddingValues,
    incidents: List<SecurityIncident>,
    totalIncidentCount: Int,
    selectedIncident: SecurityIncident?,
    mapper: IncidentHistoryMapper,
    monetizationState: MonetizationState,
    canExport: Boolean,
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
            .background(LWBackground)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.headlineMedium,
                    color = LWTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (monetizationState.isPro || totalIncidentCount <= incidents.size) {
                        "Local incident timeline"
                    } else {
                        "Showing ${incidents.size} of $totalIncidentCount"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = LWTextSecondary
                )
            }
            HistoryOutlinedChip(
                text = "Clear All",
                enabled = incidents.isNotEmpty(),
                onClick = onClearAll
            )
        }
        HistoryOutlinedChip(
            text = "Export All",
            proLabel = if (!canExport) "Pro" else null,
            enabled = incidents.isNotEmpty() && canExport,
            onClick = onExportAll
        )
        if (exportStatus != "No export created." || incidents.isNotEmpty()) {
            Text(
                text = exportStatus,
                style = MaterialTheme.typography.bodySmall,
                color = LWTextSecondary
            )
        }

        when {
            selectedIncident != null -> IncidentDetailCard(
                detail = mapper.toDetail(selectedIncident),
                onBack = onBackToTimeline,
                onDelete = { onDeleteIncident(selectedIncident.id) },
                onExport = { onExportIncident(selectedIncident) },
                onSend = { onSendIncident(selectedIncident) },
                canSend = selectedIncident.emailEnabled || selectedIncident.shareEnabled,
                canExport = canExport
            )

            incidents.isEmpty() -> EmptyHistoryCard()

            else -> {
                incidents.forEach { incident ->
                    IncidentSummaryCard(
                        summary = mapper.toSummary(incident),
                        onOpen = { onSelectIncident(incident.id) },
                        onDelete = { onDeleteIncident(incident.id) }
                    )
                }
            }
        }
    }
}

@Composable
internal fun HistoryLoadingState(contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LWBackground)
            .padding(contentPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = LWTextSecondary)
        Text("Loading incidents", style = MaterialTheme.typography.bodyMedium, color = LWTextSecondary)
    }
}

@Composable
internal fun HistoryErrorState(
    contentPadding: PaddingValues,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LWBackground)
            .padding(contentPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "History unavailable",
            style = MaterialTheme.typography.headlineSmall,
            color = LWTextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(message, style = MaterialTheme.typography.bodySmall, color = LWTextSecondary)
    }
}

@Composable
private fun HistoryOutlinedChip(
    text: String?,
    proLabel: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val borderColor = if (enabled) Color(0xFF374151) else Color(0xFF1F2937)
    val labelText = buildAnnotatedString {
        if (text != null) {
            withStyle(SpanStyle(color = if (enabled) LWTextPrimary else LWTextSecondary, fontWeight = FontWeight.Medium)) {
                append(text)
            }
        }
        if (proLabel != null) {
            if (text != null) append(" ")
            withStyle(SpanStyle(color = LWActionOrange, fontWeight = FontWeight.SemiBold)) {
                append(proLabel)
            }
        }
    }
    Text(
        text = labelText,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(LWPanel)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

@Composable
private fun EmptyHistoryCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(LWPanel)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "No incidents recorded",
            style = MaterialTheme.typography.titleMedium,
            color = LWTextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Failed-unlock records will appear here after monitoring creates local incidents.",
            style = MaterialTheme.typography.bodySmall,
            color = LWTextSecondary
        )
    }
}

@Composable
private fun IncidentSummaryCard(
    summary: IncidentSummaryUi,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(LWPanel)
            .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(10.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = summary.timestamp,
            style = MaterialTheme.typography.titleSmall,
            color = LWTextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Trigger: ${summary.triggerType}  •  Attempts: ${summary.failedAttemptCount}",
            style = MaterialTheme.typography.bodySmall,
            color = LWTextSecondary
        )
        StatusChips(summary)
        IndicatorRow(summary)
        Spacer(modifier = Modifier.height(2.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HistoryOutlinedChip(text = "Details", onClick = onOpen)
            HistoryOutlinedChip(text = "Delete", onClick = onDelete)
        }
    }
}

@Composable
private fun StatusChips(summary: IncidentSummaryUi) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text("Photo: ${summary.photoStatus}", style = MaterialTheme.typography.bodySmall, color = LWTextSecondary)
        Text("Video: ${summary.videoStatus}", style = MaterialTheme.typography.bodySmall, color = LWTextSecondary)
        Text("Location: ${summary.locationStatus}", style = MaterialTheme.typography.bodySmall, color = LWTextSecondary)
    }
}

@Composable
private fun IndicatorRow(summary: IncidentSummaryUi) {
    Text(
        text = "Photo ${summary.hasPhoto.yesNo()}  Video ${summary.hasVideo.yesNo()}  Location ${summary.hasLocation.yesNo()}",
        style = MaterialTheme.typography.bodySmall,
        color = LWTextSecondary
    )
}

@Composable
private fun IncidentDetailCard(
    detail: IncidentDetailUi,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onSend: () -> Unit,
    canSend: Boolean,
    canExport: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(LWPanel)
            .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(10.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Incident Detail",
                    style = MaterialTheme.typography.titleMedium,
                    color = LWTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(detail.timestamp, style = MaterialTheme.typography.bodySmall, color = LWTextSecondary)
            }
            HistoryOutlinedChip(text = "Back", onClick = onBack)
        }
        DetailField("Trigger", detail.triggerType)
        DetailField("Failed attempts", detail.failedAttemptCount)
        DetailSection("Settings", detail.settingsSnapshot)
        DetailSection("Device", detail.deviceMetadata)
        DetailMediaSection(detail.mediaFields)
        DetailSection("Location", detail.locationFields)
        DetailSection("Hashes", detail.hashFields)
        DetailSection("Module statuses", detail.statusFields)
        DetailField("Notes", detail.notes)
        Spacer(modifier = Modifier.height(2.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HistoryOutlinedChip(
                text = if (canExport) "Export" else null,
                proLabel = if (!canExport) "Pro" else null,
                enabled = canExport,
                onClick = onExport
            )
            HistoryOutlinedChip(text = "Send", enabled = canSend && canExport, onClick = onSend)
            HistoryOutlinedChip(text = "Delete", onClick = onDelete)
        }
    }
}

@Composable
private fun DetailMediaSection(fields: List<Pair<String, String>>) {
    if (fields.isEmpty()) {
        DetailSection("Media", listOf("Stored files" to "No photo or video path recorded."))
    } else {
        DetailSection(
            title = "Media",
            fields = fields + ("Preview" to "Safe fallback: local file path/status shown; runtime media rendering requires device test.")
        )
    }
}

@Composable
private fun DetailSection(
    title: String,
    fields: List<Pair<String, String>>
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF2A6FD6),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        if (fields.isEmpty()) {
            Text("No data recorded.", style = MaterialTheme.typography.bodySmall, color = LWTextSecondary)
        } else {
            fields.forEach { (label, value) ->
                DetailField(label, value)
            }
        }
    }
}

@Composable
private fun DetailField(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = LWTextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = LWTextPrimary
        )
    }
}

private fun Boolean.yesNo(): String =
    if (this) "yes" else "no"

private const val NO_SELECTION = -1L
