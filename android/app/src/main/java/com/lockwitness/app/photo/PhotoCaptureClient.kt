package com.lockwitness.app.photo

import java.io.File

interface PhotoCaptureClient {
    suspend fun captureFrontPhoto(): PhotoCaptureResult
}

sealed interface PhotoCaptureResult {
    data class Success(
        val file: File,
        val sha256: String
    ) : PhotoCaptureResult

    data class Failure(
        val reason: String
    ) : PhotoCaptureResult
}
