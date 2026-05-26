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
        assertEquals(DiagnosticResult.UNAVAILABLE, checks.first { it.name == "Export" }.result)
        assertEquals("Free", checks.first { it.name == "Plan" }.detail)
    }

    @Test
    fun proModeMapsToProDetail() {
        val checks = mapper.checks(baseInput(monetizationState = MonetizationState.Pro))

        assertEquals("Pro", checks.first { it.name == "Plan" }.detail)
    }

    @Test
    fun trialModeMapsToTrialDetailAndExportUnavailable() {
        val trial = MonetizationState(isPro = false, billingAvailable = true, trialDaysRemaining = 5)
        val checks = mapper.checks(baseInput(monetizationState = trial, exportAvailable = false))

        assertEquals("Trial — 5d left", checks.first { it.name == "Plan" }.detail)
        assertEquals(DiagnosticResult.UNAVAILABLE, checks.first { it.name == "Export" }.result)
    }

    private fun baseInput(
        monetizationState: MonetizationState = MonetizationState.Free,
        exportAvailable: Boolean = true
    ): DiagnosticInput =
        DiagnosticInput(
            isDeviceAdminActive = true,
            isCameraPermissionGranted = true,
            isLocationPermissionGranted = true,
            settings = SettingsState.Defaults,
            historyAvailable = true,
            exportAvailable = exportAvailable,
            monetizationState = monetizationState,
            appVersion = "1.0",
            androidVersion = "Android",
            deviceModel = "Device"
        )
}
