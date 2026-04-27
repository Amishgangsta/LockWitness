package com.lockwitness.app.photo

import android.content.Context
import java.io.File

class LocalPhotoStore(
    context: Context,
    directoryName: String = "incident_photos"
) {
    private val directory = File(context.filesDir, directoryName).apply {
        mkdirs()
    }

    fun createPhotoFile(prefix: String = "incident"): File {
        val timestamp = System.currentTimeMillis()
        return File(directory, "${prefix}_${timestamp}.jpg")
    }
}
