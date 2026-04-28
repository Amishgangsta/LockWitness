package com.lockwitness.app.export

import androidx.test.core.app.ApplicationProvider
import com.lockwitness.app.admin.FailedUnlockIncidentCreator
import com.lockwitness.app.data.incident.SecurityIncident
import com.lockwitness.app.location.LocationIncidentUpdater
import com.lockwitness.app.photo.PhotoIncidentUpdater
import com.lockwitness.app.video.VideoIncidentUpdater
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.zip.ZipFile

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class LocalIncidentExporterTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun exportIncidentsWritesMetadataCsvHashesAndAvailableMedia() = runTest {
        val photo = temporaryFolder.newFile("photo.jpg").apply { writeText("photo") }
        val video = temporaryFolder.newFile("video.mp4").apply { writeText("video") }
        val exporter = LocalIncidentExporter(ApplicationProvider.getApplicationContext())

        val result = exporter.exportIncidents(
            incidents = listOf(testIncident(photoPath = photo.absolutePath, videoPath = video.absolutePath)),
            filePrefix = "test_export"
        )

        assertEquals(1, result.incidentCount)
        assertEquals(2, result.mediaFilesIncluded)
        assertTrue(result.missingMediaFiles.isEmpty())
        ZipFile(result.file).use { zip ->
            assertTrue(zip.getEntry("metadata.json") != null)
            assertTrue(zip.getEntry("incidents.csv") != null)
            assertTrue(zip.getEntry("hashes.txt") != null)
            assertTrue(zip.entries().asSequence().any { it.name.startsWith("photos/incident_7_") })
            assertTrue(zip.entries().asSequence().any { it.name.startsWith("videos/incident_7_") })
        }
    }

    @Test
    fun exportIncidentsRecordsMissingMediaWithoutFailing() = runTest {
        val exporter = LocalIncidentExporter(ApplicationProvider.getApplicationContext())
        val missingPhoto = temporaryFolder.root.resolve("missing.jpg")

        val result = exporter.exportIncidents(
            incidents = listOf(testIncident(photoPath = missingPhoto.absolutePath, videoPath = null)),
            filePrefix = "missing_media"
        )

        assertEquals(0, result.mediaFilesIncluded)
        assertEquals(listOf(missingPhoto.absolutePath), result.missingMediaFiles)
        ZipFile(result.file).use { zip ->
            val hashes = zip.getInputStream(zip.getEntry("hashes.txt")).bufferedReader().readText()
            assertTrue(hashes.contains(missingPhoto.absolutePath))
        }
    }

    private fun testIncident(
        photoPath: String?,
        videoPath: String?
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
            notes = "notes"
        )
}
