package com.lockwitness.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lockwitness.app.admin.DeviceAdminStatus
import com.lockwitness.app.data.SettingsRepository
import com.lockwitness.app.data.SettingsState
import com.lockwitness.app.monetization.BannerAdPlaceholder
import com.lockwitness.app.monetization.MonetizationRepository
import com.lockwitness.app.monetization.MonetizationState
import com.lockwitness.app.monetization.ProFeature
import com.lockwitness.app.monetization.ProFeatureGate
import com.lockwitness.app.ui.components.ForensicCard
import com.lockwitness.app.ui.components.ForensicDivider
import com.lockwitness.app.ui.components.SectionEyebrow
import com.lockwitness.app.ui.components.StatusPill
import com.lockwitness.app.ui.theme.CautionAmber
import com.lockwitness.app.ui.theme.GraphiteBg
import com.lockwitness.app.ui.theme.HashText
import com.lockwitness.app.ui.theme.MutedChip
import com.lockwitness.app.ui.theme.ProOrange
import com.lockwitness.app.ui.theme.StrokeSubtle
import com.lockwitness.app.ui.theme.TextPrimary
import com.lockwitness.app.ui.theme.TextSecondary
import com.lockwitness.app.ui.theme.VerifiedGreen
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    onNavigateToSetup: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repository = remember(context) { SettingsRepository.create(context) }
    val monetizationRepository = remember(context) { MonetizationRepository.create(context) }
    val proFeatureGate = remember { ProFeatureGate() }
    val settings by repository.settings.collectAsState(initial = SettingsState.Defaults)
    val monetizationState by monetizationRepository.state.collectAsState(initial = MonetizationState(isPro = true, billingAvailable = false))
    val scope = rememberCoroutineScope()
    val canUseVideo = proFeatureGate.isAllowed(ProFeature.VideoCapture, monetizationState)
    val canUseLocation = proFeatureGate.isAllowed(ProFeature.LocationSnapshot, monetizationState)

    var isDeviceAdminActive by remember(context) { mutableStateOf(DeviceAdminStatus.isActive(context)) }
    var isCameraPermissionGranted by remember(context) {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var isLocationPermissionGranted by remember(context) { mutableStateOf(context.hasLocationPermission()) }
    var showAutoDeleteDialog by remember { mutableStateOf(false) }
    var showStorageDialog by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isCameraPermissionGranted = granted
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        isLocationPermissionGranted = grants.values.any { it }
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isDeviceAdminActive = DeviceAdminStatus.isActive(context)
                isCameraPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                isLocationPermissionGranted = context.hasLocationPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        // PROTECTION section
        ForensicCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                SectionEyebrow("Protection")
                Spacer(modifier = Modifier.height(10.dp))

                SettingsToggleRow(
                    icon = Icons.Outlined.VerifiedUser,
                    title = "Monitoring",
                    description = if (isDeviceAdminActive) "Detect failed unlock attempts" else "Requires Device Admin — tap Setup",
                    checked = settings.masterMonitoringEnabled && isDeviceAdminActive,
                    statusPillText = if (isDeviceAdminActive) "Active" else "Setup needed",
                    statusPillColor = if (isDeviceAdminActive) VerifiedGreen else CautionAmber,
                    onCheckedChange = { enabled ->
                        if (enabled && !isDeviceAdminActive) {
                            onNavigateToSetup()
                        } else {
                            scope.launch { repository.setMasterMonitoringEnabled(enabled) }
                        }
                    }
                )
                ForensicDivider(modifier = Modifier.padding(vertical = 6.dp))

                SettingsChevronRow(
                    icon = Icons.Outlined.Shield,
                    title = "Run Setup Wizard",
                    description = "Grant permissions and activate Device Admin",
                    onClick = onNavigateToSetup
                )
            }
        }

        // EVIDENCE CAPTURE section
        ForensicCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                SectionEyebrow("Evidence Capture")
                Spacer(modifier = Modifier.height(10.dp))

                SettingsToggleRow(
                    icon = Icons.Outlined.CameraAlt,
                    title = "Capture Photos",
                    description = if (isCameraPermissionGranted) "Front camera on failed unlock" else "Camera permission required",
                    checked = settings.photoCaptureEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && !isCameraPermissionGranted) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            scope.launch { repository.setPhotoCaptureEnabled(enabled) }
                        }
                    }
                )
                ForensicDivider(modifier = Modifier.padding(vertical = 6.dp))

                SettingsToggleRow(
                    icon = Icons.Outlined.Videocam,
                    title = "Record Video",
                    description = if (canUseVideo) "Short clip on failed unlock" else "Pro feature",
                    checked = settings.videoCaptureEnabled && canUseVideo,
                    enabled = canUseVideo,
                    trailingBadge = if (!canUseVideo) "Pro" else null,
                    onCheckedChange = { enabled ->
                        scope.launch { repository.setVideoCaptureEnabled(enabled) }
                    }
                )

                if (canUseVideo) {
                    ForensicDivider(modifier = Modifier.padding(vertical = 6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Videocam, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                        Text("Video Duration", style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SettingsState.AllowedVideoDurations.forEach { duration ->
                                FilterChip(
                                    selected = settings.videoDurationSeconds == duration,
                                    onClick = { scope.launch { repository.setVideoDurationSeconds(duration) } },
                                    label = { Text("${duration}s", style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MutedChip,
                                        selectedLabelColor = VerifiedGreen,
                                        containerColor = MutedChip,
                                        labelColor = TextSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = settings.videoDurationSeconds == duration,
                                        borderColor = StrokeSubtle,
                                        selectedBorderColor = VerifiedGreen
                                    )
                                )
                            }
                        }
                    }
                }

                ForensicDivider(modifier = Modifier.padding(vertical = 6.dp))

                SettingsToggleRow(
                    icon = Icons.Outlined.Place,
                    title = "Capture Location",
                    description = if (canUseLocation) {
                        if (isLocationPermissionGranted) "GPS snapshot on failed unlock" else "Location permission required"
                    } else "Pro feature",
                    checked = settings.locationCaptureEnabled && canUseLocation,
                    enabled = canUseLocation,
                    trailingBadge = if (!canUseLocation) "Pro" else null,
                    onCheckedChange = { enabled ->
                        if (enabled && !isLocationPermissionGranted) {
                            locationPermissionLauncher.launch(
                                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                            )
                        } else {
                            scope.launch { repository.setLocationCaptureEnabled(enabled) }
                        }
                    }
                )
            }
        }

        // EVIDENCE INTEGRITY section
        ForensicCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                SectionEyebrow("Evidence Integrity")
                Spacer(modifier = Modifier.height(10.dp))

                SettingsToggleRow(
                    icon = Icons.Outlined.Shield,
                    title = "SHA-256 Hashing",
                    description = "Cryptographic integrity for all evidence",
                    checked = settings.evidenceHashingEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch { repository.setEvidenceHashingEnabled(enabled) }
                    }
                )
                ForensicDivider(modifier = Modifier.padding(vertical = 6.dp))

                SettingsToggleRow(
                    icon = Icons.Outlined.Lock,
                    title = "Local Timeline",
                    description = "Keep incident records on this device",
                    checked = settings.localTimelineEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch { repository.setLocalTimelineEnabled(enabled) }
                    }
                )
            }
        }

        // EVIDENCE STORAGE section
        ForensicCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                SectionEyebrow("Evidence Storage")
                Spacer(modifier = Modifier.height(10.dp))

                SettingsChevronRow(
                    icon = Icons.Outlined.Delete,
                    title = "Auto Delete Old Evidence",
                    description = "Keep storage optimized",
                    trailingText = if (settings.autoDeleteDays == 0) "Off" else "${settings.autoDeleteDays} days",
                    onClick = { showAutoDeleteDialog = true }
                )
                ForensicDivider(modifier = Modifier.padding(vertical = 6.dp))

                SettingsChevronRow(
                    icon = Icons.Outlined.Storage,
                    title = "Storage Usage",
                    description = "View photos and video sizes",
                    onClick = { showStorageDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        BannerAdPlaceholder(state = monetizationState)
    }

    if (showAutoDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showAutoDeleteDialog = false },
            title = { Text("Auto Delete Evidence", color = TextPrimary) },
            containerColor = com.lockwitness.app.ui.theme.CardSurface,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Automatically delete evidence older than:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(0 to "Never (keep all)", 30 to "30 days", 60 to "60 days", 90 to "90 days").forEach { (days, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch { repository.setAutoDeleteDays(days) }
                                    showAutoDeleteDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (settings.autoDeleteDays == days) ProOrange else TextPrimary
                            )
                            if (settings.autoDeleteDays == days) {
                                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = ProOrange, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAutoDeleteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showStorageDialog) {
        val photoDir = remember(context) { java.io.File(context.filesDir, "incident_photos") }
        val videoDir = remember(context) { java.io.File(context.filesDir, "incident_videos") }
        val photoBytes = remember(photoDir) { photoDir.walkTopDown().filter { it.isFile }.sumOf { it.length() } }
        val videoBytes = remember(videoDir) { videoDir.walkTopDown().filter { it.isFile }.sumOf { it.length() } }
        fun Long.toMb(): String = if (this < 1024 * 1024) "${this / 1024} KB" else "${"%.1f".format(this / 1024.0 / 1024.0)} MB"
        AlertDialog(
            onDismissRequest = { showStorageDialog = false },
            title = { Text("Storage Usage", color = TextPrimary) },
            containerColor = com.lockwitness.app.ui.theme.CardSurface,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Photos", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        Text(photoBytes.toMb(), style = MaterialTheme.typography.bodyMedium, color = ProOrange)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Videos", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        Text(videoBytes.toMb(), style = MaterialTheme.typography.bodyMedium, color = ProOrange)
                    }
                    HorizontalDivider(color = StrokeSubtle)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text((photoBytes + videoBytes).toMb(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ProOrange)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStorageDialog = false }) { Text("Done", color = VerifiedGreen) }
            }
        )
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean = true,
    trailingBadge: String? = null,
    statusPillText: String? = null,
    statusPillColor: androidx.compose.ui.graphics.Color? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) HashText else TextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                if (trailingBadge != null) {
                    Text(trailingBadge, style = MaterialTheme.typography.labelSmall, color = ProOrange, fontWeight = FontWeight.SemiBold)
                }
                if (statusPillText != null && statusPillColor != null) {
                    StatusPill(text = statusPillText, color = statusPillColor)
                }
            }
            Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedTrackColor = VerifiedGreen,
                checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                uncheckedTrackColor = MutedChip,
                uncheckedThumbColor = TextSecondary,
                uncheckedBorderColor = MutedChip,
                disabledUncheckedTrackColor = MutedChip.copy(alpha = 0.4f),
                disabledUncheckedThumbColor = TextSecondary.copy(alpha = 0.4f),
                disabledUncheckedBorderColor = MutedChip.copy(alpha = 0.4f)
            )
        )
    }
}

@Composable
private fun SettingsChevronRow(
    icon: ImageVector,
    title: String,
    description: String? = null,
    trailingText: String? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
            if (description != null) {
                Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
        if (trailingText != null) {
            Text(trailingText, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        }
        Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
    }
}

private fun android.content.Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
