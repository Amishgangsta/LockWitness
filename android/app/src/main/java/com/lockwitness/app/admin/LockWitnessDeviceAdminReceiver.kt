package com.lockwitness.app.admin

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.lockwitness.app.data.SettingsRepository
import com.lockwitness.app.data.incident.LockWitnessDatabase
import com.lockwitness.app.data.incident.SecurityIncidentRepository
import com.lockwitness.app.location.AndroidLocationSnapshotClient
import com.lockwitness.app.location.LocationIncidentUpdater
import com.lockwitness.app.photo.Camera2PhotoCaptureClient
import com.lockwitness.app.photo.PhotoIncidentUpdater
import com.lockwitness.app.video.Camera2VideoCaptureClient
import com.lockwitness.app.video.VideoIncidentUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LockWitnessDeviceAdminReceiver : DeviceAdminReceiver() {
    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onPasswordFailed(context: Context, intent: Intent) {
        super.onPasswordFailed(context, intent)

        val appContext = context.applicationContext
        val failedAttemptCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching {
                appContext
                    .getSystemService(DevicePolicyManager::class.java)
                    .currentFailedPasswordAttempts
            }.getOrDefault(0)
        } else {
            0
        }

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val settingsRepository = SettingsRepository.create(appContext)
            val incidentRepository = SecurityIncidentRepository(
                LockWitnessDatabase.getInstance(appContext).securityIncidentDao()
            )
            val incidentId = FailedUnlockIncidentCreator(
                settingsRepository = settingsRepository,
                incidentRepository = incidentRepository,
                deviceInfoProvider = AndroidDeviceInfoProvider(appContext)
            ).createIncidentShell(failedAttemptCount)

            if (incidentId != null) {
                val settings = settingsRepository.settings.first()
                PhotoIncidentUpdater(
                    incidentRepository = incidentRepository,
                    photoCaptureClient = Camera2PhotoCaptureClient(appContext)
                ).updateIncidentPhoto(incidentId)

                VideoIncidentUpdater(
                    incidentRepository = incidentRepository,
                    videoCaptureClient = Camera2VideoCaptureClient(appContext)
                ).updateIncidentVideo(
                    incidentId = incidentId,
                    durationSeconds = settings.videoDurationSeconds
                )

                LocationIncidentUpdater(
                    incidentRepository = incidentRepository,
                    locationSnapshotClient = AndroidLocationSnapshotClient(appContext)
                ).updateIncidentLocation(incidentId)
            }
        }
    }
}
