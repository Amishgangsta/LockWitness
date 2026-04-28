package com.lockwitness.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lockwitness.app.data.incident.LockWitnessDatabase
import com.lockwitness.app.data.incident.SecurityIncident
import com.lockwitness.app.data.incident.SecurityIncidentRepository
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
    val incidents by repository
        .getAllOrderedByTimestampDesc()
        .collectAsState(initial = emptyList())
    var selectedIncidentId by remember { mutableLongStateOf(NO_SELECTION) }
    val selectedIncident = incidents.firstOrNull { it.id == selectedIncidentId }
    val scope = rememberCoroutineScope()

    HistoryContent(
        contentPadding = contentPadding,
        incidents = incidents,
        selectedIncident = selectedIncident,
        mapper = mapper,
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
        }
    )
}

@Composable
internal fun HistoryContent(
    contentPadding: PaddingValues,
    incidents: List<SecurityIncident>,
    selectedIncident: SecurityIncident?,
    mapper: IncidentHistoryMapper,
    onSelectIncident: (Long) -> Unit,
    onBackToTimeline: () -> Unit,
    onDeleteIncident: (Long) -> Unit,
    onClearAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Local incident timeline",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            OutlinedButton(
                onClick = onClearAll,
                enabled = incidents.isNotEmpty()
            ) {
                Text("Clear All")
            }
        }

        when {
            selectedIncident != null -> IncidentDetailCard(
                detail = mapper.toDetail(selectedIncident),
                onBack = onBackToTimeline,
                onDelete = { onDeleteIncident(selectedIncident.id) }
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
            .padding(contentPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Text("Loading incidents")
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
            .padding(contentPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "History unavailable",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(message)
    }
}

@Composable
private fun EmptyHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No incidents recorded",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Failed-unlock records will appear here after monitoring creates local incidents.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun IncidentSummaryCard(
    summary: IncidentSummaryUi,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = summary.timestamp,
                style = MaterialTheme.typography.titleMedium
            )
            Text("Trigger: ${summary.triggerType}")
            Text("Failed attempts: ${summary.failedAttemptCount}")
            StatusChips(summary)
            IndicatorRow(summary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpen) {
                    Text("Details")
                }
                OutlinedButton(onClick = onDelete) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun StatusChips(summary: IncidentSummaryUi) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("Photo: ${summary.photoStatus}")
        Text("Video: ${summary.videoStatus}")
        Text("Location: ${summary.locationStatus}")
        Text("Email: ${summary.emailStatus}")
        Text("Share: ${summary.shareStatus}")
    }
}

@Composable
private fun IndicatorRow(summary: IncidentSummaryUi) {
    Text(
        text = "Media: photo ${summary.hasPhoto.yesNo()}, video ${summary.hasVideo.yesNo()}, location ${summary.hasLocation.yesNo()}",
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun IncidentDetailCard(
    detail: IncidentDetailUi,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Incident detail",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(detail.timestamp)
                }
                OutlinedButton(onClick = onBack) {
                    Text("Back")
                }
            }
            DetailField("Trigger", detail.triggerType)
            DetailField("Failed attempts", detail.failedAttemptCount)
            DetailSection("Settings snapshot", detail.settingsSnapshot)
            DetailSection("Device", detail.deviceMetadata)
            DetailMediaSection(detail.mediaFields)
            DetailSection("Location", detail.locationFields)
            DetailSection("Hashes", detail.hashFields)
            DetailSection("Module statuses", detail.statusFields)
            DetailField("Notes", detail.notes)
            Spacer(modifier = Modifier.padding(top = 4.dp))
            OutlinedButton(onClick = onDelete) {
                Text("Delete Incident")
            }
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
            style = MaterialTheme.typography.titleMedium
        )
        if (fields.isEmpty()) {
            Text("No data recorded.")
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
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun Boolean.yesNo(): String =
    if (this) "yes" else "no"

private const val NO_SELECTION = -1L
