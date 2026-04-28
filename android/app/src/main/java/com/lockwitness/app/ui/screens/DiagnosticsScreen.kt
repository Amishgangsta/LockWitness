package com.lockwitness.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.lockwitness.app.alert.AlertShareIntentBuilder
import com.lockwitness.app.admin.AndroidDeviceInfoProvider
import com.lockwitness.app.admin.DeviceAdminStatus
import com.lockwitness.app.data.SettingsRepository
import com.lockwitness.app.data.SettingsState
import com.lockwitness.app.data.incident.LockWitnessDatabase
import com.lockwitness.app.data.incident.SecurityIncident
import com.lockwitness.app.data.incident.SecurityIncidentRepository
import com.lockwitness.app.diagnostics.DiagnosticCheck
import com.lockwitness.app.diagnostics.DiagnosticInput
import com.lockwitness.app.diagnostics.DiagnosticMapper
import com.lockwitness.app.diagnostics.RuntimeVerificationChecklist
import com.lockwitness.app.export.LocalIncidentExporter
import com.lockwitness.app.location.AndroidLocationSnapshotClient
import com.lockwitness.app.location.LocationSnapshotResult
import com.lockwitness.app.monetization.MonetizationRepository
import com.lockwitness.app.monetization.MonetizationState
import com.lockwitness.app.monetization.ProFeature
import com.lockwitness.app.monetization.ProFeatureGate
import com.lockwitness.app.photo.Camera2PhotoCaptureClient
import com.lockwitness.app.photo.PhotoCaptureResult
import com.lockwitness.app.video.Camera2VideoCaptureClient
import com.lockwitness.app.video.VideoCaptureResult
import kotlinx.coroutines.launch

@Composable
fun DiagnosticsScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    val settingsRepository = remember(context) { SettingsRepository.create(context) }
    val incidentRepository = remember(context) {
        SecurityIncidentRepository(
            LockWitnessDatabase.getInstance(context).securityIncidentDao()
        )
    }
    val monetizationRepository = remember(context) { MonetizationRepository.create(context) }
    val settings by settingsRepository.settings.collectAsState(initial = SettingsState.Defaults)
    val monetizationState by monetizationRepository.state.collectAsState(initial = MonetizationState.Free)
    val incidents by incidentRepository.getAllOrderedByTimestampDesc().collectAsState(initial = emptyList())
    val proFeatureGate = remember { ProFeatureGate() }
    val mapper = remember { DiagnosticMapper() }
    val deviceInfo = remember(context) { AndroidDeviceInfoProvider(context) }
    val exporter = remember(context) { LocalIncidentExporter(context) }
    val shareIntentBuilder = remember(context) { AlertShareIntentBuilder(context) }
    val scope = rememberCoroutineScope()
    var actionStatus by remember { mutableStateOf("No diagnostic action run.") }
    var chooserAvailable by remember { mutableStateOf<Boolean?>(null) }

    val input = DiagnosticInput(
        isDeviceAdminActive = DeviceAdminStatus.isActive(context),
        isCameraPermissionGranted = context.hasPermission(Manifest.permission.CAMERA),
        isLocationPermissionGranted = context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION),
        settings = settings,
        historyAvailable = true,
        exportAvailable = proFeatureGate.isAllowed(ProFeature.ExportZip, monetizationState),
        shareChooserAvailable = chooserAvailable,
        monetizationState = monetizationState,
        appVersion = deviceInfo.appVersion,
        androidVersion = deviceInfo.androidVersion,
        deviceModel = deviceInfo.deviceModel
    )

    DiagnosticsContent(
        contentPadding = contentPadding,
        checks = mapper.checks(input),
        checklistMarkdown = RuntimeVerificationChecklist.asMarkdown(),
        actionStatus = actionStatus,
        canRunVideo = proFeatureGate.isAllowed(ProFeature.VideoCapture, monetizationState),
        canRunLocation = proFeatureGate.isAllowed(ProFeature.LocationSnapshot, monetizationState),
        canRunExport = proFeatureGate.isAllowed(ProFeature.ExportZip, monetizationState),
        onTestPhoto = {
            scope.launch {
                actionStatus = "Testing photo capture..."
                actionStatus = when (val result = Camera2PhotoCaptureClient(context).captureFrontPhoto()) {
                    is PhotoCaptureResult.Success -> "Photo diagnostic PASS: ${result.file.absolutePath}"
                    is PhotoCaptureResult.Failure -> "Photo diagnostic FAIL: ${result.reason}"
                }
            }
        },
        onTestVideo = {
            scope.launch {
                actionStatus = "Testing video capture..."
                actionStatus = when (val result = Camera2VideoCaptureClient(context).captureFrontVideo(settings.videoDurationSeconds)) {
                    is VideoCaptureResult.Success -> "Video diagnostic PASS: ${result.file.absolutePath}"
                    is VideoCaptureResult.Failure -> "Video diagnostic FAIL: ${result.reason}"
                }
            }
        },
        onTestLocation = {
            scope.launch {
                actionStatus = "Testing location snapshot..."
                actionStatus = when (val result = AndroidLocationSnapshotClient(context).captureLocationSnapshot()) {
                    is LocationSnapshotResult.Success -> "Location diagnostic PASS: ${result.latitude}, ${result.longitude}"
                    is LocationSnapshotResult.Unavailable -> "Location diagnostic UNAVAILABLE: ${result.reason}"
                    is LocationSnapshotResult.Failure -> "Location diagnostic FAIL: ${result.reason}"
                }
            }
        },
        onTestExport = {
            scope.launch {
                actionStatus = "Testing export generation..."
                val exportIncidents = incidents.ifEmpty { listOf(diagnosticPlaceholderIncident(deviceInfo)) }
                val result = exporter.exportIncidents(exportIncidents, filePrefix = "lockwitness_diagnostic")
                actionStatus = "Export diagnostic PASS: ${result.file.absolutePath}"
            }
        },
        onTestShareChooser = {
            scope.launch {
                actionStatus = "Testing share chooser availability..."
                val exportIncidents = incidents.ifEmpty { listOf(diagnosticPlaceholderIncident(deviceInfo)) }
                val result = exporter.exportIncidents(exportIncidents, filePrefix = "lockwitness_diagnostic_share")
                val intent = shareIntentBuilder.buildChooserIntent(result.file)
                chooserAvailable = intent.resolveActivity(context.packageManager) != null
                actionStatus = if (chooserAvailable == true) {
                    "Share chooser diagnostic PASS"
                } else {
                    "Share chooser diagnostic UNAVAILABLE"
                }
            }
        }
    )
}

