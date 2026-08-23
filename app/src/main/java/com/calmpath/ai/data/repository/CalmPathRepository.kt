package com.calmpath.ai.data.repository

import com.calmpath.ai.data.auth.AuthManager
import com.calmpath.ai.data.local.CalmPathDatabase
import com.calmpath.ai.data.local.DatabaseSeeder
import com.calmpath.ai.data.local.entities.AppSettingsEntity
import com.calmpath.ai.data.local.entities.EnvironmentalSnapshotEntity
import com.calmpath.ai.data.local.entities.FavoritePlaceEntity
import com.calmpath.ai.data.local.entities.FavoriteWithPlace
import com.calmpath.ai.data.local.entities.MoodHistoryEntity
import com.calmpath.ai.data.local.entities.PlaceEntity
import com.calmpath.ai.data.local.entities.PlaceHistoryEntity
import com.calmpath.ai.data.local.entities.PlaceHistoryWithPlace
import com.calmpath.ai.data.local.entities.UserPreferencesEntity
import com.calmpath.ai.data.local.entities.UserProfileEntity
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
 * Master Repository coordinating all 8 Room entities (CO3) and cloud sync (CO4).
 */
class CalmPathRepository(
    private val database: CalmPathDatabase,
    private val authManager: AuthManager,
    private val firestoreSync: FirestoreSyncManager = FirestoreSyncManager(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val placeRepository = PlaceRepository(database.placeDao(), database.environmentalSnapshotDao())
    val favoriteRepository = FavoriteRepository(database.favoritePlaceDao())
    val historyRepository = HistoryRepository(database.placeHistoryDao())
    val moodRepository = MoodRepository(database.moodHistoryDao())
    val userRepository = UserRepository(database.userProfileDao(), database.userPreferencesDao())
    val settingsRepository = SettingsRepository(database.appSettingsDao())

    init {
        scope.launch {
            // Seed 8 Room entities with initial realistic dataset
            DatabaseSeeder.seedDatabase(database)
        }
    }

    private fun getCurrentUserId(): String {
        return authManager.currentUser.value?.uid ?: DatabaseSeeder.DEFAULT_USER_ID
    }

    // ==========================================
    // PLACES & EXPLORE (CO3)
    // ==========================================

    val placesFlow: Flow<List<PlaceEntity>> = placeRepository.getAllPlacesFlow()

    suspend fun getAllPlaces(): List<PlaceEntity> = placeRepository.getAllPlaces()

    suspend fun getPlaceById(id: String): PlaceEntity? = placeRepository.getPlaceById(id)

    fun getPlaceByIdFlow(id: String): Flow<PlaceEntity?> = placeRepository.getPlaceByIdFlow(id)

    fun getPlacesByCategory(category: String): Flow<List<PlaceEntity>> =
        placeRepository.getPlacesByCategory(category)

    fun searchPlaces(
        query: String = "",
        category: String = "All",
        maxAqi: Int = 200,
        maxNoiseDb: Int = 120
    ): Flow<List<PlaceEntity>> = placeRepository.searchPlaces(query, category, maxAqi, maxNoiseDb)

    fun getLatestSnapshotFlow(placeId: String): Flow<EnvironmentalSnapshotEntity?> =
        placeRepository.getLatestSnapshotFlow(placeId)

    // ==========================================
    // FAVORITES (CO3: FavoritePlaceEntity)
    // ==========================================

    val favoritesWithPlacesFlow: Flow<List<FavoriteWithPlace>> =
        favoriteRepository.getFavoritesWithPlaces(DatabaseSeeder.DEFAULT_USER_ID)

    fun getFavoritesFlowForUser(userId: String = getCurrentUserId()): Flow<List<FavoriteWithPlace>> =
        favoriteRepository.getFavoritesWithPlaces(userId)

    fun isFavoriteFlow(placeId: String, userId: String = getCurrentUserId()): Flow<Boolean> =
        favoriteRepository.isFavoriteFlow(userId, placeId)

    suspend fun isFavorite(placeId: String, userId: String = getCurrentUserId()): Boolean =
        favoriteRepository.isFavorite(userId, placeId)

    suspend fun toggleFavorite(
        placeId: String,
        userRating: Int = 5,
        personalNote: String = "",
        userId: String = getCurrentUserId()
    ): Boolean {
        return favoriteRepository.toggleFavorite(userId, placeId, userRating, personalNote)
    }

    suspend fun removeFavorite(placeId: String, userId: String = getCurrentUserId()) {
        favoriteRepository.removeFavorite(userId, placeId)
    }

    suspend fun clearAllFavorites(userId: String = getCurrentUserId()) {
        favoriteRepository.clearFavorites(userId)
    }

    // ==========================================
    // HISTORY (CO3: PlaceHistoryEntity)
    // ==========================================

    val historyWithPlacesFlow: Flow<List<PlaceHistoryWithPlace>> =
        historyRepository.getHistoryWithPlaces(DatabaseSeeder.DEFAULT_USER_ID)

    fun getHistoryFlowForUser(userId: String = getCurrentUserId()): Flow<List<PlaceHistoryWithPlace>> =
        historyRepository.getHistoryWithPlaces(userId)

    suspend fun recordPlaceView(
        placeId: String,
        peaceScore: Int,
        aqi: Int,
        noiseLevel: Int,
        userId: String = getCurrentUserId()
    ) {
        historyRepository.recordPlaceView(userId, placeId, peaceScore, aqi, noiseLevel)
    }

    suspend fun clearHistory(userId: String = getCurrentUserId()) {
        historyRepository.clearHistory(userId)
    }

    // ==========================================
    // MOOD HISTORY (CO3: MoodHistoryEntity)
    // ==========================================

    val latestMoodFlow: Flow<MoodHistoryEntity?> =
        moodRepository.getLatestMoodFlow(DatabaseSeeder.DEFAULT_USER_ID)

    fun getMoodHistoryFlow(userId: String = getCurrentUserId()): Flow<List<MoodHistoryEntity>> =
        moodRepository.getMoodHistoryFlow(userId)

    suspend fun recordMood(
        mood: String,
        recommendedPlaceId: String? = null,
        selectedPlaceId: String? = null,
        userId: String = getCurrentUserId()
    ) {
        moodRepository.recordMood(userId, mood, recommendedPlaceId, selectedPlaceId)
        userRepository.updatePreferredMood(userId, mood)
    }

    suspend fun saveMood(mood: Mood) {
        recordMood(mood.title)
    }

    // ==========================================
    // USER PROFILE & PREFERENCES (CO3)
    // ==========================================

    val userProfileFlow: Flow<UserProfileEntity?> =
        userRepository.getUserFlow(DatabaseSeeder.DEFAULT_USER_ID)

    val preferencesFlow: Flow<UserPreferencesEntity> =
        userRepository.getPreferencesFlow(DatabaseSeeder.DEFAULT_USER_ID)
            .map { it ?: DatabaseSeeder.defaultPreferences }

    suspend fun getUserProfile(userId: String = getCurrentUserId()): UserProfileEntity? =
        userRepository.getUser(userId)

    suspend fun getUserPreferences(userId: String = getCurrentUserId()): UserPreferencesEntity =
        userRepository.getPreferences(userId) ?: DatabaseSeeder.defaultPreferences

    suspend fun saveEnvironmentalPreferences(
        maxAqi: Int,
        noiseLevel: Int,
        distanceKm: Double,
        userId: String = getCurrentUserId()
    ) {
        userRepository.updateEnvironmentalTolerances(userId, maxAqi, noiseLevel, distanceKm)
    }

    // ==========================================
    // APP SETTINGS (CO3: AppSettingsEntity)
    // ==========================================

    val settingsFlow: Flow<AppSettingsEntity> =
        settingsRepository.getSettingsFlow(DatabaseSeeder.DEFAULT_USER_ID)
            .map { it ?: DatabaseSeeder.defaultAppSettings }

    suspend fun getAppSettings(userId: String = getCurrentUserId()): AppSettingsEntity =
        settingsRepository.getSettings(userId) ?: DatabaseSeeder.defaultAppSettings

    suspend fun saveTheme(theme: String, userId: String = getCurrentUserId()) {
        settingsRepository.updateTheme(userId, theme)
    }

    suspend fun saveNotifications(enabled: Boolean, userId: String = getCurrentUserId()) {
        settingsRepository.updateNotifications(userId, enabled)
    }

    suspend fun saveUnits(distanceUnit: String, tempUnit: String, userId: String = getCurrentUserId()) {
        settingsRepository.updateUnits(userId, distanceUnit, tempUnit)
    }

    // ==========================================
    // DASHBOARD & HEATMAP SUMMARY
    // ==========================================

    fun getEnvironmentalSummary(): EnvironmentalSummary = SampleDataSource.environmentalSummary

    fun getHeatmapZones(): List<HeatmapZone> = SampleDataSource.heatmapZones
}
