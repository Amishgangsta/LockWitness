package com.lockwitness.app.ui.history

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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class IncidentHistoryMapperTest {
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
    fun summaryMapsStatusAndStoredMediaIndicators() {
        val mapper = IncidentHistoryMapper(clockFormatter = { "time-$it" })

        val summary = mapper.toSummary(testIncident())

        assertEquals("time-1000", summary.timestamp)
        assertEquals(FailedUnlockIncidentCreator.TRIGGER_FAILED_UNLOCK, summary.triggerType)
        assertEquals("2", summary.failedAttemptCount)
        assertEquals(PhotoIncidentUpdater.STATUS_SUCCESS, summary.photoStatus)
        assertEquals(VideoIncidentUpdater.STATUS_SUCCESS, summary.videoStatus)
        assertEquals(LocationIncidentUpdater.STATUS_SUCCESS, summary.locationStatus)
        assertTrue(summary.hasPhoto)
        assertTrue(summary.hasVideo)
        assertTrue(summary.hasLocation)
    }

    @Test
    fun detailMapsMetadataStatusHashesAndPaths() {
        val mapper = IncidentHistoryMapper(clockFormatter = { "time-$it" })

        val detail = mapper.toDetail(testIncident())

        assertEquals("time-1000", detail.timestamp)
        assertEquals("2", detail.failedAttemptCount)
        assertTrue(detail.settingsSnapshot.contains("Photo enabled" to "Yes"))
        assertTrue(detail.deviceMetadata.contains("Device model" to "model"))
        assertTrue(detail.mediaFields.contains("Photo path" to "photo.jpg"))
        assertTrue(detail.mediaFields.contains("Video path" to "video.mp4"))
        assertTrue(detail.locationFields.contains("Latitude" to "35.0"))
        assertTrue(detail.locationFields.contains("Longitude" to "-80.0"))
        assertTrue(detail.hashFields.contains("Image SHA-256" to "image-hash"))
        assertTrue(detail.hashFields.contains("Video SHA-256" to "video-hash"))
        assertTrue(detail.statusFields.contains("Location status" to LocationIncidentUpdater.STATUS_SUCCESS))
        assertEquals("notes", detail.notes)
    }

    @Test
    fun actionsDeleteSingleIncident() = runTest {
        val actions = IncidentHistoryActions(repository)
        val keepId = repository.insert(testIncident(timestamp = 1000L, notes = "keep"))
        val deleteId = repository.insert(testIncident(timestamp = 2000L, notes = "delete"))

        assertEquals(1, actions.deleteIncident(deleteId))

        val incidents = repository.getAllOrderedByTimestampDesc().first()
        assertEquals(listOf(keepId), incidents.map { it.id })
    }

    @Test
    fun actionsClearAllIncidents() = runTest {
        val actions = IncidentHistoryActions(repository)
        repository.insert(testIncident(timestamp = 1000L, notes = "one"))
        repository.insert(testIncident(timestamp = 2000L, notes = "two"))

        actions.clearIncidents()

        assertFalse(repository.getAllOrderedByTimestampDesc().first().isNotEmpty())
    }

    private fun testIncident(
        timestamp: Long = 1000L,
        notes: String = "notes"
    ): SecurityIncident =
        SecurityIncident(
            timestamp = timestamp,
            triggerType = FailedUnlockIncidentCreator.TRIGGER_FAILED_UNLOCK,
            failedAttemptCount = 2,
            photoEnabled = true,
            videoEnabled = true,
            locationEnabled = true,
            emailEnabled = false,
            shareEnabled = false,
            timelineEnabled = true,
            photoPath = "photo.jpg",
            videoPath = "video.mp4",
            latitude = 35.0,
            longitude = -80.0,
            locationAccuracy = 9.5f,
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
