package com.lockwitness.app.data.incident

import kotlinx.coroutines.flow.Flow

class SecurityIncidentRepository(
    private val dao: SecurityIncidentDao
) {
    fun getAllOrderedByTimestampDesc(): Flow<List<SecurityIncident>> =
        dao.getAllOrderedByTimestampDesc()

    fun getById(id: Long): Flow<SecurityIncident?> =
        dao.getById(id)

    suspend fun insert(incident: SecurityIncident): Long =
        dao.insert(incident)

    suspend fun deleteById(id: Long): Int =
        dao.deleteById(id)

    suspend fun clearAll() =
        dao.clearAll()

    suspend fun updatePhotoResult(
        id: Long,
        photoPath: String?,
        imageSha256: String?,
        photoStatus: String,
        notes: String
    ): Int =
        dao.updatePhotoResult(
            id = id,
            photoPath = photoPath,
            imageSha256 = imageSha256,
            photoStatus = photoStatus,
            notes = notes
        )

    suspend fun updateVideoResult(
        id: Long,
        videoPath: String?,
        videoSha256: String?,
        videoStatus: String,
        notes: String
    ): Int =
        dao.updateVideoResult(
            id = id,
            videoPath = videoPath,
            videoSha256 = videoSha256,
            videoStatus = videoStatus,
            notes = notes
        )

    suspend fun updateLocationResult(
        id: Long,
        latitude: Double?,
        longitude: Double?,
        locationAccuracy: Float?,
        locationProvider: String?,
        locationStatus: String,
        notes: String
    ): Int =
        dao.updateLocationResult(
            id = id,
            latitude = latitude,
            longitude = longitude,
            locationAccuracy = locationAccuracy,
            locationProvider = locationProvider,
            locationStatus = locationStatus,
            notes = notes
        )

    suspend fun updateAlertResult(
        id: Long,
        emailStatus: String,
        shareStatus: String,
        notes: String
    ): Int =
        dao.updateAlertResult(
            id = id,
            emailStatus = emailStatus,
            shareStatus = shareStatus,
            notes = notes
        )
}
