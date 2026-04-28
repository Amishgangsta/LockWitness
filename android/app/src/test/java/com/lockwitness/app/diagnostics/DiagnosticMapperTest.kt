package com.lockwitness.app.diagnostics

import com.lockwitness.app.data.SettingsState
import com.lockwitness.app.monetization.MonetizationState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
                shareChooserAvailable = null,
                monetizationState = MonetizationState.Free,
                appVersion = "1.0",
                androidVersion = "Android",
                deviceModel = "Device"
            )
        )

        assertEquals(DiagnosticResult.PASS, checks.first { it.name == "Device Admin" }.result)
        assertEquals(DiagnosticResult.PASS, checks.first { it.name == "Camera permission" }.result)
        assertEquals(DiagnosticResult.FAIL, checks.first { it.name == "Location permission" }.result)
        assertEquals(DiagnosticResult.WARNING, checks.first { it.name == "Video toggle" }.result)
        assertEquals(DiagnosticResult.FAIL, checks.first { it.name == "Export availability" }.result)
        assertEquals(DiagnosticResult.NOT_TESTED, checks.first { it.name == "Share chooser availability" }.result)
        assertEquals("Free", checks.first { it.name == "Free/Pro mode" }.detail)
    }

    @Test
    fun shareChooserUnavailableMapsToUnavailable() {
        val checks = mapper.checks(baseInput(shareChooserAvailable = false))

        assertEquals(
            DiagnosticResult.UNAVAILABLE,
            checks.first { it.name == "Share chooser availability" }.result
        )
    }

    @Test
    fun proModeMapsToProDetail() {
        val checks = mapper.checks(baseInput(monetizationState = MonetizationState.Pro))

        assertEquals("Pro", checks.first { it.name == "Free/Pro mode" }.detail)
    }

    private fun baseInput(
        shareChooserAvailable: Boolean? = true,
        monetizationState: MonetizationState = MonetizationState.Free
    ): DiagnosticInput =
        DiagnosticInput(
            isDeviceAdminActive = true,
            isCameraPermissionGranted = true,
            isLocationPermissionGranted = true,
            settings = SettingsState.Defaults,
            historyAvailable = true,
            exportAvailable = true,
            shareChooserAvailable = shareChooserAvailable,
            monetizationState = monetizationState,
            appVersion = "1.0",
            androidVersion = "Android",
            deviceModel = "Device"
        )
}
