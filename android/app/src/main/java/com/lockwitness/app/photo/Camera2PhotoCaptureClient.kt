package com.lockwitness.app.photo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import kotlin.coroutines.resume

class Camera2PhotoCaptureClient(
    private val context: Context,
    private val photoStore: LocalPhotoStore = LocalPhotoStore(context)
) : PhotoCaptureClient {
    override suspend fun captureFrontPhoto(): PhotoCaptureResult =
        withContext(Dispatchers.IO) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return@withContext PhotoCaptureResult.Failure("Camera permission is not granted.")
            }

            runCatching {
                withTimeout(CAPTURE_TIMEOUT_MS) {
                    capture()
                }
            }.getOrElse { error ->
                PhotoCaptureResult.Failure(error.message ?: "Photo capture failed.")
            }
        }

    private suspend fun capture(): PhotoCaptureResult =
        suspendCancellableCoroutine { continuation ->
            val manager = context.getSystemService(CameraManager::class.java)
            val cameraId = manager.cameraIdList.firstOrNull { id ->
                manager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
            }

            if (cameraId == null) {
                continuation.resume(PhotoCaptureResult.Failure("No front camera found."))
                return@suspendCancellableCoroutine
            }

            val thread = HandlerThread("LockWitnessPhotoCapture").apply { start() }
            val handler = Handler(thread.looper)
            var cameraDevice: CameraDevice? = null
            var captureSession: CameraCaptureSession? = null
            var imageReader: ImageReader? = null
            val outputFile = photoStore.createPhotoFile()

            fun cleanup() {
                runCatching { captureSession?.close() }
                runCatching { cameraDevice?.close() }
                runCatching { imageReader?.close() }
                thread.quitSafely()
            }

            continuation.invokeOnCancellation {
                cleanup()
            }

            fun finish(result: PhotoCaptureResult) {
                if (continuation.isActive) {
                    continuation.resume(result)
                }
                cleanup()
            }

            try {
                val size = manager.getCameraCharacteristics(cameraId)
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    ?.getOutputSizes(ImageFormat.JPEG)
                    ?.maxByOrNull { it.width * it.height }

                if (size == null) {
                    finish(PhotoCaptureResult.Failure("No JPEG output size available."))
                    return@suspendCancellableCoroutine
                }

                imageReader = ImageReader.newInstance(size.width, size.height, ImageFormat.JPEG, 1).apply {
                    setOnImageAvailableListener({ reader ->
                        runCatching {
                            reader.acquireLatestImage().use { image ->
                                val buffer = image.planes[0].buffer
                                val bytes = ByteArray(buffer.remaining())
                                buffer.get(bytes)
                                outputFile.writeBytes(bytes)
                            }
                            finish(
                                PhotoCaptureResult.Success(
                                    file = outputFile,
                                    sha256 = Sha256Hasher.hash(outputFile)
                                )
                            )
                        }.getOrElse { error ->
                            runCatching { outputFile.delete() }
                            finish(PhotoCaptureResult.Failure(error.message ?: "Unable to save photo."))
                        }
                    }, handler)
                }

                @Suppress("MissingPermission")
                manager.openCamera(
                    cameraId,
                    object : CameraDevice.StateCallback() {
                        override fun onOpened(camera: CameraDevice) {
                            cameraDevice = camera
                            @Suppress("DEPRECATION")
                            camera.createCaptureSession(
                                listOf(imageReader!!.surface),
                                object : CameraCaptureSession.StateCallback() {
                                    override fun onConfigured(session: CameraCaptureSession) {
                                        captureSession = session
                                        val request = camera
                                            .createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                                            .apply {
                                                addTarget(imageReader!!.surface)
                                                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                                                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                                            }
                                            .build()
                                        session.capture(request, null, handler)
                                    }

                                    override fun onConfigureFailed(session: CameraCaptureSession) {
                                        finish(PhotoCaptureResult.Failure("Camera capture session configuration failed."))
                                    }
                                },
                                handler
                            )
                        }

                        override fun onDisconnected(camera: CameraDevice) {
                            finish(PhotoCaptureResult.Failure("Camera disconnected."))
                        }

                        override fun onError(camera: CameraDevice, error: Int) {
                            finish(PhotoCaptureResult.Failure("Camera error: $error."))
                        }
                    },
                    handler
                )
            } catch (error: Exception) {
                runCatching { outputFile.delete() }
                finish(PhotoCaptureResult.Failure(error.message ?: "Unable to start photo capture."))
            }
        }

    private companion object {
        const val CAPTURE_TIMEOUT_MS = 10_000L
    }
}
