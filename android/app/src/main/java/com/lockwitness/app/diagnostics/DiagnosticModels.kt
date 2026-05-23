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
    val monetizationState: MonetizationState,
    val appVersion: String,
    val androidVersion: String,
    val deviceModel: String
)

class DiagnosticMapper {
    fun checks(input: DiagnosticInput): List<DiagnosticCheck> =
        listOf(
            toggleCheck("Master monitoring", input.settings.masterMonitoringEnabled),
            toggleCheck("Photo capture", input.settings.photoCaptureEnabled),
            toggleCheck("Video capture", input.settings.videoCaptureEnabled),
            toggleCheck("Location capture", input.settings.locationCaptureEnabled),
            booleanCheck("Local timeline", input.historyAvailable, "Available", "Unavailable"),
            booleanCheck("Export", input.exportAvailable, "Available", "Unavailable"),
            DiagnosticCheck(
                name = "Plan",
                result = DiagnosticResult.PASS,
                detail = if (input.monetizationState.isPro) "Pro" else "Free"
            ),
            DiagnosticCheck("App version", DiagnosticResult.PASS, input.appVersion)
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

}
