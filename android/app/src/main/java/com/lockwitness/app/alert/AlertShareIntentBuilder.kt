package com.lockwitness.app.alert

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

class AlertShareIntentBuilder(
    private val context: Context
) {
    fun buildChooserIntent(exportFile: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            exportFile
        )
        return Intent.createChooser(buildSendIntent(uri), "Send LockWitness export")
    }

    fun buildSendIntent(uri: android.net.Uri): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = ZIP_MIME_TYPE
            putExtra(Intent.EXTRA_SUBJECT, "LockWitness incident export")
            putExtra(Intent.EXTRA_TEXT, "LockWitness local incident export attached by user action.")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

    companion object {
        const val ZIP_MIME_TYPE = "application/zip"
    }
}
