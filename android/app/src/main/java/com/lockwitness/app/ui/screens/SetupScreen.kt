package com.lockwitness.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.lockwitness.app.ui.components.ForensicCard
import com.lockwitness.app.ui.components.ForensicDivider
import com.lockwitness.app.ui.components.SectionEyebrow
import com.lockwitness.app.ui.components.StatusPill
import com.lockwitness.app.ui.theme.CautionAmber
import com.lockwitness.app.ui.theme.GraphiteBg
import com.lockwitness.app.ui.theme.StrokeSubtle
import com.lockwitness.app.ui.theme.TextPrimary
import com.lockwitness.app.ui.theme.TextSecondary
import com.lockwitness.app.ui.theme.VerifiedGreen

@Composable
fun SetupScreen(
    contentPadding: PaddingValues,
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToDiagnostics: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isDeviceAdminActive by remember { mutableStateOf(DeviceAdminStatus.isActive(context)) }
    var isCameraGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var isLocationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        isCameraGranted = granted
    }
    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        isLocationGranted = grants.values.any { it }
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isDeviceAdminActive = DeviceAdminStatus.isActive(context)
                isCameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                isLocationGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var showDeviceAdminExplanation by remember { mutableStateOf(false) }

    val steps = listOf(isDeviceAdminActive, isCameraGranted, isLocationGranted)
    val completedCount = steps.count { it }
    val progress = completedCount / steps.size.toFloat()
    val allDone = steps.all { it }

    if (showDeviceAdminExplanation) {
        AlertDialog(
            onDismissRequest = { showDeviceAdminExplanation = false },
            icon = { Icon(Icons.Outlined.Shield, contentDescription = null) },
            title = { Text("Why Lock Witness needs Device Admin") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Android requires Device Admin permission to notify Lock Witness when an incorrect PIN, password, or pattern is entered.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    val points = listOf(
                        "Lock Witness does not erase your phone.",
                        "Lock Witness does not lock your phone.",
                        "Lock Witness does not monitor calls, contacts, microphone, or messages.",
                        "You can disable Device Admin at any time in Android Settings."
                    )
                    points.forEach { point ->
                        Text("• $point", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeviceAdminExplanation = false
                        context.startActivity(DeviceAdminStatus.activationIntent(context))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen, contentColor = TextPrimary)
                ) {
                    Text("Continue to System Settings", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeviceAdminExplanation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GraphiteBg)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Setup",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        // Progress card
        ForensicCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionEyebrow("Setup Progress")
                    Text(
                        text = "$completedCount / ${steps.size}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (allDone) VerifiedGreen else CautionAmber
                    )
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = if (allDone) VerifiedGreen else CautionAmber,
                    trackColor = StrokeSubtle
                )
                Text(
                    text = if (allDone) "All permissions granted. Lock Witness is ready." else "Complete the steps below to enable evidence capture.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (allDone) VerifiedGreen else TextSecondary
                )
            }
        }

        // Step 1: Device Admin
        SetupStepCard(
            stepNumber = 1,
            icon = Icons.Outlined.Shield,
            title = "Device Admin",
            description = "Detects failed PIN, password, and pattern unlock attempts. Face and fingerprint failures are not captured — Android does not expose biometric failure events to apps.",
            isComplete = isDeviceAdminActive,
            actionLabel = "Activate Device Admin",
            onAction = { showDeviceAdminExplanation = true }
        )

        // Step 2: Camera
        SetupStepCard(
            stepNumber = 2,
            icon = Icons.Outlined.CameraAlt,
            title = "Camera Permission",
            description = "Grants access to the front camera to capture photos when a failed unlock occurs.",
            isComplete = isCameraGranted,
            actionLabel = "Grant Camera Access",
            onAction = { cameraLauncher.launch(Manifest.permission.CAMERA) }
        )

        // Step 3: Location
        SetupStepCard(
            stepNumber = 3,
            icon = Icons.Outlined.LocationOn,
            title = "Location Permission",
            description = "Optional. Captures GPS coordinates at the time of a failed unlock (Pro feature).",
            isComplete = isLocationGranted,
            actionLabel = "Grant Location Access",
            onAction = {
                locationLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                )
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (allDone) {
            Button(
                onClick = onNavigateToDashboard,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen, contentColor = TextPrimary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("  Setup Complete — Go to Dashboard", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }

        OutlinedButton(
            onClick = onNavigateToDiagnostics,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
            border = androidx.compose.foundation.BorderStroke(1.dp, StrokeSubtle)
        ) {
            Text("Run Diagnostics", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SetupStepCard(
    stepNumber: Int,
    icon: ImageVector,
    title: String,
    description: String,
    isComplete: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    ForensicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isComplete) Icons.Outlined.CheckCircle else icon,
                    contentDescription = null,
                    tint = if (isComplete) VerifiedGreen else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Step $stepNumber — $title",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                StatusPill(
                    text = if (isComplete) "Done" else "Pending",
                    color = if (isComplete) VerifiedGreen else CautionAmber
                )
            }
            ForensicDivider()
            Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            if (!isComplete) {
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen, contentColor = TextPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(actionLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
