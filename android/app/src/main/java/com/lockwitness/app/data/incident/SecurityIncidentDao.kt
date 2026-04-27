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
}
