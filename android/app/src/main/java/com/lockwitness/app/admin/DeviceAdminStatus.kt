package com.lockwitness.app.admin

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object DeviceAdminStatus {
    fun componentName(context: Context): ComponentName =
        ComponentName(context.applicationContext, LockWitnessDeviceAdminReceiver::class.java)

    fun isActive(context: Context): Boolean {
        val manager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return manager.isAdminActive(componentName(context))
    }

    fun activationIntent(context: Context): Intent =
        Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName(context))
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "LockWitness uses Device Admin only to receive failed unlock events when monitoring is enabled."
            )
        }
}
