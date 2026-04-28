package com.lockwitness.app.export

import java.io.File

data class IncidentExportResult(
    val file: File,
    val incidentCount: Int,
    val mediaFilesIncluded: Int,
    val missingMediaFiles: List<String>
)
