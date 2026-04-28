package com.lockwitness.app.location

interface LocationSnapshotClient {
    suspend fun captureLocationSnapshot(): LocationSnapshotResult
}

sealed interface LocationSnapshotResult {
    data class Success(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float?,
        val provider: String?
    ) : LocationSnapshotResult

    data class Unavailable(
        val reason: String
    ) : LocationSnapshotResult

    data class Failure(
        val reason: String
    ) : LocationSnapshotResult
}
