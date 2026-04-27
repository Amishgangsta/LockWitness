package com.lockwitness.app.video

import com.lockwitness.app.admin.FailedUnlockIncidentCreator
import com.lockwitness.app.data.SettingsState
import com.lockwitness.app.data.incident.SecurityIncidentRepository
import kotlinx.coroutines.flow.first

class VideoIncidentUpdater(
    private val incidentRepository: SecurityIncidentRepository,
    private val videoCaptureClient: VideoCaptureClient
) {
    suspend fun updateIncidentVideo(
        incidentId: Long,
        durationSeconds: Int
    ): VideoUpdateResult {
        val incident = incidentRepository.getById(incidentId).first()
            ?: return VideoUpdateResult.Failed("Incident not found.")

        if (!incident.videoEnabled) {
            incidentRepository.updateVideoResult(
                id = incidentId,
                videoPath = null,
                videoSha256 = null,
                videoStatus = FailedUnlockIncidentCreator.STATUS_DISABLED,
                notes = incident.notes
            )
            return VideoUpdateResult.Disabled
        }

        val safeDurationSeconds = sanitizeDuration(durationSeconds)
        return when (val result = videoCaptureClient.captureFrontVideo(safeDurationSeconds)) {
            is VideoCaptureResult.Success -> {
                incidentRepository.updateVideoResult(
                    id = incidentId,
                    videoPath = result.file.absolutePath,
                    videoSha256 = result.sha256,
                    videoStatus = STATUS_SUCCESS,
                    notes = incident.notes
                )
                VideoUpdateResult.Success
            }

            is VideoCaptureResult.Failure -> {
                incidentRepository.updateVideoResult(
                    id = incidentId,
                    videoPath = null,
                    videoSha256 = null,
                    videoStatus = STATUS_FAILED,
                    notes = appendVideoFailure(incident.notes, result.reason)
                )
                VideoUpdateResult.Failed(result.reason)
            }
        }
    }

    private fun appendVideoFailure(existingNotes: String, reason: String): String =
        listOf(existingNotes, "Video capture failed: $reason")
            .filter { it.isNotBlank() }
            .joinToString(separator = "\n")

    private fun sanitizeDuration(seconds: Int): Int =
        if (seconds in SettingsState.AllowedVideoDurations) seconds else SettingsState.Defaults.videoDurationSeconds

    companion object {
        const val STATUS_SUCCESS = "SUCCESS"
        const val STATUS_FAILED = "FAILED"
    }
}

sealed interface VideoUpdateResult {
    data object Success : VideoUpdateResult
    data object Disabled : VideoUpdateResult
    data class Failed(val reason: String) : VideoUpdateResult
}
