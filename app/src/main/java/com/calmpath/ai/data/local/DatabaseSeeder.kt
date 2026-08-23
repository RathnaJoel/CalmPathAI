package com.calmpath.ai.data.local

import com.calmpath.ai.data.local.entities.AppSettingsEntity
import com.calmpath.ai.data.local.entities.EnvironmentalSnapshotEntity
import com.calmpath.ai.data.local.entities.FavoritePlaceEntity
import com.calmpath.ai.data.local.entities.MoodHistoryEntity
import com.calmpath.ai.data.local.entities.PlaceEntity
import com.calmpath.ai.data.local.entities.PlaceHistoryEntity
import com.calmpath.ai.data.local.entities.UserPreferencesEntity
import com.calmpath.ai.data.local.entities.UserProfileEntity
import java.util.UUID

/**
 * Seeder to populate the Room Database with rich, realistic sample entities.
 */
object DatabaseSeeder {

    const val DEFAULT_USER_ID = "user_default"

    val defaultUserProfile = UserProfileEntity(
        userId = DEFAULT_USER_ID,
        name = "Joel",
        email = "joel@calmpath.ai",
        profileImage = null,
        createdAt = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
        lastLogin = System.currentTimeMillis()
    )

    val defaultPreferences = UserPreferencesEntity(
        preferenceId = "pref_default",
        userId = DEFAULT_USER_ID,
        preferredMood = "Relax",
        preferredCategory = "All",
        maxDistance = 10.0,
        maxAQI = 60,
        maxNoiseLevel = 45,
        preferredTemperature = 22.0
    )

    val defaultAppSettings = AppSettingsEntity(
        settingsId = "settings_default",
        userId = DEFAULT_USER_ID,
        theme = "SYSTEM",
        notificationsEnabled = true,
        locationEnabled = true,
        distanceUnit = "km",
        temperatureUnit = "°C",
        soundUnit = "dB"
    )

    val samplePlaces = listOf(
        PlaceEntity(
            placeId = "place_1",
            name = "Zenith Botanical Conservatory",
            category = "Parks",
            address = "742 Evergreen Conservatory Way, Green Hills",
            latitude = 37.7694,
            longitude = -122.4662,
            description = "A lush Victorian-style glass greenhouse surrounded by aromatic fern groves, quiet water lily ponds, and towering palm canopies. Designed specifically as an acoustic sanctuary for mental decompression.",
            imageUrl = "https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 22,
            averageNoiseLevel = 32,
            peaceScore = 94
        ),
        PlaceEntity(
            placeId = "place_2",
            name = "Mirror Lake Lotus Promenade",
            category = "Lakes",
            address = "120 Lakefront Boardwalk, Serenity Bay",
            latitude = 37.7720,
            longitude = -122.4510,
            description = "A sweeping freshwater lake flanked by weeping willows and wooden meditation piers. Natural ripples and gentle waterfowl calls mask urban noise.",
            imageUrl = "https://images.unsplash.com/photo-1439853941329-a99ce0435c81?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 28,
            averageNoiseLevel = 36,
            peaceScore = 91
        ),
        PlaceEntity(
            placeId = "place_3",
            name = "The Athenaeum Reading Cloister",
            category = "Libraries",
            address = "400 Heritage Library Square, Academic District",
            latitude = 37.7833,
            longitude = -122.4167,
            description = "A historic architectural masterpiece with double-height vaulted oak ceilings, acoustic cork flooring, soft ambient reading lamps, and strict whisper-only zones.",
            imageUrl = "https://images.unsplash.com/photo-1521587760476-6c12a4b040da?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 34,
            averageNoiseLevel = 28,
            peaceScore = 89
        ),
        PlaceEntity(
            placeId = "place_4",
            name = "Komorebi Tea & Roastery Nook",
            category = "Cafes",
            address = "88 Sakura Lane, Arts Quarter",
            latitude = 37.7650,
            longitude = -122.4330,
            description = "A minimalist Japanese garden cafe with courtyard bamboo fountains, specialty matcha ceremonies, and zero loud espresso machinery.",
            imageUrl = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 42,
            averageNoiseLevel = 41,
            peaceScore = 86
        ),
        PlaceEntity(
            placeId = "place_5",
            name = "Shambhala Pine Meditation Ridge",
            category = "Meditation",
            address = "900 Lookout Vista Summit, Highland Reserve",
            latitude = 37.7550,
            longitude = -122.4480,
            description = "An elevated mountain plateau lined with ancient cedar and pine groves. Features 360-degree horizon vistas, stone meditation circles, and pristine air currents.",
            imageUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 15,
            averageNoiseLevel = 25,
            peaceScore = 97
        ),
        PlaceEntity(
            placeId = "place_6",
            name = "Redwood Creek Fitness Sanctuary",
            category = "Fitness",
            address = "310 Valley Stream Road, Redwood Canyon",
            latitude = 37.7490,
            longitude = -122.4600,
            description = "A soft-surface pine needle jogging and calisthenics circuit weaving along a crystal-clear spring creek beneath coastal redwoods.",
            imageUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 18,
            averageNoiseLevel = 38,
            peaceScore = 92
        ),
        PlaceEntity(
            placeId = "place_7",
            name = "Echo Hollow Pine Trail",
            category = "Parks",
            address = "55 Whispering Pines Pass, North Ridge",
            latitude = 37.7810,
            longitude = -122.4720,
            description = "A secluded 3.4 km loop shaded by evergreen canopies. Renowned for its rich oxygen concentration, negative ions from cascading brooks, and natural acoustic buffers.",
            imageUrl = "https://images.unsplash.com/photo-1473448912268-2022ce9509d8?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 19,
            averageNoiseLevel = 29,
            peaceScore = 95
        ),
        PlaceEntity(
            placeId = "place_8",
            name = "Starlight Observatory Meadow",
            category = "Meditation",
            address = "1200 Celestial Way, Astronomy Hill",
            latitude = 37.7390,
            longitude = -122.4410,
            description = "An expansive sub-alpine grass meadow free from urban light and noise pollution. Ideal for stargazing, deep meditation, and nocturnal peaceful reflection.",
            imageUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 20,
            averageNoiseLevel = 24,
            peaceScore = 96
        )
    )

