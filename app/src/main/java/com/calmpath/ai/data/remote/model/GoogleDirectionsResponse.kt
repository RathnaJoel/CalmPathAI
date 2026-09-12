package com.calmpath.ai.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Data models for Google Directions REST API (CO6 Street Navigation).
 */
data class GoogleDirectionsResponse(
    @SerializedName("routes") val routes: List<GoogleRoute> = emptyList(),
    @SerializedName("status") val status: String = "",
    @SerializedName("error_message") val errorMessage: String? = null
)

data class GoogleRoute(
    @SerializedName("overview_polyline") val overviewPolyline: GoogleOverviewPolyline? = null,
    @SerializedName("legs") val legs: List<GoogleRouteLeg> = emptyList(),
    @SerializedName("summary") val summary: String? = null
)

data class GoogleOverviewPolyline(
    @SerializedName("points") val points: String = ""
)

data class GoogleRouteLeg(
    @SerializedName("distance") val distance: GoogleTextValue? = null,
    @SerializedName("duration") val duration: GoogleTextValue? = null,
    @SerializedName("steps") val steps: List<GoogleRouteStep> = emptyList(),
    @SerializedName("start_address") val startAddress: String? = null,
    @SerializedName("end_address") val endAddress: String? = null
)

data class GoogleTextValue(
    @SerializedName("text") val text: String = "",
    @SerializedName("value") val value: Long = 0L
)

data class GoogleRouteStep(
    @SerializedName("distance") val distance: GoogleTextValue? = null,
    @SerializedName("duration") val duration: GoogleTextValue? = null,
    @SerializedName("html_instructions") val htmlInstructions: String = "",
    @SerializedName("maneuver") val maneuver: String? = null
)
