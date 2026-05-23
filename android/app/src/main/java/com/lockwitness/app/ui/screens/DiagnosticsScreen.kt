package com.lockwitness.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
import com.lockwitness.app.diagnostics.DiagnosticResult
import com.lockwitness.app.export.LocalIncidentExporter
import com.lockwitness.app.location.AndroidLocationSnapshotClient
import com.lockwitness.app.location.LocationSnapshotResult
import com.lockwitness.app.monetization.MonetizationRepository
import com.lockwitness.app.monetization.MonetizationState
import com.lockwitness.app.monetization.ProFeature
import com.lockwitness.app.monetization.ProFeatureGate
import com.lockwitness.app.photo.Camera2PhotoCaptureClient
import com.lockwitness.app.photo.PhotoCaptureResult
import com.lockwitness.app.ui.components.ForensicCard
import com.lockwitness.app.ui.components.ForensicDivider
import com.lockwitness.app.ui.components.SectionEyebrow
import com.lockwitness.app.ui.components.StatusPill
import com.lockwitness.app.ui.theme.CautionAmber
import com.lockwitness.app.ui.theme.DestructiveRed
import com.lockwitness.app.ui.theme.GraphiteBg
import com.lockwitness.app.ui.theme.StrokeSubtle
import com.lockwitness.app.ui.theme.TextPrimary
import com.lockwitness.app.ui.theme.TextSecondary
import com.lockwitness.app.ui.theme.VerifiedGreen
import com.lockwitness.app.video.Camera2VideoCaptureClient
import com.lockwitness.app.video.VideoCaptureResult
import kotlinx.coroutines.launch

@Composable
fun DiagnosticsScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    val settingsRepository = remember(context) { SettingsRepository.create(context) }
    val incidentRepository = remember(context) {
        SecurityIncidentRepository(LockWitnessDatabase.getInstance(context).securityIncidentDao())
    }
    val monetizationRepository = remember(context) { MonetizationRepository.create(context) }
    val settings by settingsRepository.settings.collectAsState(initial = SettingsState.Defaults)
    val monetizationState by monetizationRepository.state.collectAsState(initial = MonetizationState(isPro = true, billingAvailable = false))
    val incidents by incidentRepository.getAllOrderedByTimestampDesc().collectAsState(initial = emptyList())
    val proFeatureGate = remember { ProFeatureGate() }
    val mapper = remember { DiagnosticMapper() }
    val deviceInfo = remember(context) { AndroidDeviceInfoProvider(context) }
    val exporter = remember(context) { LocalIncidentExporter(context) }
    val scope = rememberCoroutineScope()
    var actionStatus by remember { mutableStateOf("") }

    val input = DiagnosticInput(
        isDeviceAdminActive = DeviceAdminStatus.isActive(context),
        isCameraPermissionGranted = context.hasPermission(Manifest.permission.CAMERA),
        isLocationPermissionGranted = context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION),
        settings = settings,
        historyAvailable = true,
        exportAvailable = proFeatureGate.isAllowed(ProFeature.ExportZip, monetizationState),
        monetizationState = monetizationState,
        appVersion = deviceInfo.appVersion,
        androidVersion = deviceInfo.androidVersion,
        deviceModel = deviceInfo.deviceModel
    )

    DiagnosticsContent(
        contentPadding = contentPadding,
        checks = mapper.checks(input),
        actionStatus = actionStatus,
        canRunVideo = proFeatureGate.isAllowed(ProFeature.VideoCapture, monetizationState),
        canRunLocation = proFeatureGate.isAllowed(ProFeature.LocationSnapshot, monetizationState),
        canRunExport = proFeatureGate.isAllowed(ProFeature.ExportZip, monetizationState),
        onTestPhoto = {
            scope.launch {
                actionStatus = "Testing photo capture…"
                actionStatus = when (val r = Camera2PhotoCaptureClient(context).captureFrontPhoto()) {
                    is PhotoCaptureResult.Success -> "PASS — Photo: ${r.file.name}"
                    is PhotoCaptureResult.Failure -> "FAIL — Photo: ${r.reason}"
                }
            }
        },
        onTestVideo = {
            scope.launch {
                actionStatus = "Testing video capture…"
                actionStatus = when (val r = Camera2VideoCaptureClient(context).captureFrontVideo(settings.videoDurationSeconds)) {
                    is VideoCaptureResult.Success -> "PASS — Video: ${r.file.name}"
                    is VideoCaptureResult.Failure -> "FAIL — Video: ${r.reason}"
                }
            }
        },
        onTestLocation = {
            scope.launch {
                actionStatus = "Testing location snapshot…"
                actionStatus = when (val r = AndroidLocationSnapshotClient(context).captureLocationSnapshot()) {
                    is LocationSnapshotResult.Success -> "PASS — Location: ${r.latitude}, ${r.longitude}"
                    is LocationSnapshotResult.Unavailable -> "UNAVAILABLE — ${r.reason}"
                    is LocationSnapshotResult.Failure -> "FAIL — ${r.reason}"
                }
            }
        },
        onTestExport = {
            scope.launch {
                actionStatus = "Testing export generation…"
                val exportIncidents = incidents.ifEmpty { listOf(diagnosticPlaceholderIncident(deviceInfo)) }
                val result = exporter.exportIncidents(exportIncidents, filePrefix = "lockwitness_diagnostic")
                actionStatus = "PASS — Export: ${result.file.name}"
            }
        }
    )
}

