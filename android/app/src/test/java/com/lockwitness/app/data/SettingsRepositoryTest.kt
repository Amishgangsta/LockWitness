package com.lockwitness.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val testScope = TestScope(UnconfinedTestDispatcher() + Job())
    private lateinit var dataStore: DataStore<Preferences>

    @After
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun defaultSettingsMatchPhaseSpecification() = runTest {
        val repository = createRepository()

        assertEquals(SettingsState.Defaults, repository.settings.first())
    }

    @Test
    fun settingsPersistAfterUpdates() = runTest {
        val repository = createRepository()

        repository.setMasterMonitoringEnabled(true)
        repository.setVideoCaptureEnabled(true)
        repository.setVideoDurationSeconds(30)
        repository.setLocationCaptureEnabled(true)
        repository.setEmailAlertEnabled(true)
        repository.setShareAlertEnabled(true)
        repository.setPhotoCaptureEnabled(false)
        repository.setLocalTimelineEnabled(false)
        repository.setEvidenceHashingEnabled(false)

        assertEquals(
            SettingsState(
                masterMonitoringEnabled = true,
                photoCaptureEnabled = false,
                videoCaptureEnabled = true,
                videoDurationSeconds = 30,
                locationCaptureEnabled = true,
                localTimelineEnabled = false,
                emailAlertEnabled = true,
                shareAlertEnabled = true,
                evidenceHashingEnabled = false
            ),
            repository.settings.first()
        )
    }

    @Test
    fun invalidVideoDurationFallsBackToDefault() = runTest {
        val repository = createRepository()

        repository.setVideoDurationSeconds(99)

        assertEquals(5, repository.settings.first().videoDurationSeconds)
    }

    private fun createRepository(): SettingsRepository {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = {
                temporaryFolder.newFile("${System.nanoTime()}-${DataStoreFactory::class.simpleName}.preferences_pb")
            }
        )
        return SettingsRepository(dataStore)
    }
}
