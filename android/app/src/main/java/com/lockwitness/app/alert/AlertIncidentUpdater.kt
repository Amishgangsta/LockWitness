package com.lockwitness.app.alert

import com.lockwitness.app.admin.FailedUnlockIncidentCreator
import com.lockwitness.app.data.incident.SecurityIncidentRepository
import kotlinx.coroutines.flow.first

class AlertIncidentUpdater(
    private val incidentRepository: SecurityIncidentRepository
) {
    suspend fun markUserActionRequired(incidentId: Long): AlertUpdateResult {
        val incident = incidentRepository.getById(incidentId).first()
            ?: return AlertUpdateResult.Failed("Incident not found.")

        val emailStatus = if (incident.emailEnabled) {
            STATUS_UNAVAILABLE
        } else {
            FailedUnlockIncidentCreator.STATUS_DISABLED
        }
        val shareStatus = if (incident.shareEnabled) {
            STATUS_UNAVAILABLE
        } else {
            FailedUnlockIncidentCreator.STATUS_DISABLED
        }

        incidentRepository.updateAlertResult(
            id = incidentId,
            emailStatus = emailStatus,
            shareStatus = shareStatus,
            notes = appendAlertNote(
                existingNotes = incident.notes,
                emailEnabled = incident.emailEnabled,
                shareEnabled = incident.shareEnabled
            )
        )

        return AlertUpdateResult.Updated(emailStatus = emailStatus, shareStatus = shareStatus)
    }

    suspend fun markManualShareLaunched(incidentId: Long): AlertUpdateResult {
        val incident = incidentRepository.getById(incidentId).first()
            ?: return AlertUpdateResult.Failed("Incident not found.")

        val emailStatus = if (incident.emailEnabled) STATUS_SUCCESS else FailedUnlockIncidentCreator.STATUS_DISABLED
        val shareStatus = if (incident.shareEnabled) STATUS_SUCCESS else FailedUnlockIncidentCreator.STATUS_DISABLED

        incidentRepository.updateAlertResult(
            id = incidentId,
            emailStatus = emailStatus,
            shareStatus = shareStatus,
            notes = appendManualShareNote(incident.notes)
        )
        return AlertUpdateResult.Updated(emailStatus = emailStatus, shareStatus = shareStatus)
    }

    suspend fun markManualShareFailed(incidentId: Long, reason: String): AlertUpdateResult {
        val incident = incidentRepository.getById(incidentId).first()
            ?: return AlertUpdateResult.Failed("Incident not found.")

        val emailStatus = if (incident.emailEnabled) STATUS_FAILED else FailedUnlockIncidentCreator.STATUS_DISABLED
        val shareStatus = if (incident.shareEnabled) STATUS_FAILED else FailedUnlockIncidentCreator.STATUS_DISABLED

        incidentRepository.updateAlertResult(
            id = incidentId,
            emailStatus = emailStatus,
            shareStatus = shareStatus,
            notes = appendFailureNote(incident.notes, reason)
        )
        return AlertUpdateResult.Updated(emailStatus = emailStatus, shareStatus = shareStatus)
    }

    private fun appendAlertNote(
        existingNotes: String,
        emailEnabled: Boolean,
        shareEnabled: Boolean
    ): String {
        val notes = mutableListOf(existingNotes).filterTo(mutableListOf()) { it.isNotBlank() }
        if (emailEnabled) {
            notes += "Email alert requires user-initiated chooser action; no automatic email was sent."
        }
        if (shareEnabled) {
            notes += "Share alert requires user-initiated chooser action; no automatic share was sent."
        }
        return notes.joinToString(separator = "\n")
    }

    private fun appendManualShareNote(existingNotes: String): String =
        listOf(existingNotes, "Manual user-controlled share/email chooser launched.")
            .filter { it.isNotBlank() }
            .joinToString(separator = "\n")

    private fun appendFailureNote(existingNotes: String, reason: String): String =
        listOf(existingNotes, "Manual share/email chooser failed: $reason")
            .filter { it.isNotBlank() }
            .joinToString(separator = "\n")

    companion object {
        const val STATUS_SUCCESS = "SUCCESS"
        const val STATUS_FAILED = "FAILED"
        const val STATUS_UNAVAILABLE = "UNAVAILABLE"
    }
}

sealed interface AlertUpdateResult {
    data class Updated(
        val emailStatus: String,
        val shareStatus: String
    ) : AlertUpdateResult

    data class Failed(val reason: String) : AlertUpdateResult
}
