package com.lockwitness.app.admin

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.lockwitness.app.data.SettingsRepository
import com.lockwitness.app.data.incident.LockWitnessDatabase
import com.lockwitness.app.data.incident.SecurityIncidentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LockWitnessDeviceAdminReceiver : DeviceAdminReceiver() {
    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onPasswordFailed(context: Context, intent: Intent) {
        super.onPasswordFailed(context, intent)

        val appContext = context.applicationContext
        val failedAttemptCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching {
                appContext
                    .getSystemService(DevicePolicyManager::class.java)
                    .currentFailedPasswordAttempts
            }.getOrDefault(0)
        } else {
            0
        }

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            FailedUnlockIncidentCreator(
                settingsRepository = SettingsRepository.create(appContext),
                incidentRepository = SecurityIncidentRepository(
                    LockWitnessDatabase.getInstance(appContext).securityIncidentDao()
                ),
                deviceInfoProvider = AndroidDeviceInfoProvider(appContext)
            ).createIncidentShell(failedAttemptCount)
        }
    }
}