    fun createSampleSnapshots(): List<EnvironmentalSnapshotEntity> {
        val now = System.currentTimeMillis()
        return samplePlaces.flatMap { place ->
            listOf(
                EnvironmentalSnapshotEntity(
                    snapshotId = UUID.randomUUID().toString(),
                    placeId = place.placeId,
                    aqi = place.averageAQI,
                    noiseLevelDb = place.averageNoiseLevel,
                    temperature = 21.5,
                    humidity = 58,
                    weatherCondition = "Clear & Gentle Breeze",
                    peaceScore = place.peaceScore,
                    recordedAt = now - 30 * 60 * 1000L
                ),
                EnvironmentalSnapshotEntity(
                    snapshotId = UUID.randomUUID().toString(),
                    placeId = place.placeId,
                    aqi = place.averageAQI + 3,
                    noiseLevelDb = place.averageNoiseLevel + 2,
                    temperature = 22.0,
                    humidity = 60,
                    weatherCondition = "Partly Cloudy",
                    peaceScore = place.peaceScore - 1,
                    recordedAt = now
                )
            )
        }
    }

    suspend fun seedDatabase(database: CalmPathDatabase) {
        val userProfileDao = database.userProfileDao()
        val userPreferencesDao = database.userPreferencesDao()
        val placeDao = database.placeDao()
        val favoritePlaceDao = database.favoritePlaceDao()
        val placeHistoryDao = database.placeHistoryDao()
        val moodHistoryDao = database.moodHistoryDao()
        val environmentalSnapshotDao = database.environmentalSnapshotDao()
        val appSettingsDao = database.appSettingsDao()

        // 1. Seed UserProfile
        userProfileDao.insertUser(defaultUserProfile)

        // 2. Seed UserPreferences
        userPreferencesDao.insertOrUpdate(defaultPreferences)

        // 3. Seed AppSettings
        appSettingsDao.insertOrUpdate(defaultAppSettings)

        // 4. Seed Places if empty
        if (placeDao.getPlaceCount() == 0) {
            placeDao.insertAllPlaces(samplePlaces)
        }

        // 5. Seed Snapshots if empty
        if (environmentalSnapshotDao.getSnapshotCount() == 0) {
            environmentalSnapshotDao.insertAllSnapshots(createSampleSnapshots())
        }

        // 6. Seed initial FavoritePlace
        if (favoritePlaceDao.getFavoriteCount(DEFAULT_USER_ID) == 0) {
            favoritePlaceDao.insertFavorite(
                FavoritePlaceEntity(
                    favoriteId = "fav_initial_1",
                    userId = DEFAULT_USER_ID,
                    placeId = "place_1",
                    savedAt = System.currentTimeMillis() - 3600 * 1000L,
                    userRating = 5,
                    personalNote = "Incredible botanical diversity and whisper-quiet reading spots."
                )
            )
        }

        // 7. Seed initial PlaceHistory
        if (placeHistoryDao.getHistoryCount(DEFAULT_USER_ID) == 0) {
            placeHistoryDao.insertHistory(
                PlaceHistoryEntity(
                    historyId = "hist_initial_1",
                    userId = DEFAULT_USER_ID,
                    placeId = "place_1",
                    viewedAt = System.currentTimeMillis() - 2 * 3600 * 1000L,
                    peaceScoreAtVisit = 94,
                    aqiAtVisit = 22,
                    noiseLevelAtVisit = 32
                )
            )
        }

        // 8. Seed initial MoodHistory
        moodHistoryDao.insertMoodHistory(
            MoodHistoryEntity(
                moodHistoryId = "mood_initial_1",
                userId = DEFAULT_USER_ID,
                mood = "Relax",
                selectedAt = System.currentTimeMillis() - 3600 * 1000L,
                recommendedPlaceId = "place_1",
                selectedPlaceId = "place_1"
            )
        )
    }
}
