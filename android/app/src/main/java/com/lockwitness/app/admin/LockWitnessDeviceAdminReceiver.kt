package com.lockwitness.app.admin

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.lockwitness.app.capture.LockWitnessCaptureService

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

        val serviceIntent = Intent(appContext, LockWitnessCaptureService::class.java).apply {
            putExtra(LockWitnessCaptureService.EXTRA_FAILED_ATTEMPT_COUNT, failedAttemptCount)
        }
        appContext.startForegroundService(serviceIntent)
    }
}
