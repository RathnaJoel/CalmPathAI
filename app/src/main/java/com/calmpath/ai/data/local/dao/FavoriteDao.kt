package com.calmpath.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calmpath.ai.data.local.entities.FavoritePlaceEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Favorite Places (CO3).
 */
@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorite_places ORDER BY savedAtTimestamp DESC")
    fun getAllFavorites(): Flow<List<FavoritePlaceEntity>>

    @Query("SELECT * FROM favorite_places WHERE id = :id LIMIT 1")
    suspend fun getFavoriteById(id: String): FavoritePlaceEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_places WHERE id = :id)")
    fun isFavoriteFlow(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_places WHERE id = :id)")
    suspend fun isFavorite(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(place: FavoritePlaceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(places: List<FavoritePlaceEntity>)

    @Delete
    suspend fun deleteFavorite(place: FavoritePlaceEntity)

    @Query("DELETE FROM favorite_places WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM favorite_places")
    suspend fun clearAllFavorites()

    @Query("SELECT COUNT(*) FROM favorite_places")
    fun getFavoritesCount(): Flow<Int>
}