@Composable
internal fun DiagnosticsContent(
    contentPadding: PaddingValues,
    checks: List<DiagnosticCheck>,
    actionStatus: String,
    canRunVideo: Boolean,
    canRunLocation: Boolean,
    canRunExport: Boolean,
    onTestPhoto: () -> Unit,
    onTestVideo: () -> Unit,
    onTestLocation: () -> Unit,
    onTestExport: () -> Unit
) {
    val passCount = checks.count { it.result == DiagnosticResult.PASS }
    val totalCount = checks.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GraphiteBg)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Diagnostics",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        // Readiness score card
        ForensicCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Speed, contentDescription = null, tint = if (passCount == totalCount) VerifiedGreen else CautionAmber, modifier = Modifier.size(28.dp))
                Column(modifier = Modifier.weight(1f)) {
                    SectionEyebrow("Readiness Score")
                    Text(
                        text = "$passCount / $totalCount checks passing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (passCount == totalCount) "Evidence system fully ready." else "Evidence system mostly ready.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Runtime tests
        ForensicCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionEyebrow("Runtime Tests")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DiagTestButton(label = "Photo", icon = Icons.Outlined.CameraAlt, enabled = true, onClick = onTestPhoto, modifier = Modifier.weight(1f))
                    DiagTestButton(label = "Video", icon = Icons.Outlined.Videocam, enabled = canRunVideo, onClick = onTestVideo, modifier = Modifier.weight(1f))
                    DiagTestButton(label = "Location", icon = Icons.Outlined.LocationOn, enabled = canRunLocation, onClick = onTestLocation, modifier = Modifier.weight(1f))
                    DiagTestButton(label = "Export", icon = Icons.Outlined.Archive, enabled = canRunExport, onClick = onTestExport, modifier = Modifier.weight(1f))
                }
                if (actionStatus.isNotEmpty()) {
                    val color = when {
                        actionStatus.startsWith("PASS") -> VerifiedGreen
                        actionStatus.startsWith("FAIL") -> DestructiveRed
                        actionStatus.startsWith("UNAVAILABLE") -> CautionAmber
                        else -> TextSecondary
                    }
                    Text(
                        text = actionStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = color
                    )
                }
            }
        }

        // Checks list
        ForensicCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                SectionEyebrow("Checks")
                Spacer(modifier = Modifier.height(10.dp))
                checks.forEachIndexed { index, check ->
                    DiagCheckRow(check = check)
                    if (index < checks.lastIndex) {
                        ForensicDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun DiagTestButton(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (enabled) TextPrimary else TextSecondary
        ),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun DiagCheckRow(check: DiagnosticCheck) {
    val (pillText, pillColor, icon) = when (check.result) {
        DiagnosticResult.PASS -> Triple("Pass", VerifiedGreen, Icons.Outlined.CheckCircle)
        DiagnosticResult.FAIL -> Triple("Fail", DestructiveRed, Icons.Outlined.Error)
        DiagnosticResult.WARNING -> Triple("Warning", CautionAmber, Icons.Outlined.Warning)
        DiagnosticResult.UNAVAILABLE -> Triple("Unavailable", CautionAmber, Icons.Outlined.Info)
        DiagnosticResult.NOT_TESTED -> Triple("Not Tested", TextSecondary, Icons.Outlined.Info)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = pillColor, modifier = Modifier.size(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(check.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(check.detail, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        StatusPill(text = pillText, color = pillColor)
    }
}

private fun android.content.Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

private fun diagnosticPlaceholderIncident(deviceInfo: AndroidDeviceInfoProvider): SecurityIncident =
    SecurityIncident(
        timestamp = System.currentTimeMillis(),
        triggerType = "DIAGNOSTIC_EXPORT",
        failedAttemptCount = 0,
        photoEnabled = false, videoEnabled = false, locationEnabled = false,
        emailEnabled = false, shareEnabled = false, timelineEnabled = true,
        photoPath = null, videoPath = null, latitude = null, longitude = null,
        locationAccuracy = null, locationProvider = null,
        imageSha256 = null, videoSha256 = null,
        deviceModel = deviceInfo.deviceModel,
        androidVersion = deviceInfo.androidVersion,
        appVersion = deviceInfo.appVersion,
        photoStatus = "NOT_ATTEMPTED", videoStatus = "NOT_ATTEMPTED",
        locationStatus = "NOT_ATTEMPTED", emailStatus = "DISABLED", shareStatus = "DISABLED",
        notes = "Diagnostic placeholder export only; not stored as an incident."
    )
