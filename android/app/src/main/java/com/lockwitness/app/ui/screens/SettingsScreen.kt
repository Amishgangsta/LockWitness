package com.lockwitness.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.lockwitness.app.location.AndroidLocationSnapshotClient
import com.lockwitness.app.location.LocationSnapshotResult
import com.lockwitness.app.monetization.BannerAdPlaceholder
import com.lockwitness.app.monetization.MonetizationRepository
import com.lockwitness.app.monetization.MonetizationState
import com.lockwitness.app.monetization.ProFeature
import com.lockwitness.app.monetization.ProFeatureGate
import com.lockwitness.app.photo.Camera2PhotoCaptureClient
import com.lockwitness.app.photo.PhotoCaptureResult
import com.lockwitness.app.ui.components.ForensicCard
import com.lockwitness.app.ui.components.SectionEyebrow
import com.lockwitness.app.ui.theme.LockWitnessBackground
import com.lockwitness.app.ui.theme.LockWitnessBorder
import com.lockwitness.app.ui.theme.LockWitnessPrimary
import com.lockwitness.app.ui.theme.LockWitnessPrimaryDark
import com.lockwitness.app.ui.theme.LockWitnessTextSecondary
import com.lockwitness.app.video.Camera2VideoCaptureClient
import com.lockwitness.app.video.VideoCaptureResult
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repository = remember(context) { SettingsRepository.create(context) }
    val monetizationRepository = remember(context) { MonetizationRepository.create(context) }
    val proFeatureGate = remember { ProFeatureGate() }
    val settings by repository.settings.collectAsState(initial = SettingsState.Defaults)
    val monetizationState by monetizationRepository.state.collectAsState(initial = MonetizationState.Free)
    val scope = rememberCoroutineScope()
    val canUseVideo = proFeatureGate.isAllowed(ProFeature.VideoCapture, monetizationState)
    val canUseLocation = proFeatureGate.isAllowed(ProFeature.LocationSnapshot, monetizationState)
    var isDeviceAdminActive by remember(context) {
        mutableStateOf(DeviceAdminStatus.isActive(context))
    }
    var isCameraPermissionGranted by remember(context) {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var isLocationPermissionGranted by remember(context) {
        mutableStateOf(context.hasLocationPermission())
    }
    var testPhotoStatus by remember { mutableStateOf("") }
    var testVideoStatus by remember { mutableStateOf("") }
    var testLocationStatus by remember { mutableStateOf("") }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isCameraPermissionGranted = granted
        testPhotoStatus = if (granted) "Camera permission granted." else "Camera permission denied."
        testVideoStatus = testPhotoStatus
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        isLocationPermissionGranted = grants.values.any { it }
        testLocationStatus = if (isLocationPermissionGranted) "Location permission granted." else "Location permission denied."
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isDeviceAdminActive = DeviceAdminStatus.isActive(context)
                isCameraPermissionGranted =
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED
                isLocationPermissionGranted = context.hasLocationPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Device Admin status banner
        if (!isDeviceAdminActive) {
            ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = LockWitnessPrimary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Device Admin Required",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Activate to enable failed-unlock monitoring",
                            style = MaterialTheme.typography.bodySmall,
                            color = LockWitnessTextSecondary
                        )
                    }
                    Button(
                        onClick = { context.startActivity(DeviceAdminStatus.activationIntent(context)) },
                        colors = ButtonDefaults.buttonColors(containerColor = LockWitnessPrimary)
                    ) {
                        Text("Activate", color = Color.White)
                    }
                }
            }
        }

        // MONITORING section
        ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                SectionEyebrow("Monitoring")
                Spacer(modifier = Modifier.height(12.dp))

                SettingsToggleRow(
                    icon = Icons.Outlined.VerifiedUser,
                    title = "Monitoring",
                    description = if (isDeviceAdminActive) "Monitor failed unlock attempts" else "Requires Device Admin",
                    checked = settings.masterMonitoringEnabled && isDeviceAdminActive,
                    onCheckedChange = { enabled ->
                        if (enabled && !isDeviceAdminActive) {
                            context.startActivity(DeviceAdminStatus.activationIntent(context))
                        } else {
                            scope.launch { repository.setMasterMonitoringEnabled(enabled) }
                        }
                    }
                )
                SettingsDivider()

                SettingsToggleRow(
                    icon = Icons.Outlined.CameraAlt,
                    title = "Capture Photos",
                    description = if (isCameraPermissionGranted) "Capture photo on failed unlock" else "Camera permission required",
                    checked = settings.photoCaptureEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && !isCameraPermissionGranted) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            scope.launch { repository.setPhotoCaptureEnabled(enabled) }
                        }
                    }
                )
                SettingsDivider()

                SettingsToggleRow(
                    icon = Icons.Outlined.Videocam,
                    title = "Record Videos",
                    description = if (canUseVideo) "Record short video on failed unlock" else "Pro feature",
                    checked = settings.videoCaptureEnabled && canUseVideo,
                    enabled = canUseVideo,
                    trailingBadge = if (!canUseVideo) "Pro" else null,
                    onCheckedChange = { enabled ->
                        scope.launch { repository.setVideoCaptureEnabled(enabled) }
                    }
                )
                SettingsDivider()

                SettingsToggleRow(
                    icon = Icons.Outlined.Place,
                    title = "Capture Location",
                    description = if (canUseLocation) {
                        if (isLocationPermissionGranted) "Capture GPS location on failed unlock" else "Location permission required"
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

        // SECURITY & STORAGE section
        ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = false) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                SectionEyebrow("Security & Storage")
                Spacer(modifier = Modifier.height(12.dp))

                SettingsToggleRow(
                    icon = Icons.Outlined.Shield,
                    title = "SHA-256 Hashing",
                    description = "Cryptographic integrity for all evidence",
                    checked = settings.evidenceHashingEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch { repository.setEvidenceHashingEnabled(enabled) }
                    }
                )
                SettingsDivider()

                SettingsToggleRow(
                    icon = Icons.Outlined.Lock,
                    title = "Local Timeline",
                    description = "Keep incident records on this device",
                    checked = settings.localTimelineEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch { repository.setLocalTimelineEnabled(enabled) }
                    }
                )
                SettingsDivider()

                SettingsChevronRow(
                    icon = Icons.Outlined.Delete,
                    title = "Auto Delete Old Evidence",
                    trailingText = "Off"
                )
                SettingsDivider()

                SettingsChevronRow(
                    icon = Icons.Outlined.Storage,
                    title = "Storage Usage",
                    trailingText = "View details"
                )
            }
        }

        // SYSTEM section
        ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = false) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                SectionEyebrow("System")
                Spacer(modifier = Modifier.height(12.dp))

                SettingsStatusRow(
                    icon = Icons.Outlined.Security,
                    title = "Device Admin",
                    statusText = if (isDeviceAdminActive) "Active" else "Inactive",
                    statusOk = isDeviceAdminActive
                )
                SettingsDivider()

                SettingsStatusRow(
                    icon = Icons.Outlined.Build,
                    title = "Billing",
                    statusText = if (monetizationState.isPro) "Pro" else if (monetizationState.billingAvailable) "Free" else "Unavailable",
                    statusOk = monetizationState.billingAvailable
                )
                SettingsDivider()

                SettingsStatusRow(
                    icon = Icons.Outlined.CameraAlt,
                    title = "Camera Permission",
                    statusText = if (isCameraPermissionGranted) "Granted" else "Not granted",
                    statusOk = isCameraPermissionGranted
                )
                SettingsDivider()

                SettingsStatusRow(
                    icon = Icons.Outlined.Place,
                    title = "Location Permission",
                    statusText = if (isLocationPermissionGranted) "Granted" else "Not granted",
                    statusOk = isLocationPermissionGranted
                )
            }
        }

        // Test capture section (only shown when camera is granted)
        if (isCameraPermissionGranted) {
            ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = false) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionEyebrow("Self Test")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    testPhotoStatus = "Capturing..."
                                    testPhotoStatus = when (val r = Camera2PhotoCaptureClient(context).captureFrontPhoto()) {
                                        is PhotoCaptureResult.Success -> "Photo saved: ${r.file.name}"
                                        is PhotoCaptureResult.Failure -> "Failed: ${r.reason}"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LockWitnessPrimary)
                        ) {
                            Text("Test Photo", color = Color.White)
                        }
                        if (canUseVideo) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        testVideoStatus = "Capturing..."
                                        testVideoStatus = when (val r = Camera2VideoCaptureClient(context).captureFrontVideo(settings.videoDurationSeconds)) {
                                            is VideoCaptureResult.Success -> "Video saved: ${r.file.name}"
                                            is VideoCaptureResult.Failure -> "Failed: ${r.reason}"
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LockWitnessPrimary)
                            ) {
                                Text("Test Video", color = Color.White)
                            }
                        }
                    }
                    if (testPhotoStatus.isNotEmpty()) {
                        Text(testPhotoStatus, style = MaterialTheme.typography.bodySmall, color = LockWitnessTextSecondary)
                    }
                    if (testVideoStatus.isNotEmpty()) {
                        Text(testVideoStatus, style = MaterialTheme.typography.bodySmall, color = LockWitnessTextSecondary)
                    }
                }
            }
        }

        // Video duration (Pro)
        if (canUseVideo) {
            ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = false) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionEyebrow("Video Duration")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsState.AllowedVideoDurations.forEach { duration ->
                            FilterChip(
                                selected = settings.videoDurationSeconds == duration,
                                onClick = { scope.launch { repository.setVideoDurationSeconds(duration) } },
                                label = { Text("${duration}s") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LockWitnessPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = settings.videoDurationSeconds == duration,
                                    borderColor = LockWitnessBorder,
                                    selectedBorderColor = LockWitnessPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Location test
        if (canUseLocation) {
            ForensicCard(modifier = Modifier.fillMaxWidth(), elevated = false) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionEyebrow("Location Test")
                    if (!isLocationPermissionGranted) {
                        Button(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LockWitnessPrimary)
                        ) {
                            Text("Grant Location Permission", color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = {
                                scope.launch {
                                    testLocationStatus = "Capturing location..."
                                    testLocationStatus = when (val r = AndroidLocationSnapshotClient(context).captureLocationSnapshot()) {
                                        is LocationSnapshotResult.Success -> "Location: ${r.latitude}, ${r.longitude}"
                                        is LocationSnapshotResult.Unavailable -> "Unavailable: ${r.reason}"
                                        is LocationSnapshotResult.Failure -> "Failed: ${r.reason}"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LockWitnessPrimary)
                        ) {
                            Text("Test Location", color = Color.White)
                        }
                    }
                    if (testLocationStatus.isNotEmpty()) {
                        Text(testLocationStatus, style = MaterialTheme.typography.bodySmall, color = LockWitnessTextSecondary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        BannerAdPlaceholder(state = monetizationState)
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 10.dp),
        color = LockWitnessBorder
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean = true,
    trailingBadge: String? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) LockWitnessPrimary else LockWitnessTextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (trailingBadge != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = LockWitnessPrimaryDark.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = trailingBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = LockWitnessTextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedTrackColor = LockWitnessPrimary,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = LockWitnessBorder,
                uncheckedBorderColor = LockWitnessBorder
            )
        )
    }
}

@Composable
private fun SettingsChevronRow(
    icon: ImageVector,
    title: String,
    trailingText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LockWitnessTextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = trailingText,
            style = MaterialTheme.typography.bodySmall,
            color = LockWitnessTextSecondary
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = LockWitnessTextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingsStatusRow(
    icon: ImageVector,
    title: String,
    statusText: String,
    statusOk: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LockWitnessTextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (statusOk) Icons.Outlined.CheckCircle else Icons.Outlined.Warning,
            contentDescription = null,
            tint = if (statusOk) LockWitnessPrimary else LockWitnessTextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelMedium,
            color = if (statusOk) LockWitnessPrimary else LockWitnessTextSecondary
        )
    }
}

private fun android.content.Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
