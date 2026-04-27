package com.lockwitness.app.video

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import androidx.core.content.ContextCompat
import com.lockwitness.app.data.SettingsState
import com.lockwitness.app.photo.Sha256Hasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import kotlin.coroutines.resume

class Camera2VideoCaptureClient(
    private val context: Context,
    private val videoStore: LocalVideoStore = LocalVideoStore(context)
) : VideoCaptureClient {
    override suspend fun captureFrontVideo(durationSeconds: Int): VideoCaptureResult =
        withContext(Dispatchers.IO) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return@withContext VideoCaptureResult.Failure("Camera permission is not granted.")
            }

            val safeDurationSeconds = sanitizeDuration(durationSeconds)
            runCatching {
                withTimeout((safeDurationSeconds * 1000L) + CAPTURE_TIMEOUT_OVERHEAD_MS) {
                    capture(safeDurationSeconds)
                }
            }.getOrElse { error ->
                VideoCaptureResult.Failure(error.message ?: "Video capture failed.")
            }
        }

    private suspend fun capture(durationSeconds: Int): VideoCaptureResult =
        suspendCancellableCoroutine { continuation ->
            val manager = context.getSystemService(CameraManager::class.java)
            val cameraId = manager.cameraIdList.firstOrNull { id ->
                manager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
            }

            if (cameraId == null) {
                continuation.resume(VideoCaptureResult.Failure("No front camera found."))
                return@suspendCancellableCoroutine
            }

            val thread = HandlerThread("LockWitnessVideoCapture").apply { start() }
            val handler = Handler(thread.looper)
            var cameraDevice: CameraDevice? = null
            var captureSession: CameraCaptureSession? = null
            var recorder: MediaRecorder? = null
            var outputFile: File? = null
            var finished = false

            fun cleanup() {
                runCatching { captureSession?.close() }
                runCatching { cameraDevice?.close() }
                runCatching {
                    recorder?.reset()
                    recorder?.release()
                }
                thread.quitSafely()
            }

            continuation.invokeOnCancellation {
                cleanup()
            }

            fun finish(result: VideoCaptureResult) {
                if (!finished) {
                    finished = true
                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
                    cleanup()
                }
            }

            try {
                val characteristics = manager.getCameraCharacteristics(cameraId)
                val size = selectVideoSize(
                    characteristics
                        .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        ?.getOutputSizes(MediaRecorder::class.java)
                        ?.toList()
                        .orEmpty()
                )

                if (size == null) {
                    finish(VideoCaptureResult.Failure("No video output size available."))
                    return@suspendCancellableCoroutine
                }

                outputFile = videoStore.createVideoFile()
                recorder = createRecorder(outputFile!!, size)

                @Suppress("MissingPermission")
                manager.openCamera(
                    cameraId,
                    object : CameraDevice.StateCallback() {
                        override fun onOpened(camera: CameraDevice) {
                            cameraDevice = camera
                            @Suppress("DEPRECATION")
                            camera.createCaptureSession(
                                listOf(recorder!!.surface),
                                object : CameraCaptureSession.StateCallback() {
                                    override fun onConfigured(session: CameraCaptureSession) {
                                        captureSession = session
                                        val request = camera
                                            .createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
                                            .apply {
                                                addTarget(recorder!!.surface)
                                                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO)
                                                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                                            }
                                            .build()

                                        runCatching {
                                            session.setRepeatingRequest(request, null, handler)
                                            recorder!!.start()
                                            handler.postDelayed({
                                                stopRecording(outputFile!!, recorder!!, session, ::finish)
                                            }, durationSeconds * 1000L)
                                        }.getOrElse { error ->
                                            runCatching { outputFile?.delete() }
                                            finish(VideoCaptureResult.Failure(error.message ?: "Unable to start video capture."))
                                        }
                                    }

                                    override fun onConfigureFailed(session: CameraCaptureSession) {
                                        runCatching { outputFile?.delete() }
                                        finish(VideoCaptureResult.Failure("Camera video session configuration failed."))
                                    }
                                },
                                handler
                            )
                        }

                        override fun onDisconnected(camera: CameraDevice) {
                            runCatching { outputFile?.delete() }
                            finish(VideoCaptureResult.Failure("Camera disconnected."))
                        }

                        override fun onError(camera: CameraDevice, error: Int) {
                            runCatching { outputFile?.delete() }
                            finish(VideoCaptureResult.Failure("Camera error: $error."))
                        }
                    },
                    handler
                )
            } catch (error: Exception) {
                runCatching { outputFile?.delete() }
                finish(VideoCaptureResult.Failure(error.message ?: "Unable to start video capture."))
            }
        }

    private fun createRecorder(outputFile: File, size: Size): MediaRecorder =
        MediaRecorder().apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(outputFile.absolutePath)
            setVideoEncodingBitRate(VIDEO_BIT_RATE)
            setVideoFrameRate(VIDEO_FRAME_RATE)
            setVideoSize(size.width, size.height)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            prepare()
        }

    private fun stopRecording(
        outputFile: File,
        recorder: MediaRecorder,
        session: CameraCaptureSession,
        finish: (VideoCaptureResult) -> Unit
    ) {
        runCatching {
            session.stopRepeating()
            session.abortCaptures()
            recorder.stop()
            finish(
                VideoCaptureResult.Success(
                    file = outputFile,
                    sha256 = Sha256Hasher.hash(outputFile)
                )
            )
        }.getOrElse { error ->
            runCatching { outputFile.delete() }
            finish(VideoCaptureResult.Failure(error.message ?: "Unable to save video."))
        }
    }

    private fun selectVideoSize(sizes: List<Size>): Size? =
        sizes
            .filter { it.width <= MAX_VIDEO_WIDTH && it.height <= MAX_VIDEO_HEIGHT }
            .maxByOrNull { it.width * it.height }
            ?: sizes.maxByOrNull { it.width * it.height }

    private fun sanitizeDuration(seconds: Int): Int =
        if (seconds in SettingsState.AllowedVideoDurations) seconds else SettingsState.Defaults.videoDurationSeconds

    private companion object {
        const val CAPTURE_TIMEOUT_OVERHEAD_MS = 10_000L
        const val VIDEO_BIT_RATE = 5_000_000
        const val VIDEO_FRAME_RATE = 30
        const val MAX_VIDEO_WIDTH = 1920
        const val MAX_VIDEO_HEIGHT = 1080
    }
}
