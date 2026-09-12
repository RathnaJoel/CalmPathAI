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
        // Western India: Mumbai
        PlaceEntity(
            placeId = "place_1",
            name = "Hanging Gardens (Pherozeshah Mehta Gardens)",
            category = "Parks",
            address = "Ridge Road, Simla Nagar, Malabar Hill, Mumbai, Maharashtra 400006",
            latitude = 18.9567,
            longitude = 72.8052,
            description = "A terraced hilltop sanctuary atop Malabar Hill overlooking the Arabian Sea. Renowned for its ancient banyan trees, sculpted hedges, coastal sea breezes, and shaded sunset gazebos.",
            imageUrl = "https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 42,
            averageNoiseLevel = 38,
            peaceScore = 93
        ),
        PlaceEntity(
            placeId = "place_2",
            name = "Marine Drive Waterfront Promenade",
            category = "Lakes",
            address = "Netaji Subhash Chandra Bose Road, Marine Drive, Mumbai, Maharashtra 400020",
            latitude = 18.9430,
            longitude = 72.8230,
            description = "A sweeping 3.6 km crescent waterfront promenade along Back Bay. Natural rhythmic ocean swells and cool maritime air provide therapeutic white noise against urban commotion.",
            imageUrl = "https://images.unsplash.com/photo-1439853941329-a99ce0435c81?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 48,
            averageNoiseLevel = 44,
            peaceScore = 89
        ),
        PlaceEntity(
            placeId = "place_3",
            name = "The Asiatic Society Library Cloister",
            category = "Libraries",
            address = "Town Hall, Shahid Bhagat Singh Road, Fort, Mumbai, Maharashtra 400023",
            latitude = 18.9322,
            longitude = 72.8358,
            description = "A majestic neoclassical monument featuring double-height teakwood reading halls, vintage cork-padded floors, and strict silence covenants for deep scholarly absorption.",
            imageUrl = "https://images.unsplash.com/photo-1521587760476-6c12a4b040da?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 38,
            averageNoiseLevel = 28,
            peaceScore = 95
        ),
        PlaceEntity(
            placeId = "place_4",
            name = "Prithvi Courtyard & Tea Atelier",
            category = "Cafes",
            address = "20 Janki Kutir, Juhu Church Road, Juhu, Mumbai, Maharashtra 400049",
            latitude = 19.1065,
            longitude = 72.8258,
            description = "An iconic bohemian open-air courtyard sheltered by towering bamboo trees and Irish moss lanterns. Renowned for herbal infusions, soft acoustic warmth, and creative tranquility.",
            imageUrl = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 45,
            averageNoiseLevel = 42,
            peaceScore = 88
        ),
        PlaceEntity(
            placeId = "place_5",
            name = "Sanjay Gandhi National Park Kanheri Trail",
            category = "Fitness",
            address = "Western Express Highway, Borivali East, Mumbai, Maharashtra 400066",
            latitude = 19.2288,
            longitude = 72.9106,
            description = "A pristine 103 sq km forested wilderness within city limits. Shaded earthen jogging trails, dense sal canopies, and historic basalt caves offer record oxygen purity.",
            imageUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 26,
            averageNoiseLevel = 32,
            peaceScore = 96
        ),
        PlaceEntity(
            placeId = "place_6",
            name = "Global Vipassana Pagoda Peace Dome",
            category = "Meditation",
            address = "Next to EsselWorld, Gorai Creek, Borivali West, Mumbai, Maharashtra 400091",
            latitude = 19.2282,
            longitude = 72.8061,
            description = "The world's largest stone dome monument designed without supporting pillars. An acoustic marvel offering complete sensory detachment, pristine sea winds, and deep meditation.",
            imageUrl = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 24,
            averageNoiseLevel = 22,
            peaceScore = 98
        ),

        // Northern India: Delhi NCR
        PlaceEntity(
            placeId = "place_7",
            name = "Lodhi Garden Heritage Arboretum",
            category = "Parks",
            address = "Lodhi Road, Lodhi Estate, New Delhi, Delhi 110003",
            latitude = 28.5933,
            longitude = 77.2197,
            description = "A 90-acre heritage sanctuary combining 15th-century Mughal architectural tombs with landscaped bonsai gardens, lotus ponds, and century-old neem trees.",
            imageUrl = "https://images.unsplash.com/photo-1519331379826-f10be5486c6f?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 46,
            averageNoiseLevel = 35,
            peaceScore = 92
        ),
        PlaceEntity(
            placeId = "place_8",
            name = "Sunder Nursery Mughal Amphitheatre",
            category = "Parks",
            address = "Bharat Scouts and Guides Marg, Nizamuddin, New Delhi, Delhi 110013",
            latitude = 28.5912,
            longitude = 77.2435,
            description = "Delhi's first heritage botanical park spanning 90 acres with micro-climate water channels, 300+ tree species, and serene marble water fountains.",
            imageUrl = "https://images.unsplash.com/photo-1473448912268-2022ce9509d8?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 44,
            averageNoiseLevel = 33,
            peaceScore = 94
        ),
        PlaceEntity(
            placeId = "place_9",
            name = "Lotus Temple Reflection Sanctuary",
            category = "Meditation",
            address = "Lotus Temple Road, Bahapur, Kalkaji, New Delhi, Delhi 110019",
            latitude = 28.5535,
            longitude = 77.2588,
            description = "An architectural wonder of pure white Greek marble petals surrounded by nine tranquil ponds. A universal sanctuary for silent contemplation and inner stillness.",
            imageUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 40,
            averageNoiseLevel = 24,
            peaceScore = 97
        ),
        PlaceEntity(
            placeId = "place_10",
            name = "Delhi Public Library Whispering Hall",
            category = "Libraries",
            address = "Opposite Old Delhi Railway Station, SP Mukherjee Marg, Delhi 110006",
            latitude = 28.6580,
            longitude = 77.2280,
            description = "An extensive heritage archive with dedicated quiet study carrels, expansive research stacks, and acoustic insulation blocking old quarter street noise.",
            imageUrl = "https://images.unsplash.com/photo-1521587760476-6c12a4b040da?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 48,
            averageNoiseLevel = 29,
            peaceScore = 90
        ),

        // Southern India: Bengaluru
        PlaceEntity(
            placeId = "place_11",
            name = "Cubbon Park & Bamboo Grottos",
            category = "Parks",
            address = "Kasturba Road, Sampangi Rama Nagara, Bengaluru, Karnataka 560001",
            latitude = 12.9763,
            longitude = 77.5929,
            description = "A 300-acre green lung in the heart of Bangalore. Boasts over 6,000 botanical plants, towering silver oaks, and vehicle-free morning tranquility corridors.",
            imageUrl = "https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 32,
            averageNoiseLevel = 34,
            peaceScore = 95
        ),
        PlaceEntity(
            placeId = "place_12",
            name = "Lalbagh Botanical Lake & Glass House",
            category = "Lakes",
            address = "Mavalli, Bengaluru, Karnataka 560004",
            latitude = 12.9507,
            longitude = 77.5848,
            description = "A historic 240-acre botanical haven founded in 1760. Features a serene lotus lake, a century-old London-inspired glass pavilion, and rare tropical flora.",
            imageUrl = "https://images.unsplash.com/photo-1439853941329-a99ce0435c81?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 34,
            averageNoiseLevel = 36,
            peaceScore = 93
        ),
        PlaceEntity(
            placeId = "place_13",
            name = "State Central Library (Seshadri Iyer Cloister)",
            category = "Libraries",
            address = "Cubbon Park, Ambedkar Veedhi, Bengaluru, Karnataka 560001",
            latitude = 12.9772,
            longitude = 77.5914,
            description = "A gorgeous crimson red Tuscan-style palace hidden inside Cubbon Park with over 300,000 vintage volumes and silent sunlit reading alcoves.",
            imageUrl = "https://images.unsplash.com/photo-1521587760476-6c12a4b040da?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 30,
            averageNoiseLevel = 25,
            peaceScore = 96
        ),
        PlaceEntity(
            placeId = "place_14",
            name = "Art of Living Meditation Amphitheatre",
            category = "Meditation",
            address = "21st Km, Kanakapura Road, Udayapura, Bengaluru, Karnataka 560082",
            latitude = 12.8021,
            longitude = 77.5186,
            description = "A five-tier lotus-shaped meditation hall (Vishalakshi Mantap) set atop rolling Panchagiri hills. Known for divine acoustic chants, clean breeze, and absolute peace.",
            imageUrl = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 22,
            averageNoiseLevel = 20,
            peaceScore = 99
        ),
        PlaceEntity(
            placeId = "place_15",
            name = "Agara Lake Running & Fitness Circuit",
            category = "Fitness",
            address = "HSR Layout 1st Sector, Outer Ring Road, Bengaluru, Karnataka 560102",
            latitude = 12.9234,
            longitude = 77.6483,
            description = "A scenic 3.2 km red-earth running track looping around restored wetland waters. Equipped with outdoor calisthenics zones, shaded butterfly gardens, and fresh lake air.",
            imageUrl = "https://images.unsplash.com/photo-1473448912268-2022ce9509d8?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 36,
            averageNoiseLevel = 39,
            peaceScore = 91
        ),

        // Western India: Pune
        PlaceEntity(
            placeId = "place_16",
            name = "Osho Teerth Zen Park & Watercourse",
            category = "Parks",
            address = "17 Koregaon Park Road, Dhole Patil Rd, Pune, Maharashtra 411001",
            latitude = 18.5362,
            longitude = 73.8940,
            description = "A transformed ecological sanctuary featuring babbling cobblestone brooklets, dense bamboo groves, stone meditation platforms, and soothing wind chimes.",
            imageUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 35,
            averageNoiseLevel = 26,
            peaceScore = 97
        ),
        PlaceEntity(
            placeId = "place_17",
            name = "Pashan Lake Wetland & Bird Walk",
            category = "Lakes",
            address = "Pashan Lake Road, Pashan, Pune, Maharashtra 411021",
            latitude = 18.5385,
            longitude = 73.7844,
            description = "A tranquil artificial lake built during British era flanked by bamboo thickets and migratory waterbird sanctuaries. A serene walking trail ideal for solo contemplation.",
            imageUrl = "https://images.unsplash.com/photo-1439853941329-a99ce0435c81?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 38,
            averageNoiseLevel = 34,
            peaceScore = 92
        ),
        PlaceEntity(
            placeId = "place_18",
            name = "Vetal Tekdi Hilltop Summit Trail",
            category = "Fitness",
            address = "Vetal Hill Road, Kothrud, Pune, Maharashtra 411038",
            latitude = 18.5246,
            longitude = 73.8188,
            description = "The highest geographical summit in Pune city (2,600 ft). Offers untouched forest running trails, clean unpolluted morning air currents, and panoramic city sunrises.",
            imageUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 28,
            averageNoiseLevel = 30,
            peaceScore = 95
        ),

        // Southern & Eastern India: Chennai, Kolkata, Chandigarh, Rishikesh
        PlaceEntity(
            placeId = "place_19",
            name = "Theosophical Society Adyar Forest",
            category = "Parks",
            address = "Adyar, Chennai, Tamil Nadu 600020",
            latitude = 13.0067,
            longitude = 80.2580,
            description = "A 260-acre coastal forest sanctuary on the banks of Adyar River. Home to a 450-year-old banyan tree, quiet meditation paths, and coastal mangrove breezes.",
            imageUrl = "https://images.unsplash.com/photo-1519331379826-f10be5486c6f?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 36,
            averageNoiseLevel = 31,
            peaceScore = 94
        ),
        PlaceEntity(
            placeId = "place_20",
            name = "Rabindra Sarobar Lake & Walkway",
            category = "Lakes",
            address = "Southern Avenue, Dhakuria, Kolkata, West Bengal 700029",
            latitude = 22.5110,
            longitude = 88.3580,
            description = "A 192-acre artificial lake surrounded by broad-leaved mahogany canopies, quiet water-lily bays, and shaded stone benches for peaceful morning walks.",
            imageUrl = "https://images.unsplash.com/photo-1439853941329-a99ce0435c81?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 42,
            averageNoiseLevel = 38,
            peaceScore = 90
        ),
        PlaceEntity(
            placeId = "place_21",
            name = "Zakir Hussain Rose Garden & Lawn",
            category = "Parks",
            address = "Jan Marg, Sector 16, Chandigarh 160016",
            latitude = 30.7450,
            longitude = 76.7820,
            description = "Asia's largest rose garden spanning 30 acres with 50,000 rose bushes of 1,600 varieties, sunken lawns, and whispering medicinal shrub groves.",
            imageUrl = "https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 30,
            averageNoiseLevel = 32,
            peaceScore = 95
        ),
        PlaceEntity(
            placeId = "place_22",
            name = "Vashishta Gufa & Sacred Ganges Bank",
            category = "Meditation",
            address = "Badrinath Road, Shivpuri, Rishikesh, Uttarakhand 249192",
            latitude = 30.1158,
            longitude = 78.4230,
            description = "An ancient natural cave situated on the pristine white sand banks of the upper Ganges. Filled with natural negative ions, holy silence, and pure Himalayan mountain air.",
            imageUrl = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?auto=format&fit=crop&w=1200&q=80",
            averageAQI = 14,
            averageNoiseLevel = 18,
            peaceScore = 99
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

        // 4. Seed Places: ensure all places are authentic Indian sanctuaries
        val existingPlaces = placeDao.getAllPlaces()
        val hasForeignPlaces = existingPlaces.any { it.latitude > 38.0 || it.latitude < 6.0 || it.longitude < 68.0 || it.longitude > 98.0 }
        if (existingPlaces.isEmpty() || hasForeignPlaces || existingPlaces.size < samplePlaces.size) {
            // Purge any legacy places outside India
            existingPlaces.filter { it.latitude > 38.0 || it.latitude < 6.0 || it.longitude < 68.0 || it.longitude > 98.0 }
                .forEach { placeDao.deletePlace(it) }
            placeDao.insertAllPlaces(samplePlaces)
        }

        // 5. Seed Snapshots if empty or if foreign places purged
        if (environmentalSnapshotDao.getSnapshotCount() < samplePlaces.size * 2) {
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
