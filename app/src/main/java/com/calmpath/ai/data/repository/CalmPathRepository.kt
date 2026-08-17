package com.calmpath.ai.data.repository

import com.calmpath.ai.data.auth.AuthManager
import com.calmpath.ai.data.local.CalmPathDatabase
import com.calmpath.ai.data.local.entities.FavoritePlaceEntity
import com.calmpath.ai.data.local.entities.HistoryEntity
import com.calmpath.ai.data.local.entities.UserPreferencesEntity
import com.calmpath.ai.data.model.EnvironmentalSummary
import com.calmpath.ai.data.model.HeatmapZone
import com.calmpath.ai.data.model.Mood
import com.calmpath.ai.data.model.Place
import com.calmpath.ai.data.remote.FirestoreSyncManager
import com.calmpath.ai.data.remote.SampleDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Main Repository coordinating Local Room Database (CO3) and Remote Firestore Sync (CO4).
 */
class CalmPathRepository(
    private val database: CalmPathDatabase,
    private val authManager: AuthManager,
    private val firestoreSync: FirestoreSyncManager = FirestoreSyncManager(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val favoriteDao = database.favoriteDao()
    private val historyDao = database.historyDao()
    private val preferencesDao = database.userPreferencesDao()

    init {
        // Pre-populate some initial favorites and default preferences if first run
        scope.launch {
            initDefaultPreferences()
            syncRemoteFavoritesIfLoggedIn()
        }
    }

    private suspend fun initDefaultPreferences() {
        val currentPref = preferencesDao.getPreferences()
        if (currentPref == null) {
            preferencesDao.insertOrUpdate(
                UserPreferencesEntity(
                    id = 1,
                    selectedMood = "relax",
                    preferredCategory = "All",
                    maxAqi = 60,
                    preferredNoiseLevel = 45,
                    preferredDistanceKm = 8,
                    notificationsEnabled = true,
                    themeMode = "SYSTEM"
                )
            )
            // Pre-seed one sample favorite so favorites screen is demo-ready
            val sampleFav = SampleDataSource.places.first()
            favoriteDao.insertFavorite(FavoritePlaceEntity.fromPlace(sampleFav, isSynced = false))
        }
    }

    suspend fun syncRemoteFavoritesIfLoggedIn() {
        val currentUserId = authManager.currentUser.value?.uid
        if (!currentUserId.isNullOrBlank()) {
            val remoteFavs = firestoreSync.fetchRemoteFavorites(currentUserId)
            if (remoteFavs.isNotEmpty()) {
                favoriteDao.insertAll(remoteFavs)
            }
        }
    }

    // ==========================================
    // PLACES & EXPLORE
    // ==========================================

    fun getAllPlaces(): List<Place> = SampleDataSource.places

    fun getPlaceById(id: String): Place? {
        return SampleDataSource.places.firstOrNull { it.id == id }
    }

    fun getRecommendedPlaces(mood: Mood): List<Place> {
        val all = SampleDataSource.places
        // Prioritize places matching the user's selected mood, then sort by peace score
        return all.sortedWith(
            compareByDescending<Place> { it.suitableMoods.contains(mood) }
                .thenByDescending { it.peaceScore }
        )
    }

    fun searchPlaces(
        query: String = "",
        category: String = "All",
        maxAqi: Int = 200,
        maxNoiseDb: Int = 120
    ): List<Place> {
        return SampleDataSource.places.filter { place ->
            val matchesQuery = query.isBlank() ||
                    place.name.contains(query, ignoreCase = true) ||
                    place.description.contains(query, ignoreCase = true) ||
                    place.category.contains(query, ignoreCase = true) ||
                    place.address.contains(query, ignoreCase = true)

            val matchesCategory = category.equals("All", ignoreCase = true) ||
                    place.category.equals(category, ignoreCase = true)

            val matchesAqi = place.aqi <= maxAqi
            val matchesNoise = place.noiseDb <= maxNoiseDb

            matchesQuery && matchesCategory && matchesAqi && matchesNoise
        }.sortedByDescending { it.peaceScore }
    }

    fun getHeatmapZones(): List<HeatmapZone> = SampleDataSource.heatmapZones

    fun getEnvironmentalSummary(): EnvironmentalSummary {
        return EnvironmentalSummary()
    }

    // ==========================================
    // FAVORITES (CO3 & CO4)
    // ==========================================

    val favoritesFlow: Flow<List<FavoritePlaceEntity>> = favoriteDao.getAllFavorites()

    fun isFavoriteFlow(placeId: String): Flow<Boolean> = favoriteDao.isFavoriteFlow(placeId)

    suspend fun isFavorite(placeId: String): Boolean = favoriteDao.isFavorite(placeId)

    suspend fun toggleFavorite(place: Place): Boolean {
        val isFav = favoriteDao.isFavorite(place.id)
        val userId = authManager.currentUser.value?.uid ?: "guest"

        if (isFav) {
            favoriteDao.deleteById(place.id)
            scope.launch {
                firestoreSync.deleteFavoriteFromCloud(userId, place.id)
            }
            return false
        } else {
            val entity = FavoritePlaceEntity.fromPlace(place, isSynced = false)
            favoriteDao.insertFavorite(entity)
            scope.launch {
                val synced = firestoreSync.syncFavoriteToCloud(userId, entity)
                if (synced) {
                    favoriteDao.insertFavorite(entity.copy(isSyncedWithCloud = true))
                }
            }
            return true
        }
    }

    suspend fun removeFavoriteById(placeId: String) {
        favoriteDao.deleteById(placeId)
        val userId = authManager.currentUser.value?.uid ?: "guest"
        scope.launch {
            firestoreSync.deleteFavoriteFromCloud(userId, placeId)
        }
    }

    suspend fun clearAllFavorites() {
        favoriteDao.clearAllFavorites()
    }

    // ==========================================
    // HISTORY (CO3 & CO4)
    // ==========================================

    val historyFlow: Flow<List<HistoryEntity>> = historyDao.getAllHistory()

    suspend fun recordPlaceView(place: Place) {
        val historyEntry = HistoryEntity(
            placeId = place.id,
            placeName = place.name,
            category = place.category,
            categoryIcon = place.categoryIcon,
            viewedAt = System.currentTimeMillis(),
            peaceScore = place.peaceScore,
            aqi = place.aqi,
            noiseLevel = place.noiseDb,
            distance = place.distanceKm,
            imageUrl = place.imageUrl,
            address = place.address
        )
        historyDao.insertHistory(historyEntry)

        val userId = authManager.currentUser.value?.uid ?: "guest"
        scope.launch {
            firestoreSync.syncHistoryToCloud(userId, historyEntry)
        }
    }

    suspend fun clearHistory() {
        historyDao.clearAllHistory()
    }

    // ==========================================
    // USER PREFERENCES (CO3 & CO4)
    // ==========================================

    val preferencesFlow: Flow<UserPreferencesEntity> = preferencesDao.getPreferencesFlow()
        .map { it ?: UserPreferencesEntity() }

    suspend fun getPreferences(): UserPreferencesEntity {
        return preferencesDao.getPreferences() ?: UserPreferencesEntity()
    }

    suspend fun saveMood(mood: Mood) {
        preferencesDao.updateSelectedMood(mood.id)
        syncPreferencesToCloud()
    }

    suspend fun saveThemeMode(theme: String) {
        preferencesDao.updateThemeMode(theme)
        syncPreferencesToCloud()
    }

    suspend fun saveNotifications(enabled: Boolean) {
        preferencesDao.updateNotifications(enabled)
        syncPreferencesToCloud()
    }

    suspend fun saveEnvironmentalPreferences(maxAqi: Int, noiseLevel: Int, distanceKm: Int) {
        preferencesDao.updateEnvironmentalPreferences(maxAqi, noiseLevel, distanceKm)
        syncPreferencesToCloud()
    }

    private fun syncPreferencesToCloud() {
        val userId = authManager.currentUser.value?.uid ?: return
        scope.launch {
            val pref = preferencesDao.getPreferences()
            if (pref != null) {
                firestoreSync.syncPreferencesToCloud(userId, pref)
            }
        }
    }
}
