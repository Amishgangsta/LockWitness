package com.lockwitness.app.ui.history

import com.lockwitness.app.data.incident.SecurityIncident
import com.lockwitness.app.data.incident.SecurityIncidentRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class IncidentSummaryUi(
    val id: Long,
    val timestamp: String,
    val triggerType: String,
    val failedAttemptCount: String,
    val photoStatus: String,
    val videoStatus: String,
    val locationStatus: String,
    val emailStatus: String,
    val shareStatus: String,
    val hasPhoto: Boolean,
    val hasVideo: Boolean,
    val hasLocation: Boolean,
    val photoPath: String?,
    val videoPath: String?,
    val latitude: Double?,
    val longitude: Double?
)

data class IncidentDetailUi(
    val id: Long,
    val timestamp: String,
    val triggerType: String,
    val failedAttemptCount: String,
    val settingsSnapshot: List<Pair<String, String>>,
    val deviceMetadata: List<Pair<String, String>>,
    val mediaFields: List<Pair<String, String>>,
    val locationFields: List<Pair<String, String>>,
    val hashFields: List<Pair<String, String>>,
    val statusFields: List<Pair<String, String>>,
    val notes: String
)

class IncidentHistoryMapper(
    private val clockFormatter: (Long) -> String = ::defaultTimestampFormatter
) {
    fun toSummary(incident: SecurityIncident): IncidentSummaryUi =
        IncidentSummaryUi(
            id = incident.id,
            timestamp = clockFormatter(incident.timestamp),
            triggerType = incident.triggerType,
            failedAttemptCount = incident.failedAttemptCount.toString(),
            photoStatus = incident.photoStatus,
            videoStatus = incident.videoStatus,
            locationStatus = incident.locationStatus,
            emailStatus = incident.emailStatus,
            shareStatus = incident.shareStatus,
            hasPhoto = !incident.photoPath.isNullOrBlank(),
            hasVideo = !incident.videoPath.isNullOrBlank(),
            hasLocation = incident.latitude != null && incident.longitude != null,
            photoPath = incident.photoPath,
            videoPath = incident.videoPath,
            latitude = incident.latitude,
            longitude = incident.longitude
        )

    fun toDetail(incident: SecurityIncident): IncidentDetailUi =
        IncidentDetailUi(
            id = incident.id,
            timestamp = clockFormatter(incident.timestamp),
            triggerType = incident.triggerType,
            failedAttemptCount = incident.failedAttemptCount.toString(),
            settingsSnapshot = listOf(
                "Photo enabled" to incident.photoEnabled.yesNo(),
                "Video enabled" to incident.videoEnabled.yesNo(),
                "Location enabled" to incident.locationEnabled.yesNo(),
                "Email enabled" to incident.emailEnabled.yesNo(),
                "Share enabled" to incident.shareEnabled.yesNo(),
                "Timeline enabled" to incident.timelineEnabled.yesNo()
            ),
            deviceMetadata = listOf(
                "Device model" to incident.deviceModel,
                "Android version" to incident.androidVersion,
                "App version" to incident.appVersion
            ),
            mediaFields = listOfNotNull(
                incident.photoPath?.takeIf { it.isNotBlank() }?.let { "Photo path" to it },
                incident.videoPath?.takeIf { it.isNotBlank() }?.let { "Video path" to it }
            ),
            locationFields = listOfNotNull(
                incident.latitude?.let { "Latitude" to it.toString() },
                incident.longitude?.let { "Longitude" to it.toString() },
                incident.locationAccuracy?.let { "Accuracy" to "$it m" },
                incident.locationProvider?.takeIf { it.isNotBlank() }?.let { "Provider" to it }
            ),
            hashFields = listOfNotNull(
                incident.imageSha256?.takeIf { it.isNotBlank() }?.let { "Image SHA-256" to it },
                incident.videoSha256?.takeIf { it.isNotBlank() }?.let { "Video SHA-256" to it }
            ),
            statusFields = listOf(
                "Photo status" to incident.photoStatus,
                "Video status" to incident.videoStatus,
                "Location status" to incident.locationStatus,
                "Email status" to incident.emailStatus,
                "Share status" to incident.shareStatus
            ),
            notes = incident.notes.ifBlank { "No notes recorded." }
        )
}

class IncidentHistoryActions(
    private val repository: SecurityIncidentRepository
) {
    suspend fun deleteIncident(id: Long): Int =
        repository.deleteById(id)

    suspend fun clearIncidents() =
        repository.clearAll()
}

private fun Boolean.yesNo(): String =
    if (this) "Yes" else "No"

private fun defaultTimestampFormatter(timestamp: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(timestamp))
