package com.lockwitness.app.monetization

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import java.io.IOException

private val Context.lockWitnessMonetizationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "lockwitness_monetization"
)

class MonetizationRepository(
    private val dataStore: DataStore<Preferences>,
    private val billingService: ProBillingService = SafeFallbackBillingService()
) {
    val state: Flow<MonetizationState> = combine(
        dataStore.data.catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        },
        billingService.purchaseState
    ) { prefs, billingState ->
        val trialStartMs = prefs[Keys.TrialStartMs]
        val trialDaysRemaining = trialStartMs?.let {
            val remainingMs = it + TRIAL_DURATION_MS - System.currentTimeMillis()
            (remainingMs / DAY_MS).toInt().coerceAtLeast(0)
        }
        MonetizationState(
            isPro = billingState.isPro,
            billingAvailable = billingState.billingAvailable,
            trialDaysRemaining = trialDaysRemaining
        )
    }

    suspend fun startTrialIfNotStarted() {
        dataStore.edit { prefs ->
            if (prefs[Keys.TrialStartMs] == null) {
                prefs[Keys.TrialStartMs] = System.currentTimeMillis()
            }
        }
    }

    suspend fun refreshBillingStatus(): BillingStatus {
        val status = billingService.refreshStatus()
        dataStore.edit { preferences ->
            preferences[Keys.BillingAvailable] = status.available
            preferences[Keys.IsPro] = if (status.available) status.isPro else false
        }
        return status
    }

    suspend fun setLocalProStateForTesting(isPro: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.IsPro] = isPro
            preferences[Keys.BillingAvailable] = isPro
        }
    }

    suspend fun setLocalTrialStartForTesting(timestampMs: Long) {
        dataStore.edit { prefs -> prefs[Keys.TrialStartMs] = timestampMs }
    }

    private object Keys {
        val IsPro = booleanPreferencesKey("is_pro")
        val BillingAvailable = booleanPreferencesKey("billing_available")
        val TrialStartMs = longPreferencesKey("trial_start_ms")
    }

    companion object {
        private const val TRIAL_DURATION_MS = 7L * 24 * 60 * 60 * 1000
        private const val DAY_MS = 24L * 60 * 60 * 1000

        fun create(context: Context): MonetizationRepository =
            MonetizationRepository(
                dataStore = context.applicationContext.lockWitnessMonetizationDataStore,
                billingService = PlayBillingService.getInstance(context)
            )
    }
}
