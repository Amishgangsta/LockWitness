package com.lockwitness.app.video

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lockwitness.app.admin.FailedUnlockIncidentCreator
import com.lockwitness.app.data.SettingsState
import com.lockwitness.app.data.incident.LockWitnessDatabase
import com.lockwitness.app.data.incident.SecurityIncident
import com.lockwitness.app.data.incident.SecurityIncidentRepository
import com.lockwitness.app.photo.PhotoIncidentUpdater
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class VideoIncidentUpdaterTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val database = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        LockWitnessDatabase::class.java
    ).allowMainThreadQueries().build()
    private val repository = SecurityIncidentRepository(database.securityIncidentDao())

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun updateIncidentVideoStoresSuccessPathHashAndStatus() = runTest {
        val video = temporaryFolder.newFile("video.mp4").apply {
            writeText("video")
        }
        val id = repository.insert(testIncident(videoEnabled = true))

        VideoIncidentUpdater(
            incidentRepository = repository,
            videoCaptureClient = fakeSuccessClient(video)
        ).updateIncidentVideo(id, durationSeconds = 10)

        val incident = repository.getById(id).first()
        assertEquals(video.absolutePath, incident?.videoPath)
        assertEquals("video-hash", incident?.videoSha256)
        assertEquals(VideoIncidentUpdater.STATUS_SUCCESS, incident?.videoStatus)
    }

    @Test
    fun updateIncidentVideoMarksFailureAndPreservesIncident() = runTest {
        val id = repository.insert(testIncident(videoEnabled = true))

        VideoIncidentUpdater(
            incidentRepository = repository,
            videoCaptureClient = fakeFailureClient("camera unavailable")
        ).updateIncidentVideo(id, durationSeconds = 5)

        val incident = repository.getById(id).first()
        assertEquals(id, incident?.id)
        assertNull(incident?.videoPath)
        assertNull(incident?.videoSha256)
        assertEquals(VideoIncidentUpdater.STATUS_FAILED, incident?.videoStatus)
        assertEquals(true, incident?.notes?.contains("camera unavailable"))
    }

    @Test
    fun updateIncidentVideoDoesNotCaptureWhenVideoDisabled() = runTest {
        val id = repository.insert(testIncident(videoEnabled = false))

        VideoIncidentUpdater(
            incidentRepository = repository,
            videoCaptureClient = object : VideoCaptureClient {
                override suspend fun captureFrontVideo(durationSeconds: Int): VideoCaptureResult {
                    error("Capture should not be called when video is disabled.")
                }
            }
        ).updateIncidentVideo(id, durationSeconds = 5)

        val incident = repository.getById(id).first()
        assertEquals(FailedUnlockIncidentCreator.STATUS_DISABLED, incident?.videoStatus)
        assertNull(incident?.videoPath)
        assertNull(incident?.videoSha256)
    }

    @Test
    fun updateIncidentVideoPassesAllowedDurationAndSanitizesInvalidDuration() = runTest {
        val allowedClient = RecordingVideoCaptureClient(temporaryFolder.newFile("allowed.mp4"))
        val allowedId = repository.insert(testIncident(videoEnabled = true))

        VideoIncidentUpdater(repository, allowedClient).updateIncidentVideo(allowedId, durationSeconds = 30)

        assertEquals(30, allowedClient.requestedDurationSeconds)

        val fallbackClient = RecordingVideoCaptureClient(temporaryFolder.newFile("fallback.mp4"))
        val fallbackId = repository.insert(testIncident(videoEnabled = true))

        VideoIncidentUpdater(repository, fallbackClient).updateIncidentVideo(fallbackId, durationSeconds = 99)

        assertEquals(SettingsState.Defaults.videoDurationSeconds, fallbackClient.requestedDurationSeconds)
    }

    @Test
    fun updateIncidentVideoFailurePreservesPhotoSuccess() = runTest {
        val id = repository.insert(
            testIncident(
                videoEnabled = true,
                photoPath = "existing-photo.jpg",
                imageSha256 = "existing-photo-hash",
                photoStatus = PhotoIncidentUpdater.STATUS_SUCCESS
            )
        )

        VideoIncidentUpdater(
            incidentRepository = repository,
            videoCaptureClient = fakeFailureClient("video failed")
        ).updateIncidentVideo(id, durationSeconds = 5)

        val incident = repository.getById(id).first()
        assertEquals("existing-photo.jpg", incident?.photoPath)
        assertEquals("existing-photo-hash", incident?.imageSha256)
        assertEquals(PhotoIncidentUpdater.STATUS_SUCCESS, incident?.photoStatus)
        assertEquals(VideoIncidentUpdater.STATUS_FAILED, incident?.videoStatus)
    }

    private fun fakeSuccessClient(file: File): VideoCaptureClient =
        object : VideoCaptureClient {
            override suspend fun captureFrontVideo(durationSeconds: Int): VideoCaptureResult =
                VideoCaptureResult.Success(file = file, sha256 = "video-hash")
        }

    private fun fakeFailureClient(reason: String): VideoCaptureClient =
        object : VideoCaptureClient {
            override suspend fun captureFrontVideo(durationSeconds: Int): VideoCaptureResult =
                VideoCaptureResult.Failure(reason)
        }

    private class RecordingVideoCaptureClient(
        private val file: File
    ) : VideoCaptureClient {
        var requestedDurationSeconds: Int? = null

        override suspend fun captureFrontVideo(durationSeconds: Int): VideoCaptureResult {
            requestedDurationSeconds = durationSeconds
            return VideoCaptureResult.Success(file = file, sha256 = "video-hash")
        }
    }

    private fun testIncident(
        videoEnabled: Boolean,
        photoPath: String? = null,
        imageSha256: String? = null,
        photoStatus: String = FailedUnlockIncidentCreator.STATUS_NOT_ATTEMPTED
    ): SecurityIncident =
        SecurityIncident(
            timestamp = 1000L,
            triggerType = FailedUnlockIncidentCreator.TRIGGER_FAILED_UNLOCK,
            failedAttemptCount = 1,
            photoEnabled = true,
            videoEnabled = videoEnabled,
            locationEnabled = false,
            emailEnabled = false,
            shareEnabled = false,
            timelineEnabled = true,
            photoPath = photoPath,
            videoPath = null,
            latitude = null,
            longitude = null,
            locationAccuracy = null,
            locationProvider = null,
            imageSha256 = imageSha256,
            videoSha256 = null,
            deviceModel = "model",
            androidVersion = "android",
            appVersion = "app",
            photoStatus = photoStatus,
            videoStatus = if (videoEnabled) {
                FailedUnlockIncidentCreator.STATUS_NOT_ATTEMPTED
            } else {
                FailedUnlockIncidentCreator.STATUS_DISABLED
            },
            locationStatus = FailedUnlockIncidentCreator.STATUS_DISABLED,
            emailStatus = FailedUnlockIncidentCreator.STATUS_DISABLED,
            shareStatus = FailedUnlockIncidentCreator.STATUS_DISABLED,
            notes = "shell"
        )
}