@Composable
internal fun DiagnosticsContent(
    contentPadding: PaddingValues,
    checks: List<DiagnosticCheck>,
    checklistMarkdown: String,
    actionStatus: String,
    canRunVideo: Boolean,
    canRunLocation: Boolean,
    canRunExport: Boolean,
    onTestPhoto: () -> Unit,
    onTestVideo: () -> Unit,
    onTestLocation: () -> Unit,
    onTestExport: () -> Unit,
    onTestShareChooser: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Diagnostics",
            style = MaterialTheme.typography.headlineMedium
        )
        DiagnosticSection(title = "Checks") {
            checks.forEach { check ->
                Text("${check.result}: ${check.name} - ${check.detail}")
            }
        }
        DiagnosticSection(title = "Manual diagnostics") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onTestPhoto) { Text("Photo") }
                Button(onClick = onTestVideo, enabled = canRunVideo) { Text("Video") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onTestLocation, enabled = canRunLocation) { Text("Location") }
                Button(onClick = onTestExport, enabled = canRunExport) { Text("Export") }
            }
            OutlinedButton(
                onClick = onTestShareChooser,
                enabled = canRunExport
            ) {
                Text("Share Chooser")
            }
            Text(actionStatus)
        }
        DiagnosticSection(title = "Runtime Verification Checklist") {
            Text(checklistMarkdown)
        }
    }
}

@Composable
private fun DiagnosticSection(
    title: String,
    content: @Composable () -> Unit
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
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            content()
        }
    }
}

private fun android.content.Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

private fun diagnosticPlaceholderIncident(deviceInfo: AndroidDeviceInfoProvider): SecurityIncident =
    SecurityIncident(
        timestamp = System.currentTimeMillis(),
        triggerType = "DIAGNOSTIC_EXPORT",
        failedAttemptCount = 0,
        photoEnabled = false,
        videoEnabled = false,
        locationEnabled = false,
        emailEnabled = false,
        shareEnabled = false,
        timelineEnabled = true,
        photoPath = null,
        videoPath = null,
        latitude = null,
        longitude = null,
        locationAccuracy = null,
        locationProvider = null,
        imageSha256 = null,
        videoSha256 = null,
        deviceModel = deviceInfo.deviceModel,
        androidVersion = deviceInfo.androidVersion,
        appVersion = deviceInfo.appVersion,
        photoStatus = "NOT_ATTEMPTED",
        videoStatus = "NOT_ATTEMPTED",
        locationStatus = "NOT_ATTEMPTED",
        emailStatus = "DISABLED",
        shareStatus = "DISABLED",
        notes = "Diagnostic placeholder export only; not stored as an incident."
    )
