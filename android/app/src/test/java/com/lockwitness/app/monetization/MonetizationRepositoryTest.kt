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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun freshInstall_noTrialStarted_isNotProAndTrialDaysNull() = runTest {
        val repository = createRepository()

        val state = repository.state.first()

        assertFalse(state.isPro)
        assertNull(state.trialDaysRemaining)
        assertFalse(state.isInTrial)
        assertFalse(state.trialExpired)
    }

    @Test
    fun afterStartTrial_isInTrialWith7Days() = runTest {
        val repository = createRepository()

        repository.startTrialIfNotStarted()
        val state = repository.state.first()

        assertFalse(state.isPro)
        assertNotNull(state.trialDaysRemaining)
        assertTrue(state.trialDaysRemaining!! in 6..7)
        assertTrue(state.isInTrial)
        assertFalse(state.trialExpired)
    }

    @Test
    fun startTrialIfNotStarted_idempotent_doesNotResetTimer() = runTest {
        val repository = createRepository()

        repository.startTrialIfNotStarted()
        val first = repository.state.first()

        repository.startTrialIfNotStarted()
        val second = repository.state.first()

        assertEquals(first.trialDaysRemaining, second.trialDaysRemaining)
    }

    @Test
    fun expiredTrial_trialExpiredTrue_isNotPro() = runTest {
        val billingService = fixedStateBillingService(isPro = false)
        val repository = createRepository(billingService = billingService)

        // Simulate expired trial by directly writing a past timestamp
        repository.simulateExpiredTrial()
        val state = repository.state.first()

        assertFalse(state.isPro)
        assertEquals(0, state.trialDaysRemaining)
        assertFalse(state.isInTrial)
        assertTrue(state.trialExpired)
    }

    @Test
    fun proPurchased_isPro_trialFieldsIgnored() = runTest {
        val billingService = fixedStateBillingService(isPro = true)
        val repository = createRepository(billingService = billingService)

        repository.startTrialIfNotStarted()
        val state = repository.state.first()

        assertTrue(state.isPro)
        assertFalse(state.isInTrial)
        assertFalse(state.trialExpired)
    }

    @Test
    fun billingUnavailable_fallsBackToTrialState() = runTest {
        val billingService = object : ProBillingService {
            override suspend fun refreshStatus(): BillingStatus =
                BillingStatus(available = false, message = "billing down")
        }
        val repository = createRepository(billingService = billingService)

        repository.startTrialIfNotStarted()
        val state = repository.state.first()

        assertFalse(state.isPro)
        assertTrue(state.isInTrial)
    }

    private fun fixedStateBillingService(isPro: Boolean): ProBillingService =
        object : ProBillingService {
            override suspend fun refreshStatus(): BillingStatus =
                BillingStatus(available = true, isPro = isPro, message = if (isPro) "Pro active." else "base mode.")

            override val purchaseState = kotlinx.coroutines.flow.flowOf(
                MonetizationState(isPro = isPro, billingAvailable = true)
            )
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

private suspend fun MonetizationRepository.simulateExpiredTrial() {
    // Write a trial start timestamp 8 days in the past so the trial is expired
    val eightDaysAgoMs = System.currentTimeMillis() - (8L * 24 * 60 * 60 * 1000)
    setLocalTrialStartForTesting(eightDaysAgoMs)
}
