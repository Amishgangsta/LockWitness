package com.lockwitness.app.admin

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

interface DeviceInfoProvider {
    val deviceModel: String
    val androidVersion: String
    val appVersion: String
}

class AndroidDeviceInfoProvider(
    private val context: Context
) : DeviceInfoProvider {
    override val deviceModel: String
        get() = "${Build.MANUFACTURER} ${Build.MODEL}".trim()

    override val androidVersion: String
        get() = Build.VERSION.RELEASE ?: Build.VERSION.SDK_INT.toString()

    override val appVersion: String
        get() {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            return packageInfo.versionName ?: "unknown"
        }
}
