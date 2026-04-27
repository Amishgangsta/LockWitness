package com.lockwitness.app.video

import java.io.File

interface VideoCaptureClient {
    suspend fun captureFrontVideo(durationSeconds: Int): VideoCaptureResult
}

sealed interface VideoCaptureResult {
    data class Success(
        val file: File,
        val sha256: String
    ) : VideoCaptureResult

    data class Failure(
        val reason: String
    ) : VideoCaptureResult
}
