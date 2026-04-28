package com.lockwitness.app.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidLocationSnapshotClient(
    private val context: Context
) : LocationSnapshotClient {
    override suspend fun captureLocationSnapshot(): LocationSnapshotResult =
        withContext(Dispatchers.IO) {
            if (!hasLocationPermission()) {
                return@withContext LocationSnapshotResult.Failure("Location permission is not granted.")
            }

            runCatching {
                val locationManager = context.getSystemService(LocationManager::class.java)
                    ?: return@withContext LocationSnapshotResult.Failure("Location service is unavailable.")

                val enabledProviders = locationManager
                    .getProviders(true)
                    .filter { provider ->
                        provider == LocationManager.GPS_PROVIDER ||
                            provider == LocationManager.NETWORK_PROVIDER ||
                            provider == LocationManager.PASSIVE_PROVIDER
                    }

                if (enabledProviders.isEmpty()) {
                    return@withContext LocationSnapshotResult.Unavailable("No enabled location provider.")
                }

                val location = enabledProviders
                    .mapNotNull { provider ->
                        runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
                    }
                    .maxByOrNull { it.time }

                if (location == null) {
                    LocationSnapshotResult.Unavailable("No last known location available.")
                } else {
                    LocationSnapshotResult.Success(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = if (location.hasAccuracy()) location.accuracy else null,
                        provider = location.provider
                    )
                }
            }.getOrElse { error ->
                LocationSnapshotResult.Failure(error.message ?: "Location snapshot failed.")
            }
        }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
}
