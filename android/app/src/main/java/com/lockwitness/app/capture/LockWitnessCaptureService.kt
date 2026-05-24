package com.lockwitness.app.capture

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.lockwitness.app.R
import com.lockwitness.app.admin.AndroidDeviceInfoProvider
import com.lockwitness.app.admin.FailedUnlockIncidentCreator
import com.lockwitness.app.alert.AlertIncidentUpdater
import com.lockwitness.app.data.SettingsRepository
import com.lockwitness.app.data.incident.LockWitnessDatabase
import com.lockwitness.app.data.incident.SecurityIncidentRepository
import com.lockwitness.app.location.AndroidLocationSnapshotClient
import com.lockwitness.app.location.LocationIncidentUpdater
import com.lockwitness.app.monetization.MonetizationRepository
import com.lockwitness.app.photo.Camera2PhotoCaptureClient
import com.lockwitness.app.photo.PhotoIncidentUpdater
import com.lockwitness.app.video.Camera2VideoCaptureClient
import com.lockwitness.app.video.VideoIncidentUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LockWitnessCaptureService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())

        val failedAttemptCount = intent?.getIntExtra(EXTRA_FAILED_ATTEMPT_COUNT, 0) ?: 0
        val appContext = applicationContext

        scope.launch {
            try {
                val settingsRepository = SettingsRepository.create(appContext)
                val monetizationRepository = MonetizationRepository.create(appContext)
                val incidentRepository = SecurityIncidentRepository(
                    LockWitnessDatabase.getInstance(appContext).securityIncidentDao()
                )
                val incidentId = FailedUnlockIncidentCreator(
                    settingsRepository = settingsRepository,
                    incidentRepository = incidentRepository,
                    deviceInfoProvider = AndroidDeviceInfoProvider(appContext),
                    monetizationStateProvider = { monetizationRepository.state.first() }
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

                    AlertIncidentUpdater(
                        incidentRepository = incidentRepository
                    ).markUserActionRequired(incidentId)
                }
            } finally {
                stopSelf(startId)
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun buildNotification() = run {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Failed Unlock Recording",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    setShowBadge(false)
                }
            )
        }
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Lock Witness")
            .setContentText("Recording failed unlock event")
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    companion object {
        const val EXTRA_FAILED_ATTEMPT_COUNT = "failed_attempt_count"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "lockwitness_capture"
    }
}
