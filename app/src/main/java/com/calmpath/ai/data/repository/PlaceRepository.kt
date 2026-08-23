package com.calmpath.ai.data.repository

import com.calmpath.ai.data.local.dao.EnvironmentalSnapshotDao
import com.calmpath.ai.data.local.dao.PlaceDao
import com.calmpath.ai.data.local.entities.EnvironmentalSnapshotEntity
import com.calmpath.ai.data.local.entities.PlaceEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for PlaceEntity and EnvironmentalSnapshotEntity operations.
 */
class PlaceRepository(
    private val placeDao: PlaceDao,
    private val environmentalSnapshotDao: EnvironmentalSnapshotDao
) {

    fun getAllPlacesFlow(): Flow<List<PlaceEntity>> = placeDao.getAllPlacesFlow()

    suspend fun getAllPlaces(): List<PlaceEntity> = placeDao.getAllPlaces()

    suspend fun getPlaceById(placeId: String): PlaceEntity? = placeDao.getPlaceById(placeId)

    fun getPlaceByIdFlow(placeId: String): Flow<PlaceEntity?> = placeDao.getPlaceByIdFlow(placeId)

    fun getPlacesByCategory(category: String): Flow<List<PlaceEntity>> = placeDao.getPlacesByCategory(category)

    fun searchPlaces(
        query: String = "",
        category: String = "All",
        maxAqi: Int = 200,
        maxNoise: Int = 120
    ): Flow<List<PlaceEntity>> = placeDao.searchPlaces(query, category, maxAqi, maxNoise)

    fun getLatestSnapshotFlow(placeId: String): Flow<EnvironmentalSnapshotEntity?> =
        environmentalSnapshotDao.getLatestSnapshotFlowForPlace(placeId)

    suspend fun getLatestSnapshot(placeId: String): EnvironmentalSnapshotEntity? =
        environmentalSnapshotDao.getLatestSnapshotForPlace(placeId)

    suspend fun insertPlace(place: PlaceEntity) = placeDao.insertPlace(place)

    suspend fun insertSnapshot(snapshot: EnvironmentalSnapshotEntity) =
        environmentalSnapshotDao.insertSnapshot(snapshot)
}
