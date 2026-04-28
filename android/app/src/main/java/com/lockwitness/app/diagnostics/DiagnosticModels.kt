package com.lockwitness.app.diagnostics

import com.lockwitness.app.data.SettingsState
import com.lockwitness.app.monetization.MonetizationState

enum class DiagnosticResult {
    PASS,
    FAIL,
    WARNING,
    NOT_TESTED,
    UNAVAILABLE
}

data class DiagnosticCheck(
    val name: String,
    val result: DiagnosticResult,
    val detail: String
)

data class DiagnosticInput(
    val isDeviceAdminActive: Boolean,
    val isCameraPermissionGranted: Boolean,
    val isLocationPermissionGranted: Boolean,
    val settings: SettingsState,
    val historyAvailable: Boolean,
    val exportAvailable: Boolean,
    val shareChooserAvailable: Boolean?,
    val monetizationState: MonetizationState,
    val appVersion: String,
    val androidVersion: String,
    val deviceModel: String
)

class DiagnosticMapper {
    fun checks(input: DiagnosticInput): List<DiagnosticCheck> =
        listOf(
            booleanCheck("Device Admin", input.isDeviceAdminActive, "Active", "Inactive"),
            booleanCheck("Camera permission", input.isCameraPermissionGranted, "Granted", "Denied"),
            booleanCheck("Location permission", input.isLocationPermissionGranted, "Granted", "Denied"),
            toggleCheck("Master monitoring toggle", input.settings.masterMonitoringEnabled),
            toggleCheck("Photo toggle", input.settings.photoCaptureEnabled),
            toggleCheck("Video toggle", input.settings.videoCaptureEnabled),
            toggleCheck("Location toggle", input.settings.locationCaptureEnabled),
            booleanCheck("Timeline/history availability", input.historyAvailable, "Available", "Unavailable"),
            booleanCheck("Export availability", input.exportAvailable, "Available", "Unavailable"),
            shareChooserCheck(input.shareChooserAvailable),
            DiagnosticCheck(
                name = "Free/Pro mode",
                result = DiagnosticResult.PASS,
                detail = if (input.monetizationState.isPro) "Pro" else "Free"
            ),
            DiagnosticCheck("App version", DiagnosticResult.PASS, input.appVersion),
            DiagnosticCheck("Android version", DiagnosticResult.PASS, input.androidVersion),
            DiagnosticCheck("Device model", DiagnosticResult.PASS, input.deviceModel)
        )

    private fun booleanCheck(
        name: String,
        value: Boolean,
        passDetail: String,
        failDetail: String
    ): DiagnosticCheck =
        DiagnosticCheck(
            name = name,
            result = if (value) DiagnosticResult.PASS else DiagnosticResult.FAIL,
            detail = if (value) passDetail else failDetail
        )

    private fun toggleCheck(name: String, enabled: Boolean): DiagnosticCheck =
        DiagnosticCheck(
            name = name,
            result = if (enabled) DiagnosticResult.PASS else DiagnosticResult.WARNING,
            detail = if (enabled) "Enabled" else "Disabled"
        )

    private fun shareChooserCheck(available: Boolean?): DiagnosticCheck =
        when (available) {
            true -> DiagnosticCheck("Share chooser availability", DiagnosticResult.PASS, "Available")
            false -> DiagnosticCheck("Share chooser availability", DiagnosticResult.UNAVAILABLE, "No compatible chooser found")
            null -> DiagnosticCheck("Share chooser availability", DiagnosticResult.NOT_TESTED, "Not tested")
        }
}
