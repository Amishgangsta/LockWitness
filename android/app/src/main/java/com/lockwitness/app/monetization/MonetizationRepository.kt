package com.lockwitness.app.monetization

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.lockwitness.app.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.lockWitnessMonetizationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "lockwitness_monetization"
)

class MonetizationRepository(
    private val dataStore: DataStore<Preferences>,
    private val billingService: ProBillingService = SafeFallbackBillingService()
) {
    val state: Flow<MonetizationState> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            MonetizationState(
                isPro = preferences[Keys.IsPro] ?: BuildConfig.DEBUG,
                billingAvailable = preferences[Keys.BillingAvailable] ?: BuildConfig.DEBUG
            )
        }

    suspend fun refreshBillingStatus(): BillingStatus {
        val status = billingService.refreshStatus()
        dataStore.edit { preferences ->
            preferences[Keys.BillingAvailable] = status.available
            if (!status.available) {
                preferences[Keys.IsPro] = false
            }
        }
        return status
    }

    suspend fun setLocalProStateForTesting(isPro: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.IsPro] = isPro
            preferences[Keys.BillingAvailable] = isPro
        }
    }

    private object Keys {
        val IsPro = booleanPreferencesKey("is_pro")
        val BillingAvailable = booleanPreferencesKey("billing_available")
    }

    companion object {
        fun create(context: Context): MonetizationRepository =
            MonetizationRepository(context.applicationContext.lockWitnessMonetizationDataStore)
    }
}
