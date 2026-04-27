package com.lockwitness.app.data.incident

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityIncidentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(incident: SecurityIncident): Long

    @Query("SELECT * FROM security_incidents ORDER BY timestamp DESC")
    fun getAllOrderedByTimestampDesc(): Flow<List<SecurityIncident>>

    @Query("SELECT * FROM security_incidents WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<SecurityIncident?>

    @Query("DELETE FROM security_incidents WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM security_incidents")
    suspend fun clearAll()

    @Query(
        """
        UPDATE security_incidents
        SET photoPath = :photoPath,
            imageSha256 = :imageSha256,
            photoStatus = :photoStatus,
            notes = :notes
        WHERE id = :id
        """
    )
    suspend fun updatePhotoResult(
        id: Long,
        photoPath: String?,
        imageSha256: String?,
        photoStatus: String,
        notes: String
    ): Int

    @Query(
        """
        UPDATE security_incidents
        SET videoPath = :videoPath,
            videoSha256 = :videoSha256,
            videoStatus = :videoStatus,
            notes = :notes
        WHERE id = :id
        """
    )
    suspend fun updateVideoResult(
        id: Long,
        videoPath: String?,
        videoSha256: String?,
        videoStatus: String,
        notes: String
    ): Int
}
