package com.lockwitness.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.lockWitnessSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "lockwitness_settings"
)

data class SettingsState(
    val masterMonitoringEnabled: Boolean = false,
    val photoCaptureEnabled: Boolean = true,
    val videoCaptureEnabled: Boolean = false,
    val videoDurationSeconds: Int = 5,
    val locationCaptureEnabled: Boolean = false,
    val localTimelineEnabled: Boolean = true,
    val emailAlertEnabled: Boolean = false,
    val shareAlertEnabled: Boolean = false,
    val evidenceHashingEnabled: Boolean = true,
    val autoDeleteDays: Int = 0
) {
    companion object {
        val Defaults = SettingsState()
        val AllowedVideoDurations = listOf(5, 10, 15, 30)
        val AllowedAutoDeleteDays = listOf(0, 30, 60, 90)
    }
}

class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    val settings: Flow<SettingsState> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            SettingsState(
                masterMonitoringEnabled = preferences[Keys.MasterMonitoring] ?: SettingsState.Defaults.masterMonitoringEnabled,
                photoCaptureEnabled = preferences[Keys.PhotoCapture] ?: SettingsState.Defaults.photoCaptureEnabled,
                videoCaptureEnabled = preferences[Keys.VideoCapture] ?: SettingsState.Defaults.videoCaptureEnabled,
                videoDurationSeconds = sanitizeVideoDuration(
                    preferences[Keys.VideoDurationSeconds] ?: SettingsState.Defaults.videoDurationSeconds
                ),
                locationCaptureEnabled = preferences[Keys.LocationCapture] ?: SettingsState.Defaults.locationCaptureEnabled,
                localTimelineEnabled = preferences[Keys.LocalTimeline] ?: SettingsState.Defaults.localTimelineEnabled,
                emailAlertEnabled = preferences[Keys.EmailAlert] ?: SettingsState.Defaults.emailAlertEnabled,
                shareAlertEnabled = preferences[Keys.ShareAlert] ?: SettingsState.Defaults.shareAlertEnabled,
                evidenceHashingEnabled = preferences[Keys.EvidenceHashing] ?: SettingsState.Defaults.evidenceHashingEnabled,
                autoDeleteDays = preferences[Keys.AutoDeleteDays] ?: SettingsState.Defaults.autoDeleteDays
            )
        }

    suspend fun setMasterMonitoringEnabled(enabled: Boolean) = update(Keys.MasterMonitoring, enabled)
    suspend fun setPhotoCaptureEnabled(enabled: Boolean) = update(Keys.PhotoCapture, enabled)
    suspend fun setVideoCaptureEnabled(enabled: Boolean) = update(Keys.VideoCapture, enabled)
    suspend fun setLocationCaptureEnabled(enabled: Boolean) = update(Keys.LocationCapture, enabled)
    suspend fun setLocalTimelineEnabled(enabled: Boolean) = update(Keys.LocalTimeline, enabled)
    suspend fun setEmailAlertEnabled(enabled: Boolean) = update(Keys.EmailAlert, enabled)
    suspend fun setShareAlertEnabled(enabled: Boolean) = update(Keys.ShareAlert, enabled)
    suspend fun setEvidenceHashingEnabled(enabled: Boolean) = update(Keys.EvidenceHashing, enabled)

    suspend fun setAutoDeleteDays(days: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.AutoDeleteDays] = if (days in SettingsState.AllowedAutoDeleteDays) days else 0
        }
    }

    suspend fun setVideoDurationSeconds(seconds: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.VideoDurationSeconds] = sanitizeVideoDuration(seconds)
        }
    }

    private suspend fun update(key: Preferences.Key<Boolean>, enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[key] = enabled
        }
    }

    private fun sanitizeVideoDuration(seconds: Int): Int =
        if (seconds in SettingsState.AllowedVideoDurations) seconds else SettingsState.Defaults.videoDurationSeconds

    private object Keys {
        val MasterMonitoring = booleanPreferencesKey("master_monitoring_enabled")
        val PhotoCapture = booleanPreferencesKey("photo_capture_enabled")
        val VideoCapture = booleanPreferencesKey("video_capture_enabled")
        val VideoDurationSeconds = intPreferencesKey("video_duration_seconds")
        val LocationCapture = booleanPreferencesKey("location_capture_enabled")
        val LocalTimeline = booleanPreferencesKey("local_timeline_enabled")
        val EmailAlert = booleanPreferencesKey("email_alert_enabled")
        val ShareAlert = booleanPreferencesKey("share_alert_enabled")
        val EvidenceHashing = booleanPreferencesKey("evidence_hashing_enabled")
        val AutoDeleteDays = intPreferencesKey("auto_delete_days")
    }

    companion object {
        fun create(context: Context): SettingsRepository =
            SettingsRepository(context.applicationContext.lockWitnessSettingsDataStore)
    }
}
