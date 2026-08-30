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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Master Repository coordinating all 8 Room entities (CO3) and Cloud Firestore synchronization (CO4).
 */
@OptIn(ExperimentalCoroutinesApi::class)
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
            
            // Sync remote favorites from cloud for the current user
            val currentId = getCurrentUserId()
            val remoteFavs = firestoreSync.fetchRemoteFavorites(currentId)
            remoteFavs.forEach { fav ->
                database.favoritePlaceDao().insertFavorite(fav)
            }
        }
    }

    fun getCurrentUserId(): String {
        return authManager.currentUser.value?.uid ?: DatabaseSeeder.DEFAULT_USER_ID
    }

    suspend fun ensureUserExists(userId: String) {
        val existing = database.userProfileDao().getUserById(userId)
        if (existing == null) {
            val authUser = authManager.currentUser.value
            val name = if (authUser?.uid == userId) authUser.displayName else "Calm Traveler"
            val email = if (authUser?.uid == userId) authUser.email else "user@calmpath.ai"
            val newProfile = UserProfileEntity(
                userId = userId,
                name = name,
                email = email,
                profileImage = authUser?.photoUrl,
                createdAt = System.currentTimeMillis(),
                lastLogin = System.currentTimeMillis()
            )
            database.userProfileDao().insertUser(newProfile)
            
            val newPreferences = UserPreferencesEntity(
                preferenceId = "pref_$userId",
                userId = userId,
                preferredMood = "Relax",
                preferredCategory = "All",
                maxDistance = 10.0,
                maxAQI = 60,
                maxNoiseLevel = 45,
                preferredTemperature = 22.0
            )
            database.userPreferencesDao().insertOrUpdate(newPreferences)

            val newSettings = AppSettingsEntity(
                settingsId = "settings_$userId",
                userId = userId,
                theme = "SYSTEM",
                notificationsEnabled = true,
                locationEnabled = true,
                distanceUnit = "km",
                temperatureUnit = "°C",
                soundUnit = "dB"
            )
            database.appSettingsDao().insertOrUpdate(newSettings)

            // Cloud sync profile and preferences
            scope.launch {
                firestoreSync.syncUserProfileToCloud(userId, newProfile)
                firestoreSync.syncPreferencesToCloud(userId, newPreferences)
            }
        }
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
    // FAVORITES (CO3: FavoritePlaceEntity & CO4: Firestore Sync)
    // ==========================================

    val favoritesWithPlacesFlow: Flow<List<FavoriteWithPlace>> =
        authManager.currentUser.flatMapLatest { user ->
            val uid = user?.uid ?: DatabaseSeeder.DEFAULT_USER_ID
            favoriteRepository.getFavoritesWithPlaces(uid)
        }

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
        ensureUserExists(userId)
        val nowFav = favoriteRepository.toggleFavorite(userId, placeId, userRating, personalNote)
        
        scope.launch {
            if (nowFav) {
                val savedEntity = database.favoritePlaceDao().getFavorite(userId, placeId)
                if (savedEntity != null) {
                    firestoreSync.syncFavoriteToCloud(userId, savedEntity)
                }
            } else {
                firestoreSync.deleteFavoriteFromCloud(userId, placeId)
            }
        }
        return nowFav
    }

    suspend fun removeFavorite(placeId: String, userId: String = getCurrentUserId()) {
        ensureUserExists(userId)
        favoriteRepository.removeFavorite(userId, placeId)
        scope.launch {
            firestoreSync.deleteFavoriteFromCloud(userId, placeId)
        }
    }

    suspend fun clearAllFavorites(userId: String = getCurrentUserId()) {
        ensureUserExists(userId)
        favoriteRepository.clearFavorites(userId)
    }

    // ==========================================
    // HISTORY (CO3: PlaceHistoryEntity & CO4: Firestore Sync)
    // ==========================================

    val historyWithPlacesFlow: Flow<List<PlaceHistoryWithPlace>> =
        authManager.currentUser.flatMapLatest { user ->
            val uid = user?.uid ?: DatabaseSeeder.DEFAULT_USER_ID
            historyRepository.getHistoryWithPlaces(uid)
        }

    fun getHistoryFlowForUser(userId: String = getCurrentUserId()): Flow<List<PlaceHistoryWithPlace>> =
        historyRepository.getHistoryWithPlaces(userId)

    suspend fun recordPlaceView(
        placeId: String,
        peaceScore: Int,
        aqi: Int,
        noiseLevel: Int,
        userId: String = getCurrentUserId()
    ) {
        ensureUserExists(userId)
        val histId = UUID.randomUUID().toString()
        val historyEntity = PlaceHistoryEntity(
            historyId = histId,
            userId = userId,
            placeId = placeId,
            viewedAt = System.currentTimeMillis(),
            peaceScoreAtVisit = peaceScore,
            aqiAtVisit = aqi,
            noiseLevelAtVisit = noiseLevel
        )
        database.placeHistoryDao().insertHistory(historyEntity)
        scope.launch {
            firestoreSync.syncHistoryToCloud(userId, historyEntity)
        }
    }

    suspend fun clearHistory(userId: String = getCurrentUserId()) {
        ensureUserExists(userId)
        historyRepository.clearHistory(userId)
    }

    // ==========================================
    // MOOD HISTORY (CO3: MoodHistoryEntity & CO4: Firestore Sync)
    // ==========================================

    val latestMoodFlow: Flow<MoodHistoryEntity?> =
        authManager.currentUser.flatMapLatest { user ->
            val uid = user?.uid ?: DatabaseSeeder.DEFAULT_USER_ID
            moodRepository.getLatestMoodFlow(uid)
        }

    fun getMoodHistoryFlow(userId: String = getCurrentUserId()): Flow<List<MoodHistoryEntity>> =
        moodRepository.getMoodHistoryFlow(userId)

    suspend fun recordMood(
        mood: String,
        recommendedPlaceId: String? = null,
        selectedPlaceId: String? = null,
        userId: String = getCurrentUserId()
    ) {
        ensureUserExists(userId)
        val validRecommended = if (recommendedPlaceId != null && database.placeDao().getPlaceById(recommendedPlaceId) != null) recommendedPlaceId else null
        val validSelected = if (selectedPlaceId != null && database.placeDao().getPlaceById(selectedPlaceId) != null) selectedPlaceId else null

        val moodId = UUID.randomUUID().toString()
        val moodEntity = MoodHistoryEntity(
            moodHistoryId = moodId,
            userId = userId,
            mood = mood,
            selectedAt = System.currentTimeMillis(),
            recommendedPlaceId = validRecommended,
            selectedPlaceId = validSelected
        )
        database.moodHistoryDao().insertMoodHistory(moodEntity)
        userRepository.updatePreferredMood(userId, mood)

        scope.launch {
            firestoreSync.syncMoodToCloud(userId, moodEntity)
        }
    }

    suspend fun saveMood(mood: Mood) {
        recordMood(mood.title)
    }

    // ==========================================
    // USER PROFILE & PREFERENCES (CO3 & CO4)
    // ==========================================

    val userProfileFlow: Flow<UserProfileEntity?> =
        authManager.currentUser.flatMapLatest { user ->
            val uid = user?.uid ?: DatabaseSeeder.DEFAULT_USER_ID
            userRepository.getUserFlow(uid)
        }

    val preferencesFlow: Flow<UserPreferencesEntity> =
        authManager.currentUser.flatMapLatest { user ->
            val uid = user?.uid ?: DatabaseSeeder.DEFAULT_USER_ID
            userRepository.getPreferencesFlow(uid).map { it ?: DatabaseSeeder.defaultPreferences }
        }

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
        ensureUserExists(userId)
        userRepository.updateEnvironmentalTolerances(userId, maxAqi, noiseLevel, distanceKm)
        
        scope.launch {
            val updatedPref = userRepository.getPreferences(userId)
            if (updatedPref != null) {
                firestoreSync.syncPreferencesToCloud(userId, updatedPref)
            }
        }
    }

    // ==========================================
    // APP SETTINGS (CO3: AppSettingsEntity)
    // ==========================================

    val settingsFlow: Flow<AppSettingsEntity> =
        authManager.currentUser.flatMapLatest { user ->
            val uid = user?.uid ?: DatabaseSeeder.DEFAULT_USER_ID
            settingsRepository.getSettingsFlow(uid).map { it ?: DatabaseSeeder.defaultAppSettings }
        }

    suspend fun getAppSettings(userId: String = getCurrentUserId()): AppSettingsEntity =
        settingsRepository.getSettings(userId) ?: DatabaseSeeder.defaultAppSettings

    suspend fun saveTheme(theme: String, userId: String = getCurrentUserId()) {
        ensureUserExists(userId)
        settingsRepository.updateTheme(userId, theme)
    }

    suspend fun saveNotifications(enabled: Boolean, userId: String = getCurrentUserId()) {
        ensureUserExists(userId)
        settingsRepository.updateNotifications(userId, enabled)
    }

    suspend fun saveUnits(distanceUnit: String, tempUnit: String, userId: String = getCurrentUserId()) {
        ensureUserExists(userId)
        settingsRepository.updateUnits(userId, distanceUnit, tempUnit)
    }

    // ==========================================
    // DASHBOARD & HEATMAP SUMMARY
    // ==========================================

    fun getEnvironmentalSummary(): EnvironmentalSummary = SampleDataSource.environmentalSummary

    fun getHeatmapZones(): List<HeatmapZone> = SampleDataSource.heatmapZones
}
