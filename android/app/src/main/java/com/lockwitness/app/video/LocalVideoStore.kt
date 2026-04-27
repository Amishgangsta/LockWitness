package com.lockwitness.app.video

import android.content.Context
import java.io.File

class LocalVideoStore(
    context: Context,
    directoryName: String = "incident_videos"
) {
    private val directory = File(context.filesDir, directoryName).apply {
        mkdirs()
    }

    fun createVideoFile(prefix: String = "incident"): File {
        val timestamp = System.currentTimeMillis()
        return File(directory, "${prefix}_${timestamp}.mp4")
    }
}
