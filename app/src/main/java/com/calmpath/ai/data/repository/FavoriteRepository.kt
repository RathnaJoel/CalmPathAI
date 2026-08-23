package com.calmpath.ai.data.repository

import com.calmpath.ai.data.local.dao.FavoritePlaceDao
import com.calmpath.ai.data.local.entities.FavoritePlaceEntity
import com.calmpath.ai.data.local.entities.FavoriteWithPlace
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository for FavoritePlaceEntity operations.
 */
class FavoriteRepository(
    private val favoritePlaceDao: FavoritePlaceDao
) {

    fun getFavoritesWithPlaces(userId: String): Flow<List<FavoriteWithPlace>> =
        favoritePlaceDao.getFavoritesWithPlaces(userId)

    fun isFavoriteFlow(userId: String, placeId: String): Flow<Boolean> =
        favoritePlaceDao.isFavoriteFlow(userId, placeId)

    suspend fun isFavorite(userId: String, placeId: String): Boolean =
        favoritePlaceDao.isFavorite(userId, placeId)

    suspend fun toggleFavorite(
        userId: String,
        placeId: String,
        userRating: Int = 5,
        personalNote: String = ""
    ): Boolean {
        val exists = favoritePlaceDao.isFavorite(userId, placeId)
        return if (exists) {
            favoritePlaceDao.deleteFavoriteByPlaceId(userId, placeId)
            false
        } else {
            favoritePlaceDao.insertFavorite(
                FavoritePlaceEntity(
                    favoriteId = UUID.randomUUID().toString(),
                    userId = userId,
                    placeId = placeId,
                    savedAt = System.currentTimeMillis(),
                    userRating = userRating,
                    personalNote = personalNote
                )
            )
            true
        }
    }

    suspend fun removeFavorite(userId: String, placeId: String) {
        favoritePlaceDao.deleteFavoriteByPlaceId(userId, placeId)
    }

    suspend fun clearFavorites(userId: String) {
        favoritePlaceDao.clearFavoritesForUser(userId)
    }

    fun getFavoriteCountFlow(userId: String): Flow<Int> =
        favoritePlaceDao.getFavoriteCountFlow(userId)
}
