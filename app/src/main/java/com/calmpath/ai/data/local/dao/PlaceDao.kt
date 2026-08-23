package com.calmpath.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.calmpath.ai.data.local.entities.PlaceEntity
import com.calmpath.ai.data.local.entities.PlaceWithSnapshots
import kotlinx.coroutines.flow.Flow

/**
 * DAO for PlaceEntity operations.
 */
@Dao
interface PlaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: PlaceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPlaces(places: List<PlaceEntity>)

    @Update
    suspend fun updatePlace(place: PlaceEntity)

    @Delete
    suspend fun deletePlace(place: PlaceEntity)

    @Query("SELECT * FROM places ORDER BY peaceScore DESC")
    fun getAllPlacesFlow(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places ORDER BY peaceScore DESC")
    suspend fun getAllPlaces(): List<PlaceEntity>

    @Query("SELECT * FROM places WHERE placeId = :placeId LIMIT 1")
    suspend fun getPlaceById(placeId: String): PlaceEntity?

    @Query("SELECT * FROM places WHERE placeId = :placeId LIMIT 1")
    fun getPlaceByIdFlow(placeId: String): Flow<PlaceEntity?>

    @Query("SELECT * FROM places WHERE category = :category ORDER BY peaceScore DESC")
    fun getPlacesByCategory(category: String): Flow<List<PlaceEntity>>

    @Query("""
        SELECT * FROM places 
        WHERE (:category = 'All' OR category = :category)
        AND (:query = '' OR name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%')
        AND averageAQI <= :maxAqi
        AND averageNoiseLevel <= :maxNoise
        ORDER BY peaceScore DESC
    """)
    fun searchPlaces(
        query: String = "",
        category: String = "All",
        maxAqi: Int = 200,
        maxNoise: Int = 120
    ): Flow<List<PlaceEntity>>

    @Transaction
    @Query("SELECT * FROM places WHERE placeId = :placeId LIMIT 1")
    suspend fun getPlaceWithSnapshots(placeId: String): PlaceWithSnapshots?

    @Query("SELECT COUNT(*) FROM places")
    suspend fun getPlaceCount(): Int
}
