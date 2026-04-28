package com.lockwitness.app.monetization

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
class MonetizationRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val testScope = TestScope(UnconfinedTestDispatcher() + Job())
    private lateinit var dataStore: DataStore<Preferences>

    @After
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun defaultStateIsFreeAndBillingUnavailable() = runTest {
        val repository = createRepository()

        assertEquals(MonetizationState.Free, repository.state.first())
    }

    @Test
    fun billingUnavailableFallsBackToFreeMode() = runTest {
        val repository = createRepository(
            billingService = object : ProBillingService {
                override suspend fun refreshStatus(): BillingStatus =
                    BillingStatus(available = false, message = "billing down")
            }
        )
        repository.setLocalProStateForTesting(true)

        val status = repository.refreshBillingStatus()

        assertEquals(false, status.available)
        assertEquals(MonetizationState.Free, repository.state.first())
    }

    @Test
    fun localProStateCanBeReadForGateScaffolding() = runTest {
        val repository = createRepository()

        repository.setLocalProStateForTesting(true)

        assertEquals(MonetizationState.Pro, repository.state.first())
    }

    private fun createRepository(
        billingService: ProBillingService = SafeFallbackBillingService()
    ): MonetizationRepository {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = {
                temporaryFolder.newFile("${System.nanoTime()}-${DataStoreFactory::class.simpleName}.preferences_pb")
            }
        )
        return MonetizationRepository(dataStore, billingService)
    }
}
