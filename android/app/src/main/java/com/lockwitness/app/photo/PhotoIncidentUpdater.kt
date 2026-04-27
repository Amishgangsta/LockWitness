package com.lockwitness.app.photo

import com.lockwitness.app.admin.FailedUnlockIncidentCreator
import com.lockwitness.app.data.incident.SecurityIncidentRepository
import kotlinx.coroutines.flow.first

class PhotoIncidentUpdater(
    private val incidentRepository: SecurityIncidentRepository,
    private val photoCaptureClient: PhotoCaptureClient
) {
    suspend fun updateIncidentPhoto(incidentId: Long): PhotoUpdateResult {
        val incident = incidentRepository.getById(incidentId).first()
            ?: return PhotoUpdateResult.Failed("Incident not found.")

        if (!incident.photoEnabled) {
            incidentRepository.updatePhotoResult(
                id = incidentId,
                photoPath = null,
                imageSha256 = null,
                photoStatus = FailedUnlockIncidentCreator.STATUS_DISABLED,
                notes = incident.notes
            )
            return PhotoUpdateResult.Disabled
        }

        return when (val result = photoCaptureClient.captureFrontPhoto()) {
            is PhotoCaptureResult.Success -> {
                incidentRepository.updatePhotoResult(
                    id = incidentId,
                    photoPath = result.file.absolutePath,
                    imageSha256 = result.sha256,
                    photoStatus = STATUS_SUCCESS,
                    notes = incident.notes
                )
                PhotoUpdateResult.Success
            }

            is PhotoCaptureResult.Failure -> {
                incidentRepository.updatePhotoResult(
                    id = incidentId,
                    photoPath = null,
                    imageSha256 = null,
                    photoStatus = STATUS_FAILED,
                    notes = appendPhotoFailure(incident.notes, result.reason)
                )
                PhotoUpdateResult.Failed(result.reason)
            }
        }
    }

    private fun appendPhotoFailure(existingNotes: String, reason: String): String =
        listOf(existingNotes, "Photo capture failed: $reason")
            .filter { it.isNotBlank() }
            .joinToString(separator = "\n")

    companion object {
        const val STATUS_SUCCESS = "SUCCESS"
        const val STATUS_FAILED = "FAILED"
    }
}

sealed interface PhotoUpdateResult {
    data object Success : PhotoUpdateResult
    data object Disabled : PhotoUpdateResult
    data class Failed(val reason: String) : PhotoUpdateResult
}
