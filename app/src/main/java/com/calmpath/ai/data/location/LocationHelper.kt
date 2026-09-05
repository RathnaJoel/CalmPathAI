package com.calmpath.ai.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

sealed interface LocationResult {
    data class Success(
        val latitude: Double,
        val longitude: Double,
        val locality: String,
        val country: String = "India"
    ) : LocationResult

    data class OutsideIndia(
        val latitude: Double,
        val longitude: Double,
        val country: String
    ) : LocationResult

    data object PermissionDenied : LocationResult

    data class Unavailable(
        val fallbackLocation: Success
    ) : LocationResult
}

/**
 * Manages Android Location Services, GPS coordinates, and India boundary verification (CO5).
 * Defaults to Mumbai, Maharashtra, India while allowing real GPS navigation when in India.
 */
class LocationHelper(private val context: Context) {
    private val tag = "LocationHelper"
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    companion object {
        const val DEFAULT_LATITUDE = 19.0760
        const val DEFAULT_LONGITUDE = 72.8777
        const val DEFAULT_LOCALITY = "Mumbai, Maharashtra"
        const val DEFAULT_COUNTRY = "India"

        // India geographical boundary bounding box (inclusive of Andaman & Nicobar, Lakshadweep, Kashmir)
        private const val INDIA_MIN_LAT = 6.5
        private const val INDIA_MAX_LAT = 37.5
        private const val INDIA_MIN_LON = 68.0
        private const val INDIA_MAX_LON = 97.5

        val defaultLocation = LocationResult.Success(
            latitude = DEFAULT_LATITUDE,
            longitude = DEFAULT_LONGITUDE,
            locality = DEFAULT_LOCALITY,
            country = DEFAULT_COUNTRY
        )

        /**
         * Checks whether geographic coordinates fall within India's territorial boundaries.
         */
        fun isLocationInIndia(lat: Double, lon: Double): Boolean {
            return lat in INDIA_MIN_LAT..INDIA_MAX_LAT && lon in INDIA_MIN_LON..INDIA_MAX_LON
        }

        /**
         * Calculates the great-circle distance between two points on the Earth using Haversine formula.
         * @return Distance in kilometers.
         */
        fun calculateDistanceKm(
            startLat: Double,
            startLon: Double,
            endLat: Double,
            endLon: Double
        ): Double {
            val earthRadiusKm = 6371.0
            val dLat = Math.toRadians(endLat - startLat)
            val dLon = Math.toRadians(endLon - startLon)

            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(startLat)) * cos(Math.toRadians(endLat)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return (earthRadiusKm * c * 10.0).toInt() / 10.0 // Round to 1 decimal
        }
    }

    fun hasLocationPermission(): Boolean {
        val finePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarsePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return finePermission || coarsePermission
    }

    suspend fun getCurrentLocation(): LocationResult = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            Log.d(tag, "Location permission not granted. Returning PermissionDenied.")
            return@withContext LocationResult.PermissionDenied
        }

        try {
            // Attempt high accuracy current location with token cancellation
            val cts = CancellationTokenSource()
            val location: Location? = try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cts.token
                ).await() ?: fusedLocationClient.lastLocation.await()
            } catch (e: SecurityException) {
                Log.e(tag, "SecurityException while accessing location: ${e.message}")
                null
            }

            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude

                val locality = resolveLocalityName(lat, lon)
                val country = resolveCountryName(lat, lon)

                val isInIndia = isLocationInIndia(lat, lon) || country.equals("India", ignoreCase = true) || country.equals("IN", ignoreCase = true)

                if (isInIndia) {
                    Log.d(tag, "Acquired live GPS location in India: $locality ($lat, $lon)")
                    LocationResult.Success(
                        latitude = lat,
                        longitude = lon,
                        locality = locality.ifBlank { "India" },
                        country = "India"
                    )
                } else {
                    Log.w(tag, "User location is outside India: ($lat, $lon) in $country")
                    LocationResult.OutsideIndia(
                        latitude = lat,
                        longitude = lon,
                        country = country.ifBlank { "Outside India" }
                    )
                }
            } else {
                Log.w(tag, "Location is null from provider. Using default Mumbai.")
                LocationResult.Unavailable(defaultLocation)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error obtaining location: ${e.message}", e)
            LocationResult.Unavailable(defaultLocation)
        }
    }

    private fun resolveLocalityName(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val locality = address.locality ?: address.subAdminArea ?: address.adminArea
                val state = address.adminArea
                if (!locality.isNullOrBlank() && !state.isNullOrBlank() && locality != state) {
                    "$locality, $state"
                } else locality ?: state ?: "India"
            } else {
                DEFAULT_LOCALITY
            }
        } catch (e: Exception) {
            Log.w(tag, "Geocoder failed to resolve locality: ${e.message}")
            DEFAULT_LOCALITY
        }
    }

    private fun resolveCountryName(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].countryName ?: addresses[0].countryCode ?: "India"
            } else {
                "India"
            }
        } catch (e: Exception) {
            "India"
        }
    }
}
