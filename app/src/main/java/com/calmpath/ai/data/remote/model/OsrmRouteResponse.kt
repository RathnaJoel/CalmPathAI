package com.calmpath.ai.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Data models for Open Source Routing Machine (OSRM) REST API (Fallback Street Routing).
 */
data class OsrmRouteResponse(
    @SerializedName("code") val code: String = "",
    @SerializedName("routes") val routes: List<OsrmRoute> = emptyList()
)

data class OsrmRoute(
    @SerializedName("geometry") val geometry: OsrmGeometry? = null,
    @SerializedName("legs") val legs: List<OsrmLeg> = emptyList(),
    @SerializedName("distance") val distance: Double = 0.0,
    @SerializedName("duration") val duration: Double = 0.0
)

data class OsrmGeometry(
    @SerializedName("coordinates") val coordinates: List<List<Double>> = emptyList(),
    @SerializedName("type") val type: String = "LineString"
)

data class OsrmLeg(
    @SerializedName("steps") val steps: List<OsrmStep> = emptyList(),
    @SerializedName("distance") val distance: Double = 0.0,
    @SerializedName("duration") val duration: Double = 0.0
)

data class OsrmStep(
    @SerializedName("name") val name: String = "",
    @SerializedName("distance") val distance: Double = 0.0,
    @SerializedName("duration") val duration: Double = 0.0,
    @SerializedName("maneuver") val maneuver: OsrmManeuver? = null
)

data class OsrmManeuver(
    @SerializedName("type") val type: String = "",
    @SerializedName("modifier") val modifier: String = ""
)
