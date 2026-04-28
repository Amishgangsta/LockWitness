package com.lockwitness.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
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
    var testPhotoStatus by remember { mutableStateOf("No test photo captured.") }
    var testVideoStatus by remember { mutableStateOf("No test video captured.") }
    var testLocationStatus by remember { mutableStateOf("No test location captured.") }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isCameraPermissionGranted = granted
        testPhotoStatus = if (granted) {
            "Camera permission granted."
        } else {
            "Camera permission denied."
        }
        testVideoStatus = testPhotoStatus
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        isLocationPermissionGranted = grants.values.any { it }
        testLocationStatus = if (isLocationPermissionGranted) {
            "Location permission granted."
        } else {
            "Location permission denied."
        }
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
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Configure LockWitness feature defaults. Capture and alert actions are not implemented in this phase.",
            style = MaterialTheme.typography.bodyMedium
        )

        DeviceAdminStatusCard(
            isActive = isDeviceAdminActive,
            onActivateClick = {
                context.startActivity(DeviceAdminStatus.activationIntent(context))
            }
        )
        MonetizationStatusCard(
            state = monetizationState,
            onRefreshBilling = {
                scope.launch { monetizationRepository.refreshBillingStatus() }
            }
        )
        BannerAdPlaceholder(state = monetizationState)

        SettingsToggleRow(
            title = "Master monitoring",
            description = if (isDeviceAdminActive) {
                "Default: off; Device Admin active"
            } else {
                "Requires Device Admin activation"
            },
            checked = settings.masterMonitoringEnabled && isDeviceAdminActive,
            onCheckedChange = { enabled ->
                if (enabled && !isDeviceAdminActive) {
                    context.startActivity(DeviceAdminStatus.activationIntent(context))
                } else {
                    scope.launch { repository.setMasterMonitoringEnabled(enabled) }
                }
            }
        )
        SettingsToggleRow(
            title = "Photo capture",
            description = if (isCameraPermissionGranted) {
                "Default: on; camera permission granted"
            } else {
                "Default: on; camera permission not granted"
            },
            checked = settings.photoCaptureEnabled,
            onCheckedChange = { enabled ->
                scope.launch { repository.setPhotoCaptureEnabled(enabled) }
            }
        )
        PhotoPermissionAndTestCard(
            isCameraPermissionGranted = isCameraPermissionGranted,
            testPhotoStatus = testPhotoStatus,
            testVideoStatus = testVideoStatus,
            onRequestPermission = {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onTestPhotoCapture = {
                scope.launch {
                    testPhotoStatus = "Attempting test photo capture..."
                    testPhotoStatus = when (val result = Camera2PhotoCaptureClient(context).captureFrontPhoto()) {
                        is PhotoCaptureResult.Success ->
                            "Test photo saved locally: ${result.file.name}"

                        is PhotoCaptureResult.Failure ->
                            "Test photo failed: ${result.reason}"
                    }
                }
            },
            onTestVideoCapture = {
                scope.launch {
                    testVideoStatus = "Attempting ${settings.videoDurationSeconds}s test video capture..."
                    testVideoStatus = when (val result = Camera2VideoCaptureClient(context).captureFrontVideo(settings.videoDurationSeconds)) {
                        is VideoCaptureResult.Success ->
                            "Test video saved locally: ${result.file.name}"

                        is VideoCaptureResult.Failure ->
                            "Test video failed: ${result.reason}"
                    }
                }
            },
            canTestVideo = canUseVideo
        )
        SettingsToggleRow(
            title = "Video capture",
            description = if (canUseVideo) "Default: off; Pro available" else "Pro feature",
            checked = settings.videoCaptureEnabled && canUseVideo,
            enabled = canUseVideo,
            onCheckedChange = { enabled ->
                scope.launch { repository.setVideoCaptureEnabled(enabled) }
            }
        )

        SettingsSectionCard {
            Text(
                text = "Video duration",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Default: 5 seconds",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingsState.AllowedVideoDurations.forEach { duration ->
                    FilterChip(
                        selected = settings.videoDurationSeconds == duration,
                        enabled = canUseVideo,
                        onClick = {
                            scope.launch { repository.setVideoDurationSeconds(duration) }
                        },
                        label = { Text("${duration}s") }
                    )
                }
            }
        }

        SettingsToggleRow(
            title = "GPS/location capture",
            description = if (isLocationPermissionGranted) {
                if (canUseLocation) "Permission status: granted" else "Pro feature; permission granted"
            } else {
                if (canUseLocation) "Permission status: not granted" else "Pro feature; permission not granted"
            },
            checked = settings.locationCaptureEnabled && canUseLocation,
            enabled = canUseLocation,
            onCheckedChange = { enabled ->
                scope.launch { repository.setLocationCaptureEnabled(enabled) }
            }
        )
        LocationPermissionAndTestCard(
            isLocationPermissionGranted = isLocationPermissionGranted,
            testLocationStatus = testLocationStatus,
            onRequestPermission = {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            },
            onTestLocationSnapshot = {
                scope.launch {
                    testLocationStatus = "Attempting test location snapshot..."
                    testLocationStatus = when (val result = AndroidLocationSnapshotClient(context).captureLocationSnapshot()) {
                        is LocationSnapshotResult.Success ->
                            "Test location captured: ${result.latitude}, ${result.longitude}"

                        is LocationSnapshotResult.Unavailable ->
                            "Test location unavailable: ${result.reason}"

                        is LocationSnapshotResult.Failure ->
                            "Test location failed: ${result.reason}"
                    }
                }
            },
            canTestLocation = canUseLocation
        )
        SettingsToggleRow(
            title = "Local timeline",
            description = "Default: on",
            checked = settings.localTimelineEnabled,
            onCheckedChange = { enabled ->
                scope.launch { repository.setLocalTimelineEnabled(enabled) }
            }
        )
        SettingsToggleRow(
            title = "Email alert",
            description = "Permission/configuration status: not configured",
            checked = settings.emailAlertEnabled,
            onCheckedChange = { enabled ->
                scope.launch { repository.setEmailAlertEnabled(enabled) }
            }
        )
        SettingsToggleRow(
            title = "Share alert",
            description = "Permission status: not requested",
            checked = settings.shareAlertEnabled,
            onCheckedChange = { enabled ->
                scope.launch { repository.setShareAlertEnabled(enabled) }
            }
        )
        SettingsToggleRow(
            title = "Evidence hashing",
            description = "Default: on",
            checked = settings.evidenceHashingEnabled,
            onCheckedChange = { enabled ->
                scope.launch { repository.setEvidenceHashingEnabled(enabled) }
            }
        )

        SettingsSectionCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Manual export",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Pro feature; export actions are available from History.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Button(
                    onClick = {},
                    enabled = false
                ) {
                    Text("History")
                }
            }
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(if (proFeatureGate.isAllowed(ProFeature.ExportZip, monetizationState)) "Status: Pro enabled" else "Status: Pro required") }
            )
        }
    }
}

