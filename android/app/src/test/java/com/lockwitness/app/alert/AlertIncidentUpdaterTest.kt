package com.lockwitness.app.alert

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lockwitness.app.admin.FailedUnlockIncidentCreator
import com.lockwitness.app.data.incident.LockWitnessDatabase
import com.lockwitness.app.data.incident.SecurityIncident
import com.lockwitness.app.data.incident.SecurityIncidentRepository
import com.lockwitness.app.location.LocationIncidentUpdater
import com.lockwitness.app.photo.PhotoIncidentUpdater
import com.lockwitness.app.video.VideoIncidentUpdater
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AlertIncidentUpdaterTest {
    private lateinit var database: LockWitnessDatabase
    private lateinit var repository: SecurityIncidentRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LockWitnessDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = SecurityIncidentRepository(database.securityIncidentDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun markUserActionRequiredKeepsDisabledTogglesDisabled() = runTest {
        val id = repository.insert(testIncident(emailEnabled = false, shareEnabled = false))

        AlertIncidentUpdater(repository).markUserActionRequired(id)

        val incident = repository.getById(id).first()
        assertEquals(FailedUnlockIncidentCreator.STATUS_DISABLED, incident?.emailStatus)
        assertEquals(FailedUnlockIncidentCreator.STATUS_DISABLED, incident?.shareStatus)
    }

    @Test
    fun markUserActionRequiredMarksEnabledAlertsUnavailable() = runTest {
        val id = repository.insert(testIncident(emailEnabled = true, shareEnabled = true))

        AlertIncidentUpdater(repository).markUserActionRequired(id)

        val incident = repository.getById(id).first()
        assertEquals(AlertIncidentUpdater.STATUS_UNAVAILABLE, incident?.emailStatus)
        assertEquals(AlertIncidentUpdater.STATUS_UNAVAILABLE, incident?.shareStatus)
        assertTrue(incident?.notes?.contains("no automatic email was sent") == true)
        assertTrue(incident?.notes?.contains("no automatic share was sent") == true)
    }

    @Test
    fun markManualShareLaunchedUsesExistingTogglesForSuccessAndDisabled() = runTest {
        val id = repository.insert(testIncident(emailEnabled = true, shareEnabled = false))

        AlertIncidentUpdater(repository).markManualShareLaunched(id)

        val incident = repository.getById(id).first()
        assertEquals(AlertIncidentUpdater.STATUS_SUCCESS, incident?.emailStatus)
        assertEquals(FailedUnlockIncidentCreator.STATUS_DISABLED, incident?.shareStatus)
        assertTrue(incident?.notes?.contains("chooser launched") == true)
    }

    @Test
    fun markManualShareFailedUsesExistingTogglesForFailedAndDisabled() = runTest {
        val id = repository.insert(testIncident(emailEnabled = false, shareEnabled = true))

        AlertIncidentUpdater(repository).markManualShareFailed(id, "no chooser")

        val incident = repository.getById(id).first()
        assertEquals(FailedUnlockIncidentCreator.STATUS_DISABLED, incident?.emailStatus)
        assertEquals(AlertIncidentUpdater.STATUS_FAILED, incident?.shareStatus)
        assertTrue(incident?.notes?.contains("no chooser") == true)
    }

    @Test
    fun alertFailurePreservesEvidenceFields() = runTest {
        val id = repository.insert(testIncident(emailEnabled = true, shareEnabled = true))

        AlertIncidentUpdater(repository).markManualShareFailed(id, "no chooser")

        val incident = repository.getById(id).first()
        assertEquals("photo.jpg", incident?.photoPath)
        assertEquals("image-hash", incident?.imageSha256)
        assertEquals("video.mp4", incident?.videoPath)
        assertEquals("video-hash", incident?.videoSha256)
        assertEquals(35.0, incident?.latitude)
        assertEquals(-80.0, incident?.longitude)
        assertEquals(PhotoIncidentUpdater.STATUS_SUCCESS, incident?.photoStatus)
        assertEquals(VideoIncidentUpdater.STATUS_SUCCESS, incident?.videoStatus)
        assertEquals(LocationIncidentUpdater.STATUS_SUCCESS, incident?.locationStatus)
    }

    @Test
    fun missingIncidentReturnsFailureWithoutStatusUpdate() = runTest {
        val result = AlertIncidentUpdater(repository).markUserActionRequired(123L)

        assertTrue(result is AlertUpdateResult.Failed)
        assertNull(repository.getById(123L).first())
    }

    private fun testIncident(
        emailEnabled: Boolean,
        shareEnabled: Boolean
    ): SecurityIncident =
        SecurityIncident(
            id = 0L,
            timestamp = 1000L,
            triggerType = FailedUnlockIncidentCreator.TRIGGER_FAILED_UNLOCK,
            failedAttemptCount = 1,
            photoEnabled = true,
            videoEnabled = true,
            locationEnabled = true,
            emailEnabled = emailEnabled,
            shareEnabled = shareEnabled,
            timelineEnabled = true,
            photoPath = "photo.jpg",
            videoPath = "video.mp4",
            latitude = 35.0,
            longitude = -80.0,
            locationAccuracy = 5.5f,
            locationProvider = "gps",
            imageSha256 = "image-hash",
            videoSha256 = "video-hash",
            deviceModel = "model",
            androidVersion = "android",
            appVersion = "app",
            photoStatus = PhotoIncidentUpdater.STATUS_SUCCESS,
            videoStatus = VideoIncidentUpdater.STATUS_SUCCESS,
            locationStatus = LocationIncidentUpdater.STATUS_SUCCESS,
            emailStatus = if (emailEnabled) {
                FailedUnlockIncidentCreator.STATUS_NOT_ATTEMPTED
            } else {
                FailedUnlockIncidentCreator.STATUS_DISABLED
            },
            shareStatus = if (shareEnabled) {
                FailedUnlockIncidentCreator.STATUS_NOT_ATTEMPTED
            } else {
                FailedUnlockIncidentCreator.STATUS_DISABLED
            },
            notes = "shell"
        )
}
