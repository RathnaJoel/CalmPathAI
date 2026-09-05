package com.calmpath.ai.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Raw OpenStreetMap Overpass REST API response for nearby peaceful POIs (CO5).
 */
data class OverpassResponse(
    @SerializedName("elements") val elements: List<OverpassElement>?
)

data class OverpassElement(
    @SerializedName("id") val id: Long,
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lon") val lon: Double?,
    @SerializedName("center") val center: OverpassCenter?,
    @SerializedName("tags") val tags: Map<String, String>?
)

data class OverpassCenter(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double
)

/**
 * Normalized representation of a peaceful place discovered via REST API.
 */
data class DiscoveredPlace(
    val id: String,
    val name: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val distanceKm: Double = 0.0
)