@Composable
private fun MonetizationStatusCard(
    state: MonetizationState,
    onRefreshBilling: () -> Unit
) {
    SettingsSectionCard {
        Text(
            text = "Free / Pro",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = if (state.isPro) {
                "Status: Pro"
            } else {
                "Status: Free"
            },
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = if (state.billingAvailable) {
                "Billing status: available"
            } else {
                "Billing status: unavailable; Free mode remains active"
            },
            style = MaterialTheme.typography.bodyMedium
        )
        Button(onClick = onRefreshBilling) {
            Text("Refresh Billing")
        }
    }
}

@Composable
private fun LocationPermissionAndTestCard(
    isLocationPermissionGranted: Boolean,
    testLocationStatus: String,
    onRequestPermission: () -> Unit,
    onTestLocationSnapshot: () -> Unit,
    canTestLocation: Boolean
) {
    SettingsSectionCard {
        Text(
            text = "Location permission",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = if (isLocationPermissionGranted) {
                "Status: granted"
            } else {
                "Status: not granted. Location snapshots require location access and enabled location services."
            },
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onRequestPermission,
                enabled = !isLocationPermissionGranted
            ) {
                Text(if (isLocationPermissionGranted) "Granted" else "Grant")
            }
            Button(
                onClick = onTestLocationSnapshot,
                enabled = isLocationPermissionGranted && canTestLocation
            ) {
                Text("Test Location")
            }
        }
        Text(
            text = testLocationStatus,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun android.content.Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

@Composable
private fun DeviceAdminStatusCard(
    isActive: Boolean,
    onActivateClick: () -> Unit
) {
    SettingsSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Device Admin",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (isActive) {
                        "Status: active"
                    } else {
                        "Status: inactive"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(
                onClick = onActivateClick,
                enabled = !isActive
            ) {
                Text(if (isActive) "Active" else "Activate")
            }
        }
    }
}

@Composable
private fun PhotoPermissionAndTestCard(
    isCameraPermissionGranted: Boolean,
    testPhotoStatus: String,
    testVideoStatus: String,
    onRequestPermission: () -> Unit,
    onTestPhotoCapture: () -> Unit,
    onTestVideoCapture: () -> Unit,
    canTestVideo: Boolean
) {
    SettingsSectionCard {
        Text(
            text = "Camera permission",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = if (isCameraPermissionGranted) {
                "Status: granted"
            } else {
                "Status: not granted. Photo capture requires camera access."
            },
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onRequestPermission,
                enabled = !isCameraPermissionGranted
            ) {
                Text(if (isCameraPermissionGranted) "Granted" else "Grant")
            }
            Button(
                onClick = onTestPhotoCapture,
                enabled = isCameraPermissionGranted
            ) {
                Text("Test Photo")
            }
            Button(
                onClick = onTestVideoCapture,
                enabled = isCameraPermissionGranted && canTestVideo
            ) {
                Text("Test Video")
            }
        }
        Text(
            text = testPhotoStatus,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = testVideoStatus,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingsSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}

@Composable
private fun SettingsSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}
