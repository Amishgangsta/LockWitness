package com.lockwitness.app.admin

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lockwitness.app.data.SettingsRepository
import com.lockwitness.app.data.incident.LockWitnessDatabase
import com.lockwitness.app.data.incident.SecurityIncidentRepository
import com.lockwitness.app.monetization.MonetizationState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FailedUnlockIncidentCreatorTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val testScope = TestScope(UnconfinedTestDispatcher() + Job())
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var database: LockWitnessDatabase
    private lateinit var incidentRepository: SecurityIncidentRepository

    @Before
    fun setUp() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = {
                temporaryFolder.newFile("${System.nanoTime()}-settings.preferences_pb")
            }
        )
        settingsRepository = SettingsRepository(dataStore)
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LockWitnessDatabase::class.java
        ).allowMainThreadQueries().build()
        incidentRepository = SecurityIncidentRepository(database.securityIncidentDao())
    }

    @After
    fun tearDown() {
        database.close()
        testScope.cancel()
    }

    @Test
    fun createIncidentShellSkipsWhenMasterMonitoringIsOff() = runTest {
        val creator = creator()

        val id = creator.createIncidentShell(failedAttemptCount = 2)

        assertNull(id)
        assertEquals(emptyList<Any>(), incidentRepository.getAllOrderedByTimestampDesc().first())
    }

    @Test
    fun createIncidentShellPersistsSettingsSnapshotAndStatuses() = runTest {
        settingsRepository.setMasterMonitoringEnabled(true)
        settingsRepository.setPhotoCaptureEnabled(true)
        settingsRepository.setVideoCaptureEnabled(false)
        settingsRepository.setLocationCaptureEnabled(true)
        settingsRepository.setEmailAlertEnabled(false)
        settingsRepository.setShareAlertEnabled(true)
        settingsRepository.setLocalTimelineEnabled(true)

        val id = creator().createIncidentShell(failedAttemptCount = 3)
        val incident = id?.let { incidentRepository.getById(it).first() }

        assertNotNull(id)
        assertEquals(1234L, incident?.timestamp)
        assertEquals(FailedUnlockIncidentCreator.TRIGGER_FAILED_UNLOCK, incident?.triggerType)
        assertEquals(3, incident?.failedAttemptCount)
        assertEquals(true, incident?.photoEnabled)
        assertEquals(false, incident?.videoEnabled)
        assertEquals(true, incident?.locationEnabled)
        assertEquals(false, incident?.emailEnabled)
        assertEquals(true, incident?.shareEnabled)
        assertEquals(true, incident?.timelineEnabled)
        assertEquals("Test Model", incident?.deviceModel)
        assertEquals("Test Android", incident?.androidVersion)
        assertEquals("Test App", incident?.appVersion)
        assertEquals(FailedUnlockIncidentCreator.STATUS_NOT_ATTEMPTED, incident?.photoStatus)
        assertEquals(FailedUnlockIncidentCreator.STATUS_DISABLED, incident?.videoStatus)
        assertEquals(FailedUnlockIncidentCreator.STATUS_NOT_ATTEMPTED, incident?.locationStatus)
        assertEquals(FailedUnlockIncidentCreator.STATUS_DISABLED, incident?.emailStatus)
        assertEquals(FailedUnlockIncidentCreator.STATUS_NOT_ATTEMPTED, incident?.shareStatus)
        assertNull(incident?.photoPath)
        assertNull(incident?.videoPath)
        assertNull(incident?.latitude)
        assertNull(incident?.longitude)
        assertNull(incident?.imageSha256)
        assertNull(incident?.videoSha256)
    }

    @Test
    fun createIncidentShellDisablesProGatedVideoAndLocationInFreeMode() = runTest {
        settingsRepository.setMasterMonitoringEnabled(true)
        settingsRepository.setPhotoCaptureEnabled(true)
        settingsRepository.setVideoCaptureEnabled(true)
        settingsRepository.setLocationCaptureEnabled(true)

        val id = creator(monetizationState = MonetizationState.Free).createIncidentShell(failedAttemptCount = 1)
        val incident = id?.let { incidentRepository.getById(it).first() }

        assertNotNull(id)
        assertEquals(true, incident?.photoEnabled)
        assertEquals(false, incident?.videoEnabled)
        assertEquals(false, incident?.locationEnabled)
        assertEquals(FailedUnlockIncidentCreator.STATUS_NOT_ATTEMPTED, incident?.photoStatus)
        assertEquals(FailedUnlockIncidentCreator.STATUS_DISABLED, incident?.videoStatus)
        assertEquals(FailedUnlockIncidentCreator.STATUS_DISABLED, incident?.locationStatus)
    }

    private fun creator(
        monetizationState: MonetizationState = MonetizationState.Pro
    ): FailedUnlockIncidentCreator =
        FailedUnlockIncidentCreator(
            settingsRepository = settingsRepository,
            incidentRepository = incidentRepository,
            deviceInfoProvider = object : DeviceInfoProvider {
                override val deviceModel: String = "Test Model"
                override val androidVersion: String = "Test Android"
                override val appVersion: String = "Test App"
            },
            monetizationStateProvider = { monetizationState },
            clock = { 1234L }
        )
}
