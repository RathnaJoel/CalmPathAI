package com.calmpath.ai.data.remote.api

import com.calmpath.ai.data.remote.model.OsrmRouteResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for Open Source Routing Machine (OSRM) REST API.
 * Free public routing engine serving realistic street geometry and maneuvers.
 */
interface OsrmApiService {

    @GET("route/v1/foot/{coordinates}")
    suspend fun getWalkingRoute(
        @Path("coordinates", encoded = true) coordinates: String, // "lon1,lat1;lon2,lat2"
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "geojson",
        @Query("steps") steps: Boolean = true
    ): Response<OsrmRouteResponse>
}
