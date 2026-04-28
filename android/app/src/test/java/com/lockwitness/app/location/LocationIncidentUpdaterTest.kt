package com.lockwitness.app.location

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lockwitness.app.admin.FailedUnlockIncidentCreator
import com.lockwitness.app.data.incident.LockWitnessDatabase
import com.lockwitness.app.data.incident.SecurityIncident
import com.lockwitness.app.data.incident.SecurityIncidentRepository
import com.lockwitness.app.photo.PhotoIncidentUpdater
import com.lockwitness.app.video.VideoIncidentUpdater
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class LocationIncidentUpdaterTest {
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
    fun updateIncidentLocationDoesNotCaptureWhenLocationDisabled() = runTest {
        val id = repository.insert(testIncident(locationEnabled = false))

        LocationIncidentUpdater(
            incidentRepository = repository,
            locationSnapshotClient = object : LocationSnapshotClient {
                override suspend fun captureLocationSnapshot(): LocationSnapshotResult {
                    error("Location should not be called when location is disabled.")
                }
            }
        ).updateIncidentLocation(id)

        val incident = repository.getById(id).first()
        assertEquals(FailedUnlockIncidentCreator.STATUS_DISABLED, incident?.locationStatus)
        assertNull(incident?.latitude)
        assertNull(incident?.longitude)
        assertNull(incident?.locationAccuracy)
        assertNull(incident?.locationProvider)
    }

    @Test
    fun updateIncidentLocationStoresSuccessMetadataAndStatus() = runTest {
        val id = repository.insert(testIncident(locationEnabled = true))

        LocationIncidentUpdater(
            incidentRepository = repository,
            locationSnapshotClient = fakeSuccessClient()
        ).updateIncidentLocation(id)

        val incident = repository.getById(id).first()
        assertEquals(35.1234, incident?.latitude)
        assertEquals(-80.5678, incident?.longitude)
        assertEquals(12.5f, incident?.locationAccuracy)
        assertEquals("gps", incident?.locationProvider)
        assertEquals(LocationIncidentUpdater.STATUS_SUCCESS, incident?.locationStatus)
    }

    @Test
    fun updateIncidentLocationMarksUnavailableAndPreservesIncident() = runTest {
        val id = repository.insert(testIncident(locationEnabled = true))

        LocationIncidentUpdater(
            incidentRepository = repository,
            locationSnapshotClient = fakeUnavailableClient("provider disabled")
        ).updateIncidentLocation(id)

        val incident = repository.getById(id).first()
        assertEquals(id, incident?.id)
        assertNull(incident?.latitude)
        assertNull(incident?.longitude)
        assertEquals(LocationIncidentUpdater.STATUS_UNAVAILABLE, incident?.locationStatus)
        assertEquals(true, incident?.notes?.contains("provider disabled"))
    }

    @Test
    fun updateIncidentLocationMarksFailureAndPreservesIncident() = runTest {
        val id = repository.insert(testIncident(locationEnabled = true))

        LocationIncidentUpdater(
            incidentRepository = repository,
            locationSnapshotClient = fakeFailureClient("permission denied")
        ).updateIncidentLocation(id)

        val incident = repository.getById(id).first()
        assertEquals(id, incident?.id)
        assertNull(incident?.latitude)
        assertNull(incident?.longitude)
        assertEquals(LocationIncidentUpdater.STATUS_FAILED, incident?.locationStatus)
        assertEquals(true, incident?.notes?.contains("permission denied"))
    }

    @Test
    fun updateIncidentLocationFailurePreservesPhotoAndVideoSuccess() = runTest {
        val id = repository.insert(
            testIncident(
                locationEnabled = true,
                photoPath = "photo.jpg",
                imageSha256 = "photo-hash",
                photoStatus = PhotoIncidentUpdater.STATUS_SUCCESS,
                videoPath = "video.mp4",
                videoSha256 = "video-hash",
                videoStatus = VideoIncidentUpdater.STATUS_SUCCESS
            )
        )

        LocationIncidentUpdater(
            incidentRepository = repository,
            locationSnapshotClient = fakeFailureClient("location failed")
        ).updateIncidentLocation(id)

        val incident = repository.getById(id).first()
        assertEquals("photo.jpg", incident?.photoPath)
        assertEquals("photo-hash", incident?.imageSha256)
        assertEquals(PhotoIncidentUpdater.STATUS_SUCCESS, incident?.photoStatus)
        assertEquals("video.mp4", incident?.videoPath)
        assertEquals("video-hash", incident?.videoSha256)
        assertEquals(VideoIncidentUpdater.STATUS_SUCCESS, incident?.videoStatus)
        assertEquals(LocationIncidentUpdater.STATUS_FAILED, incident?.locationStatus)
    }

    private fun fakeSuccessClient(): LocationSnapshotClient =
        object : LocationSnapshotClient {
            override suspend fun captureLocationSnapshot(): LocationSnapshotResult =
                LocationSnapshotResult.Success(
                    latitude = 35.1234,
                    longitude = -80.5678,
                    accuracy = 12.5f,
                    provider = "gps"
                )
        }

    private fun fakeUnavailableClient(reason: String): LocationSnapshotClient =
        object : LocationSnapshotClient {
            override suspend fun captureLocationSnapshot(): LocationSnapshotResult =
                LocationSnapshotResult.Unavailable(reason)
        }

    private fun fakeFailureClient(reason: String): LocationSnapshotClient =
        object : LocationSnapshotClient {
            override suspend fun captureLocationSnapshot(): LocationSnapshotResult =
                LocationSnapshotResult.Failure(reason)
        }

    private fun testIncident(
        locationEnabled: Boolean,
        photoPath: String? = null,
        imageSha256: String? = null,
        photoStatus: String = FailedUnlockIncidentCreator.STATUS_NOT_ATTEMPTED,
        videoPath: String? = null,
        videoSha256: String? = null,
        videoStatus: String = FailedUnlockIncidentCreator.STATUS_NOT_ATTEMPTED
    ): SecurityIncident =
        SecurityIncident(
            timestamp = 1000L,
            triggerType = FailedUnlockIncidentCreator.TRIGGER_FAILED_UNLOCK,
            failedAttemptCount = 1,
            photoEnabled = true,
            videoEnabled = true,
            locationEnabled = locationEnabled,
            emailEnabled = false,
            shareEnabled = false,
            timelineEnabled = true,
            photoPath = photoPath,
            videoPath = videoPath,
            latitude = null,
            longitude = null,
            locationAccuracy = null,
            locationProvider = null,
            imageSha256 = imageSha256,
            videoSha256 = videoSha256,
            deviceModel = "model",
            androidVersion = "android",
            appVersion = "app",
            photoStatus = photoStatus,
            videoStatus = videoStatus,
            locationStatus = if (locationEnabled) {
                FailedUnlockIncidentCreator.STATUS_NOT_ATTEMPTED
            } else {
                FailedUnlockIncidentCreator.STATUS_DISABLED
            },
            emailStatus = FailedUnlockIncidentCreator.STATUS_DISABLED,
            shareStatus = FailedUnlockIncidentCreator.STATUS_DISABLED,
            notes = "shell"
        )
}
