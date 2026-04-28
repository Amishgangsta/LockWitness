package com.lockwitness.app.location

import com.lockwitness.app.admin.FailedUnlockIncidentCreator
import com.lockwitness.app.data.incident.SecurityIncidentRepository
import kotlinx.coroutines.flow.first

class LocationIncidentUpdater(
    private val incidentRepository: SecurityIncidentRepository,
    private val locationSnapshotClient: LocationSnapshotClient
) {
    suspend fun updateIncidentLocation(incidentId: Long): LocationUpdateResult {
        val incident = incidentRepository.getById(incidentId).first()
            ?: return LocationUpdateResult.Failed("Incident not found.")

        if (!incident.locationEnabled) {
            incidentRepository.updateLocationResult(
                id = incidentId,
                latitude = null,
                longitude = null,
                locationAccuracy = null,
                locationProvider = null,
                locationStatus = FailedUnlockIncidentCreator.STATUS_DISABLED,
                notes = incident.notes
            )
            return LocationUpdateResult.Disabled
        }

        return when (val result = locationSnapshotClient.captureLocationSnapshot()) {
            is LocationSnapshotResult.Success -> {
                incidentRepository.updateLocationResult(
                    id = incidentId,
                    latitude = result.latitude,
                    longitude = result.longitude,
                    locationAccuracy = result.accuracy,
                    locationProvider = result.provider,
                    locationStatus = STATUS_SUCCESS,
                    notes = incident.notes
                )
                LocationUpdateResult.Success
            }

            is LocationSnapshotResult.Unavailable -> {
                incidentRepository.updateLocationResult(
                    id = incidentId,
                    latitude = null,
                    longitude = null,
                    locationAccuracy = null,
                    locationProvider = null,
                    locationStatus = STATUS_UNAVAILABLE,
                    notes = appendLocationNote(incident.notes, "Location unavailable: ${result.reason}")
                )
                LocationUpdateResult.Unavailable(result.reason)
            }

            is LocationSnapshotResult.Failure -> {
                incidentRepository.updateLocationResult(
                    id = incidentId,
                    latitude = null,
                    longitude = null,
                    locationAccuracy = null,
                    locationProvider = null,
                    locationStatus = STATUS_FAILED,
                    notes = appendLocationNote(incident.notes, "Location snapshot failed: ${result.reason}")
                )
                LocationUpdateResult.Failed(result.reason)
            }
        }
    }

    private fun appendLocationNote(existingNotes: String, note: String): String =
        listOf(existingNotes, note)
            .filter { it.isNotBlank() }
            .joinToString(separator = "\n")

    companion object {
        const val STATUS_SUCCESS = "SUCCESS"
        const val STATUS_FAILED = "FAILED"
        const val STATUS_UNAVAILABLE = "UNAVAILABLE"
    }
}

sealed interface LocationUpdateResult {
    data object Success : LocationUpdateResult
    data object Disabled : LocationUpdateResult
    data class Unavailable(val reason: String) : LocationUpdateResult
    data class Failed(val reason: String) : LocationUpdateResult
}
