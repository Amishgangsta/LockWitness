package com.lockwitness.app.diagnostics

import com.lockwitness.app.data.SettingsState
import com.lockwitness.app.monetization.MonetizationState
import org.junit.Assert.assertEquals
import org.junit.Test

class DiagnosticMapperTest {
    private val mapper = DiagnosticMapper()

    @Test
    fun checksMapCoreRuntimeStateToResults() {
        val checks = mapper.checks(
            DiagnosticInput(
                isDeviceAdminActive = true,
                isCameraPermissionGranted = true,
                isLocationPermissionGranted = false,
                settings = SettingsState.Defaults.copy(
                    masterMonitoringEnabled = true,
                    photoCaptureEnabled = true,
                    videoCaptureEnabled = false,
                    locationCaptureEnabled = false
                ),
                historyAvailable = true,
                exportAvailable = false,
                monetizationState = MonetizationState.Free,
                appVersion = "1.0",
                androidVersion = "Android",
                deviceModel = "Device"
            )
        )

        assertEquals(DiagnosticResult.WARNING, checks.first { it.name == "Video capture" }.result)
        assertEquals(DiagnosticResult.FAIL, checks.first { it.name == "Export" }.result)
        assertEquals("Free", checks.first { it.name == "Plan" }.detail)
    }

    @Test
    fun proModeMapsToProDetail() {
        val checks = mapper.checks(baseInput(monetizationState = MonetizationState.Pro))

        assertEquals("Pro", checks.first { it.name == "Plan" }.detail)
    }

    private fun baseInput(
        monetizationState: MonetizationState = MonetizationState.Free
    ): DiagnosticInput =
        DiagnosticInput(
            isDeviceAdminActive = true,
            isCameraPermissionGranted = true,
            isLocationPermissionGranted = true,
            settings = SettingsState.Defaults,
            historyAvailable = true,
            exportAvailable = true,
            monetizationState = monetizationState,
            appVersion = "1.0",
            androidVersion = "Android",
            deviceModel = "Device"
        )
}
