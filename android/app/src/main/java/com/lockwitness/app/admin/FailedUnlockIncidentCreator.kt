package com.lockwitness.app.admin

import com.lockwitness.app.data.SettingsRepository
import com.lockwitness.app.data.incident.SecurityIncident
import com.lockwitness.app.data.incident.SecurityIncidentRepository
import com.lockwitness.app.monetization.MonetizationState
import com.lockwitness.app.monetization.ProFeature
import com.lockwitness.app.monetization.ProFeatureGate
import kotlinx.coroutines.flow.first

class FailedUnlockIncidentCreator(
    private val settingsRepository: SettingsRepository,
    private val incidentRepository: SecurityIncidentRepository,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val monetizationStateProvider: suspend () -> MonetizationState = { MonetizationState.Pro },
    private val proFeatureGate: ProFeatureGate = ProFeatureGate(),
    private val clock: () -> Long = System::currentTimeMillis
) {
    suspend fun createIncidentShell(failedAttemptCount: Int): Long? {
        val settings = settingsRepository.settings.first()
        if (!settings.masterMonitoringEnabled) {
            return null
        }
        val monetizationState = monetizationStateProvider()
        val gatedVideoEnabled = settings.videoCaptureEnabled &&
            proFeatureGate.isAllowed(ProFeature.VideoCapture, monetizationState)
        val gatedLocationEnabled = settings.locationCaptureEnabled &&
            proFeatureGate.isAllowed(ProFeature.LocationSnapshot, monetizationState)

        val incident = SecurityIncident(
            timestamp = clock(),
            triggerType = TRIGGER_FAILED_UNLOCK,
            failedAttemptCount = failedAttemptCount.coerceAtLeast(0),
            photoEnabled = settings.photoCaptureEnabled,
            videoEnabled = gatedVideoEnabled,
            locationEnabled = gatedLocationEnabled,
            emailEnabled = settings.emailAlertEnabled,
            shareEnabled = settings.shareAlertEnabled,
            timelineEnabled = settings.localTimelineEnabled,
            photoPath = null,
            videoPath = null,
            latitude = null,
            longitude = null,
            locationAccuracy = null,
            locationProvider = null,
            imageSha256 = null,
            videoSha256 = null,
            deviceModel = deviceInfoProvider.deviceModel,
            androidVersion = deviceInfoProvider.androidVersion,
            appVersion = deviceInfoProvider.appVersion,
            photoStatus = if (settings.photoCaptureEnabled) STATUS_NOT_ATTEMPTED else STATUS_DISABLED,
            videoStatus = if (gatedVideoEnabled) STATUS_NOT_ATTEMPTED else STATUS_DISABLED,
            locationStatus = if (gatedLocationEnabled) STATUS_NOT_ATTEMPTED else STATUS_DISABLED,
            emailStatus = if (settings.emailAlertEnabled) STATUS_NOT_ATTEMPTED else STATUS_DISABLED,
            shareStatus = if (settings.shareAlertEnabled) STATUS_NOT_ATTEMPTED else STATUS_DISABLED,
            notes = "Incident shell created from failed unlock event."
        )

        return incidentRepository.insert(incident)
    }

    companion object {
        const val TRIGGER_FAILED_UNLOCK = "FAILED_UNLOCK"
        const val STATUS_NOT_ATTEMPTED = "NOT_ATTEMPTED"
        const val STATUS_DISABLED = "DISABLED"
    }
}
