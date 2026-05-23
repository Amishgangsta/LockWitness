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
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.lockwitness.app.ui.components.SectionEyebrow
import com.lockwitness.app.ui.theme.LWActionOrange
import com.lockwitness.app.ui.theme.LWSectionBlue
import com.lockwitness.app.ui.theme.LWBackground
import com.lockwitness.app.ui.theme.LWChrome
import com.lockwitness.app.ui.theme.LWDiagDivider
import com.lockwitness.app.ui.theme.LWDiagDisabledBtn
import com.lockwitness.app.ui.theme.LWDiagDisabledText
import com.lockwitness.app.ui.theme.LWSuccessGreen
import com.lockwitness.app.ui.theme.LWTextPrimary
import com.lockwitness.app.ui.theme.LockWitnessBorder
import com.lockwitness.app.ui.theme.LockWitnessPrimary
import com.lockwitness.app.ui.theme.LockWitnessTextSecondary
import com.lockwitness.app.ui.theme.LockWitnessWarning
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
                    is LocationSnapshotResult.Unavailable -> "UNAVAILABLE — Location: ${r.reason}"
                    is LocationSnapshotResult.Failure -> "FAIL — Location: ${r.reason}"
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
    val failCount = checks.count { it.result == DiagnosticResult.FAIL }
    val allClear = failCount == 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LWBackground)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // System status summary card
        ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (allClear) Icons.Outlined.CheckCircle else Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = if (allClear) LWSuccessGreen else LockWitnessWarning,
                    modifier = Modifier.size(32.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    SectionEyebrow("System Status")
                    Text(
                        text = if (allClear) "All Systems Normal" else "$failCount issue${if (failCount != 1) "s" else ""} detected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$passCount of ${checks.size} checks passed",
                        style = MaterialTheme.typography.bodySmall,
                        color = LockWitnessTextSecondary
                    )
                }
            }
        }

        // Diagnostic checks
        ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = false) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                SectionEyebrow("Checks")
                Spacer(modifier = Modifier.height(12.dp))
                checks.forEachIndexed { index, check ->
                    DiagnosticCheckRow(check)
                    if (index < checks.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = LWDiagDivider
                        )
                    }
                }
            }
        }

        // Manual diagnostics
        ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = false) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionEyebrow("Self Test")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DiagButton(
                        label = "Photo",
                        icon = Icons.Outlined.CameraAlt,
                        onClick = onTestPhoto,
                        enabled = true,
                        modifier = Modifier.weight(1f)
                    )
                    DiagButton(
                        label = "Video",
                        icon = Icons.Outlined.Videocam,
                        onClick = onTestVideo,
                        enabled = canRunVideo,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DiagButton(
                        label = "Location",
                        icon = Icons.Outlined.Place,
                        onClick = onTestLocation,
                        enabled = canRunLocation,
                        modifier = Modifier.weight(1f)
                    )
                    DiagButton(
                        label = "Export",
                        icon = Icons.Outlined.FileDownload,
                        onClick = onTestExport,
                        enabled = canRunExport,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (actionStatus.isNotEmpty()) {
                    Text(
                        text = when {
                            actionStatus.startsWith("PASS") -> "Capture successful."
                            actionStatus.startsWith("FAIL") -> "Capture failed. Check permissions in Settings."
                            actionStatus.startsWith("UNAVAILABLE") -> "Unavailable. Check permissions in Settings."
                            else -> "Running…"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            actionStatus.startsWith("PASS") -> LWSuccessGreen
                            actionStatus.startsWith("FAIL") || actionStatus.startsWith("UNAVAILABLE") -> LockWitnessPrimary
                            else -> LWChrome
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun DiagnosticCheckRow(check: DiagnosticCheck) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val (icon, tint) = when (check.result) {
            DiagnosticResult.PASS -> Icons.Outlined.CheckCircle to LWSuccessGreen
            DiagnosticResult.FAIL -> Icons.Outlined.Error to LockWitnessPrimary
            DiagnosticResult.WARNING -> Icons.Outlined.Warning to LockWitnessWarning
            DiagnosticResult.NOT_TESTED -> Icons.Outlined.Help to LWDiagDisabledText
            DiagnosticResult.UNAVAILABLE -> Icons.Outlined.Info to LWDiagDisabledText
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = check.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = LWTextPrimary
            )
            Text(
                text = check.detail,
                style = MaterialTheme.typography.bodySmall,
                color = LWChrome
            )
        }
        Text(
            text = check.result.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = tint
        )
    }
}

@Composable
private fun DiagButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = LWSectionBlue,
            disabledContainerColor = LWDiagDisabledBtn,
            contentColor = Color.White,
            disabledContentColor = LWDiagDisabledText
        )
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp))
        Text(" $label")
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
