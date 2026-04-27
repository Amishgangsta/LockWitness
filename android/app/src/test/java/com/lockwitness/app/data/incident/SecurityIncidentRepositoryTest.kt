package com.lockwitness.app.data.incident

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SecurityIncidentRepositoryTest {
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
    fun insertAndReadReturnsIncidentById() = runTest {
        val id = repository.insert(testIncident(timestamp = 1000L, notes = "first"))

        val incident = repository.getById(id).first()

        assertEquals(id, incident?.id)
        assertEquals("manual-test", incident?.triggerType)
        assertEquals("first", incident?.notes)
    }

    @Test
    fun getAllOrdersByTimestampDescending() = runTest {
        val olderId = repository.insert(testIncident(timestamp = 1000L, notes = "older"))
        val newerId = repository.insert(testIncident(timestamp = 2000L, notes = "newer"))

        val incidents = repository.getAllOrderedByTimestampDesc().first()

        assertEquals(listOf(newerId, olderId), incidents.map { it.id })
    }

    @Test
    fun deleteByIdRemovesOnlyMatchingIncident() = runTest {
        val keepId = repository.insert(testIncident(timestamp = 1000L, notes = "keep"))
        val deleteId = repository.insert(testIncident(timestamp = 2000L, notes = "delete"))

        assertEquals(1, repository.deleteById(deleteId))

        assertNull(repository.getById(deleteId).first())
        assertEquals(keepId, repository.getById(keepId).first()?.id)
    }

    @Test
    fun clearAllRemovesAllIncidents() = runTest {
        repository.insert(testIncident(timestamp = 1000L, notes = "one"))
        repository.insert(testIncident(timestamp = 2000L, notes = "two"))

        repository.clearAll()

        assertEquals(emptyList<SecurityIncident>(), repository.getAllOrderedByTimestampDesc().first())
    }

    private fun testIncident(timestamp: Long, notes: String): SecurityIncident =
        SecurityIncident(
            timestamp = timestamp,
            triggerType = "manual-test",
            failedAttemptCount = 0,
            photoEnabled = true,
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
            deviceModel = "test-model",
            androidVersion = "test-android",
            appVersion = "test-app",
            photoStatus = "not_started",
            videoStatus = "not_started",
            locationStatus = "not_started",
            emailStatus = "not_started",
            shareStatus = "not_started",
            notes = notes
        )
}
