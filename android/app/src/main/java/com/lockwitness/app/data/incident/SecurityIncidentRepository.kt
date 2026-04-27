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
}
