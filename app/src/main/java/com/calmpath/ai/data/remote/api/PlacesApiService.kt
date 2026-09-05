package com.calmpath.ai.data.remote.api

import com.calmpath.ai.data.remote.model.OverpassResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Retrofit interface for OpenStreetMap Overpass REST API service (CO5).
 * Discovers nearby parks, gardens, libraries, and nature sanctuaries within radius.
 */
interface PlacesApiService {

    @FormUrlEncoded
    @POST("api/interpreter")
    suspend fun getNearbyPeacefulPlaces(
        @Field("data") queryData: String
    ): Response<OverpassResponse>
}
