package com.calmpath.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.calmpath.ai.data.local.entities.FavoritePlaceEntity
import com.calmpath.ai.data.local.entities.FavoriteWithPlace
import kotlinx.coroutines.flow.Flow

/**
 * DAO for FavoritePlaceEntity operations.
 */
@Dao
interface FavoritePlaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoritePlaceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFavorites(favorites: List<FavoritePlaceEntity>)

    @Update
    suspend fun updateFavorite(favorite: FavoritePlaceEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoritePlaceEntity)

    @Query("DELETE FROM favorite_places WHERE userId = :userId AND placeId = :placeId")
    suspend fun deleteFavoriteByPlaceId(userId: String, placeId: String)

    @Query("DELETE FROM favorite_places WHERE userId = :userId")
    suspend fun clearFavoritesForUser(userId: String)

    @Query("SELECT * FROM favorite_places WHERE userId = :userId AND placeId = :placeId LIMIT 1")
    suspend fun getFavorite(userId: String, placeId: String): FavoritePlaceEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_places WHERE userId = :userId AND placeId = :placeId LIMIT 1)")
    fun isFavoriteFlow(userId: String, placeId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_places WHERE userId = :userId AND placeId = :placeId LIMIT 1)")
    suspend fun isFavorite(userId: String, placeId: String): Boolean

    @Transaction
    @Query("SELECT * FROM favorite_places WHERE userId = :userId ORDER BY savedAt DESC")
    fun getFavoritesWithPlaces(userId: String): Flow<List<FavoriteWithPlace>>

    @Transaction
    @Query("SELECT * FROM favorite_places ORDER BY savedAt DESC")
    fun getAllFavoritesWithPlaces(): Flow<List<FavoriteWithPlace>>

    @Query("SELECT COUNT(*) FROM favorite_places WHERE userId = :userId")
    fun getFavoriteCountFlow(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM favorite_places WHERE userId = :userId")
    suspend fun getFavoriteCount(userId: String): Int
}
