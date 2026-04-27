package com.lockwitness.app.data.incident

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_incidents")
data class SecurityIncident(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val triggerType: String,
    val failedAttemptCount: Int,
    val photoEnabled: Boolean,
    val videoEnabled: Boolean,
    val locationEnabled: Boolean,
    val emailEnabled: Boolean,
    val shareEnabled: Boolean,
    val timelineEnabled: Boolean,
    val photoPath: String?,
    val videoPath: String?,
    val latitude: Double?,
    val longitude: Double?,
    val locationAccuracy: Float?,
    val locationProvider: String?,
    val imageSha256: String?,
    val videoSha256: String?,
    val deviceModel: String,
    val androidVersion: String,
    val appVersion: String,
    val photoStatus: String,
    val videoStatus: String,
    val locationStatus: String,
    val emailStatus: String,
    val shareStatus: String,
    val notes: String
)
