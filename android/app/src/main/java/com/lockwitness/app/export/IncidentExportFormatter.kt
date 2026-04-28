package com.lockwitness.app.export

import com.lockwitness.app.data.incident.SecurityIncident
import java.io.File

class IncidentExportFormatter {
    fun metadataJson(incidents: List<SecurityIncident>, missingMedia: List<String>): String =
        buildString {
            append("{\n")
            append("  \"exportVersion\": 1,\n")
            append("  \"incidentCount\": ${incidents.size},\n")
            append("  \"missingMediaFiles\": [")
            append(missingMedia.joinToString(separator = ", ") { "\"${it.jsonEscape()}\"" })
            append("],\n")
            append("  \"incidents\": [\n")
            incidents.forEachIndexed { index, incident ->
                append(incidentJson(incident).prependIndent("    "))
                if (index != incidents.lastIndex) append(",")
                append("\n")
            }
            append("  ]\n")
            append("}\n")
        }

    fun incidentsCsv(incidents: List<SecurityIncident>): String =
        buildString {
            appendLine(CSV_COLUMNS.joinToString(","))
            incidents.forEach { incident ->
                appendLine(
                    listOf(
                        incident.id,
                        incident.timestamp,
                        incident.triggerType,
                        incident.failedAttemptCount,
                        incident.deviceModel,
                        incident.androidVersion,
                        incident.appVersion,
                        incident.photoPath,
                        incident.videoPath,
                        incident.latitude,
                        incident.longitude,
                        incident.locationAccuracy,
                        incident.locationProvider,
                        incident.imageSha256,
                        incident.videoSha256,
                        incident.photoStatus,
                        incident.videoStatus,
                        incident.locationStatus,
                        incident.emailStatus,
                        incident.shareStatus,
                        incident.notes
                    ).joinToString(",") { it.csvEscape() }
                )
            }
        }

    fun hashesText(incidents: List<SecurityIncident>, missingMedia: List<String>): String =
        buildString {
            appendLine("LockWitness evidence hashes")
            appendLine()
            incidents.forEach { incident ->
                appendLine("Incident ${incident.id}")
                appendLine("Image SHA-256: ${incident.imageSha256.orEmptyValue()}")
                appendLine("Video SHA-256: ${incident.videoSha256.orEmptyValue()}")
                appendLine()
            }
            if (missingMedia.isNotEmpty()) {
                appendLine("Missing media files")
                missingMedia.forEach { appendLine(it) }
            }
        }

    fun mediaReferences(incident: SecurityIncident): List<Pair<String, File>> =
        listOfNotNull(
            incident.photoPath?.takeIf { it.isNotBlank() }?.let { "photos/incident_${incident.id}_${File(it).name}" to File(it) },
            incident.videoPath?.takeIf { it.isNotBlank() }?.let { "videos/incident_${incident.id}_${File(it).name}" to File(it) }
        )

    private fun incidentJson(incident: SecurityIncident): String =
        buildString {
            append("{\n")
            appendJson("id", incident.id, comma = true)
            appendJson("timestamp", incident.timestamp, comma = true)
            appendJson("triggerType", incident.triggerType, comma = true)
            appendJson("failedAttemptCount", incident.failedAttemptCount, comma = true)
            appendJson("photoEnabled", incident.photoEnabled, comma = true)
            appendJson("videoEnabled", incident.videoEnabled, comma = true)
            appendJson("locationEnabled", incident.locationEnabled, comma = true)
            appendJson("emailEnabled", incident.emailEnabled, comma = true)
            appendJson("shareEnabled", incident.shareEnabled, comma = true)
            appendJson("timelineEnabled", incident.timelineEnabled, comma = true)
            appendJson("deviceModel", incident.deviceModel, comma = true)
            appendJson("androidVersion", incident.androidVersion, comma = true)
            appendJson("appVersion", incident.appVersion, comma = true)
            appendJson("photoPath", incident.photoPath, comma = true)
            appendJson("videoPath", incident.videoPath, comma = true)
            appendJson("latitude", incident.latitude, comma = true)
            appendJson("longitude", incident.longitude, comma = true)
            appendJson("locationAccuracy", incident.locationAccuracy, comma = true)
            appendJson("locationProvider", incident.locationProvider, comma = true)
            appendJson("imageSha256", incident.imageSha256, comma = true)
            appendJson("videoSha256", incident.videoSha256, comma = true)
            appendJson("photoStatus", incident.photoStatus, comma = true)
            appendJson("videoStatus", incident.videoStatus, comma = true)
            appendJson("locationStatus", incident.locationStatus, comma = true)
            appendJson("emailStatus", incident.emailStatus, comma = true)
            appendJson("shareStatus", incident.shareStatus, comma = true)
            appendJson("notes", incident.notes, comma = false)
            append("  }")
        }

    private fun StringBuilder.appendJson(name: String, value: Any?, comma: Boolean) {
        append("  \"")
        append(name)
        append("\": ")
        when (value) {
            null -> append("null")
            is Number, is Boolean -> append(value)
            else -> {
                append("\"")
                append(value.toString().jsonEscape())
                append("\"")
            }
        }
        if (comma) append(",")
        append("\n")
    }

    private fun Any?.csvEscape(): String {
        val value = this?.toString().orEmpty()
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    private fun String.jsonEscape(): String =
        buildString {
            this@jsonEscape.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(char)
                }
            }
        }

    private fun String?.orEmptyValue(): String =
        this?.takeIf { it.isNotBlank() } ?: "not recorded"

    private companion object {
        val CSV_COLUMNS = listOf(
            "id",
            "timestamp",
            "triggerType",
            "failedAttemptCount",
            "deviceModel",
            "androidVersion",
            "appVersion",
            "photoPath",
            "videoPath",
            "latitude",
            "longitude",
            "locationAccuracy",
            "locationProvider",
            "imageSha256",
            "videoSha256",
            "photoStatus",
            "videoStatus",
            "locationStatus",
            "emailStatus",
            "shareStatus",
            "notes"
        )
    }
}
