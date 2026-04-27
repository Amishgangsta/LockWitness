package com.lockwitness.app.photo

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lockwitness.app.admin.FailedUnlockIncidentCreator
import com.lockwitness.app.data.incident.LockWitnessDatabase
import com.lockwitness.app.data.incident.SecurityIncident
import com.lockwitness.app.data.incident.SecurityIncidentRepository
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

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PhotoIncidentUpdaterTest {
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
    fun updateIncidentPhotoStoresSuccessPathHashAndStatus() = runTest {
        val photo = temporaryFolder.newFile("photo.jpg").apply {
            writeText("photo")
        }
        val id = repository.insert(testIncident(photoEnabled = true))

        PhotoIncidentUpdater(
            incidentRepository = repository,
            photoCaptureClient = fakeSuccessClient(photo)
        ).updateIncidentPhoto(id)

        val incident = repository.getById(id).first()
        assertEquals(photo.absolutePath, incident?.photoPath)
        assertEquals("hash", incident?.imageSha256)
        assertEquals(PhotoIncidentUpdater.STATUS_SUCCESS, incident?.photoStatus)
    }

    @Test
    fun updateIncidentPhotoMarksFailureAndPreservesIncident() = runTest {
        val id = repository.insert(testIncident(photoEnabled = true))

        PhotoIncidentUpdater(
            incidentRepository = repository,
            photoCaptureClient = fakeFailureClient("camera unavailable")
        ).updateIncidentPhoto(id)

        val incident = repository.getById(id).first()
        assertEquals(id, incident?.id)
        assertNull(incident?.photoPath)
        assertNull(incident?.imageSha256)
        assertEquals(PhotoIncidentUpdater.STATUS_FAILED, incident?.photoStatus)
        assertEquals(true, incident?.notes?.contains("camera unavailable"))
    }

    @Test
    fun updateIncidentPhotoDoesNotCaptureWhenPhotoDisabled() = runTest {
        val id = repository.insert(testIncident(photoEnabled = false))

        PhotoIncidentUpdater(
            incidentRepository = repository,
            photoCaptureClient = object : PhotoCaptureClient {
                override suspend fun captureFrontPhoto(): PhotoCaptureResult {
                    error("Capture should not be called when photo is disabled.")
                }
            }
        ).updateIncidentPhoto(id)

        val incident = repository.getById(id).first()
        assertEquals(FailedUnlockIncidentCreator.STATUS_DISABLED, incident?.photoStatus)
        assertNull(incident?.photoPath)
        assertNull(incident?.imageSha256)
    }

    private fun fakeSuccessClient(file: java.io.File): PhotoCaptureClient =
        object : PhotoCaptureClient {
            override suspend fun captureFrontPhoto(): PhotoCaptureResult =
                PhotoCaptureResult.Success(file = file, sha256 = "hash")
        }

    private fun fakeFailureClient(reason: String): PhotoCaptureClient =
        object : PhotoCaptureClient {
            override suspend fun captureFrontPhoto(): PhotoCaptureResult =
                PhotoCaptureResult.Failure(reason)
        }

    private fun testIncident(photoEnabled: Boolean): SecurityIncident =
        SecurityIncident(
            timestamp = 1000L,
            triggerType = FailedUnlockIncidentCreator.TRIGGER_FAILED_UNLOCK,
            failedAttemptCount = 1,
            photoEnabled = photoEnabled,
            videoEnabled = false,
            locationEnabled = false,
            emailEnabled = false,
            shareEnabled = false,
            timelineEnabled = true,
            photoPath = null,
            videoPath = null,
            latitude = null,
            longitude = null,
            locationAccuracy = null,
            locationProvider = null,
            imageSha256 = null,
            videoSha256 = null,
            deviceModel = "model",
            androidVersion = "android",
            appVersion = "app",
            photoStatus = if (photoEnabled) {
                FailedUnlockIncidentCreator.STATUS_NOT_ATTEMPTED
            } else {
                FailedUnlockIncidentCreator.STATUS_DISABLED
            },
            videoStatus = FailedUnlockIncidentCreator.STATUS_DISABLED,
            locationStatus = FailedUnlockIncidentCreator.STATUS_DISABLED,
            emailStatus = FailedUnlockIncidentCreator.STATUS_DISABLED,
            shareStatus = FailedUnlockIncidentCreator.STATUS_DISABLED,
            notes = "shell"
        )
}
