package com.lockwitness.app.export

import android.content.Context
import com.lockwitness.app.data.incident.SecurityIncident
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class LocalIncidentExporter(
    context: Context,
    private val formatter: IncidentExportFormatter = IncidentExportFormatter()
) {
    private val exportDirectory = File(context.filesDir, "exports").apply {
        mkdirs()
    }

    suspend fun exportIncidents(
        incidents: List<SecurityIncident>,
        filePrefix: String = "lockwitness_incidents"
    ): IncidentExportResult =
        withContext(Dispatchers.IO) {
            val safePrefix = filePrefix.replace(Regex("[^A-Za-z0-9_-]"), "_")
            val exportFile = File(exportDirectory, "${safePrefix}_${System.currentTimeMillis()}.zip")
            val mediaReferences = incidents.flatMap { formatter.mediaReferences(it) }
            val missingMedia = mediaReferences
                .mapNotNull { (_, file) ->
                    if (file.exists() && file.isFile) null else file.absolutePath
                }

            var includedMediaCount = 0
            ZipOutputStream(exportFile.outputStream().buffered()).use { zip ->
                zip.writeTextEntry("metadata.json", formatter.metadataJson(incidents, missingMedia))
                zip.writeTextEntry("incidents.csv", formatter.incidentsCsv(incidents))
                zip.writeTextEntry("hashes.txt", formatter.hashesText(incidents, missingMedia))

                mediaReferences.forEach { (entryName, file) ->
                    if (file.exists() && file.isFile) {
                        zip.putNextEntry(ZipEntry(entryName))
                        file.inputStream().use { input -> input.copyTo(zip) }
                        zip.closeEntry()
                        includedMediaCount += 1
                    }
                }
            }

            IncidentExportResult(
                file = exportFile,
                incidentCount = incidents.size,
                mediaFilesIncluded = includedMediaCount,
                missingMediaFiles = missingMedia
            )
        }

    private fun ZipOutputStream.writeTextEntry(name: String, content: String) {
        putNextEntry(ZipEntry(name))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }
}
