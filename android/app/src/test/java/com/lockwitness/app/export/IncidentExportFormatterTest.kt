package com.lockwitness.app.export

import com.lockwitness.app.admin.FailedUnlockIncidentCreator
import com.lockwitness.app.data.incident.SecurityIncident
import com.lockwitness.app.location.LocationIncidentUpdater
import com.lockwitness.app.photo.PhotoIncidentUpdater
import com.lockwitness.app.video.VideoIncidentUpdater
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class IncidentExportFormatterTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val formatter = IncidentExportFormatter()

    @Test
    fun metadataJsonIncludesRequiredIncidentFieldsAndMissingMedia() {
        val incident = testIncident(notes = "line one\nline two")

        val metadata = formatter.metadataJson(listOf(incident), listOf("missing.jpg"))

        assertTrue(metadata.contains("\"incidentCount\": 1"))
        assertTrue(metadata.contains("\"triggerType\": \"FAILED_UNLOCK\""))
        assertTrue(metadata.contains("\"failedAttemptCount\": 3"))
        assertTrue(metadata.contains("\"deviceModel\": \"model\""))
        assertTrue(metadata.contains("\"photoStatus\": \"SUCCESS\""))
        assertTrue(metadata.contains("\"videoStatus\": \"SUCCESS\""))
        assertTrue(metadata.contains("\"locationStatus\": \"SUCCESS\""))
        assertTrue(metadata.contains("\"missingMediaFiles\": [\"missing.jpg\"]"))
        assertTrue(metadata.contains("line one\\nline two"))
    }

    @Test
    fun incidentsCsvIncludesHeaderAndEscapesValues() {
        val csv = formatter.incidentsCsv(
            listOf(testIncident(notes = "note, with comma and \"quote\""))
        )

        assertTrue(csv.lines().first().contains("id,timestamp,triggerType"))
        assertTrue(csv.contains("\"FAILED_UNLOCK\""))
        assertTrue(csv.contains("\"note, with comma and \"\"quote\"\"\""))
    }

    @Test
    fun hashesTextIncludesImageVideoHashesAndMissingMedia() {
        val hashes = formatter.hashesText(listOf(testIncident()), listOf("missing.mp4"))

        assertTrue(hashes.contains("Image SHA-256: image-hash"))
        assertTrue(hashes.contains("Video SHA-256: video-hash"))
        assertTrue(hashes.contains("Missing media files"))
        assertTrue(hashes.contains("missing.mp4"))
    }

    @Test
    fun mediaReferencesIncludePhotoAndVideoEntries() {
        val photo = temporaryFolder.newFile("photo.jpg")
        val video = temporaryFolder.newFile("video.mp4")

        val references = formatter.mediaReferences(
            testIncident(photoPath = photo.absolutePath, videoPath = video.absolutePath)
        )

        assertEquals(2, references.size)
        assertTrue(references.any { it.first.startsWith("photos/incident_7_") && it.second == photo })
        assertTrue(references.any { it.first.startsWith("videos/incident_7_") && it.second == video })
    }

    private fun testIncident(
        photoPath: String = "photo.jpg",
        videoPath: String = "video.mp4",
        notes: String = "notes"
    ): SecurityIncident =
        SecurityIncident(
            id = 7L,
            timestamp = 1000L,
            triggerType = FailedUnlockIncidentCreator.TRIGGER_FAILED_UNLOCK,
            failedAttemptCount = 3,
            photoEnabled = true,
            videoEnabled = true,
            locationEnabled = true,
            emailEnabled = false,
            shareEnabled = false,
            timelineEnabled = true,
            photoPath = photoPath,
            videoPath = videoPath,
            latitude = 35.0,
            longitude = -80.0,
            locationAccuracy = 6.5f,
            locationProvider = "gps",
            imageSha256 = "image-hash",
            videoSha256 = "video-hash",
            deviceModel = "model",
            androidVersion = "android",
            appVersion = "app",
            photoStatus = PhotoIncidentUpdater.STATUS_SUCCESS,
            videoStatus = VideoIncidentUpdater.STATUS_SUCCESS,
            locationStatus = LocationIncidentUpdater.STATUS_SUCCESS,
            emailStatus = FailedUnlockIncidentCreator.STATUS_DISABLED,
            shareStatus = FailedUnlockIncidentCreator.STATUS_DISABLED,
            notes = notes
        )
}
