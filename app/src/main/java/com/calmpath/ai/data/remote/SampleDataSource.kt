package com.calmpath.ai.data.remote

import com.calmpath.ai.data.model.CalmnessLevel
import com.calmpath.ai.data.model.HeatmapZone
import com.calmpath.ai.data.model.Mood
import com.calmpath.ai.data.model.Place

/**
 * Rich realistic sample data source representing peaceful sanctuaries,
 * environmental metrics, and heatmap zones for CalmPath AI.
 */
object SampleDataSource {

    val places: List<Place> = listOf(
        Place(
            id = "place_1",
            name = "Zenith Botanical Conservatory",
            category = "Parks",
            categoryIcon = "🌿",
            latitude = 37.7694,
            longitude = -122.4662,
            distanceKm = 1.2,
            peaceScore = 94,
            aqi = 22,
            noiseDb = 32,
            temperatureC = 21,
            weatherCondition = "Gentle Sunshine & Mist",
            imageUrl = "https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?auto=format&fit=crop&w=1200&q=80",
            address = "742 Evergreen Conservatory Way, Green Hills",
            description = "A lush Victorian-style glass greenhouse surrounded by aromatic fern groves, quiet water lily ponds, and towering palm canopies. Designed specifically as an acoustic sanctuary for mental decompression.",
            recommendationReasons = listOf(
                "🌿 Whisper-quiet ambient sound (32 dB) ideal for mindful breathing",
                "✨ Exceptional air purity with AQI 22 filtered by 800+ botanical species",
                "📍 Proximity: Only 1.2 km away from your current location",
                "🧘 Highly matched to your 'Relax' and 'Meditate' intent"
            ),
            suitableMoods = listOf(Mood.RELAX, Mood.MEDITATE, Mood.FRESH_AIR),
            openHours = "6:30 AM – 8:00 PM",
            crowdLevel = "Very Low",
            greenDensityPercent = 95
        ),
        Place(
            id = "place_2",
            name = "Mirror Lake Lotus Promenade",
            category = "Lakes",
            categoryIcon = "🌊",
            latitude = 37.7720,
            longitude = -122.4510,
            distanceKm = 2.4,
            peaceScore = 91,
            aqi = 28,
            noiseDb = 36,
            temperatureC = 22,
            weatherCondition = "Cool Waterfront Breeze",
            imageUrl = "https://images.unsplash.com/photo-1439853941329-a99ce0435c81?auto=format&fit=crop&w=1200&q=80",
            address = "120 Lakefront Boardwalk, Serenity Bay",
            description = "A sweeping freshwater lake flanked by weeping willows and wooden meditation piers. Natural ripples and gentle waterfowl calls mask urban noise.",
            recommendationReasons = listOf(
                "🌊 Soothing natural water sounds provide natural white noise",
                "🍃 Clean coastal breeze keeping AQI at a pristine 28",
                "🚶 2.1 km scenic walking trail with shaded seating benches",
                "☕ Quiet tea pavilion located at the northern dock"
            ),
            suitableMoods = listOf(Mood.RELAX, Mood.FRESH_AIR, Mood.QUIET_TIME),
            openHours = "5:00 AM – 10:00 PM",
            crowdLevel = "Low",
            greenDensityPercent = 88
        ),
        Place(
            id = "place_3",
            name = "The Athenaeum Reading Cloister",
            category = "Libraries",
            categoryIcon = "📚",
            latitude = 37.7833,
            longitude = -122.4167,
            distanceKm = 3.1,
            peaceScore = 89,
            aqi = 34,
            noiseDb = 28,
            temperatureC = 23,
            weatherCondition = "Climate Controlled Indoor Sanctuary",
            imageUrl = "https://images.unsplash.com/photo-1521587760476-6c12a4b040da?auto=format&fit=crop&w=1200&q=80",
            address = "400 Heritage Library Square, Academic District",
            description = "A historic architectural masterpiece with double-height vaulted oak ceilings, acoustic cork flooring, soft ambient reading lamps, and strict whisper-only zones.",
            recommendationReasons = listOf(
                "🔇 Deep silence guarantee: Sustained noise below 30 dB",
                "📚 Ergonomic study pods equipped with warm spectrum reading lights",
                "⚡ High-speed fiber Wi-Fi with zero visual distractions",
                "☕ Quiet courtyard atrium for coffee breaks"
            ),
            suitableMoods = listOf(Mood.STUDY, Mood.QUIET_TIME),
            openHours = "8:00 AM – 11:00 PM",
            crowdLevel = "Moderate (Silent)",
            greenDensityPercent = 40
        ),
        Place(
            id = "place_4",
            name = "Komorebi Tea & Roastery Nook",
            category = "Cafes",
            categoryIcon = "☕",
            latitude = 37.7600,
            longitude = -122.4350,
            distanceKm = 1.8,
            peaceScore = 86,
            aqi = 40,
            noiseDb = 44,
            temperatureC = 24,
            weatherCondition = "Warm Filtered Sunlight",
            imageUrl = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=1200&q=80",
            address = "88 Solitude Lane, Artisan Quarter",
            description = "A minimalist Japanese-inspired tea atelier with private garden tatami cubbies, lo-fi acoustic acoustics, single-origin matcha, and artisan pourovers.",
            recommendationReasons = listOf(
                "☕ Low-tempo acoustic soundtrack calibrated at relaxing 44 dB",
                "🌿 Private micro-courtyard filled with Japanese maples and moss stones",
                "📖 Dedicated solo reflection booths with power outlets",
                "🍵 Curated organic herbal teas formulated for calmness"
            ),
            suitableMoods = listOf(Mood.QUIET_TIME, Mood.RELAX, Mood.STUDY),
            openHours = "7:30 AM – 7:00 PM",
            crowdLevel = "Low",
            greenDensityPercent = 65
        ),
        Place(
            id = "place_5",
            name = "Cloud Pine Mountain Ridge",
            category = "Fitness",
            categoryIcon = "🏃",
            latitude = 37.7550,
            longitude = -122.4500,
            distanceKm = 4.5,
            peaceScore = 92,
            aqi = 18,
            noiseDb = 35,
            temperatureC = 19,
            weatherCondition = "Crisp Mountain Air & Clear Sky",
            imageUrl = "https://images.unsplash.com/photo-1473448912268-2022ce9509d8?auto=format&fit=crop&w=1200&q=80",
            address = "High Ridge Trailhead, Pine Forest Reserve",
            description = "A 5 km pine-scented unpaved running and hiking trail that rises above the urban skyline, offering panoramic views, clean oxygen-rich air, and natural dirt terrain.",
            recommendationReasons = listOf(
                "🌲 Pristine mountain air with record-low AQI of 18",
                "🏃 Soft earthen jogging path reducing joint impact",
                "🧘 Scenic sunrise yoga deck at Mile Marker 2.4",
                "💨 Steady elevation gain ideal for aerobic conditioning"
            ),
            suitableMoods = listOf(Mood.EXERCISE, Mood.FRESH_AIR),
            openHours = "5:30 AM – 8:30 PM",
            crowdLevel = "Low",
            greenDensityPercent = 98
        ),
        Place(
            id = "place_6",
            name = "Shanti Bamboo Zen Sanctuary",
            category = "Meditation",
            categoryIcon = "🧘",
            latitude = 37.7650,
            longitude = -122.4400,
            distanceKm = 2.0,
            peaceScore = 96,
            aqi = 20,
            noiseDb = 29,
            temperatureC = 20,
            weatherCondition = "Gentle Shade & Wind Chimes",
            imageUrl = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?auto=format&fit=crop&w=1200&q=80",
            address = "15 Bamboo Forest Path, Serenity Heights",
            description = "An authentic meditation sanctuary set amidst a dense grove of 40-foot bamboo stalks. Features stone meditation rings, flowing streamlets, and gentle bamboo wind chimes.",
            recommendationReasons = listOf(
                "🧘 Calibrated for zero digital distractions and deep stillness",
                "🍃 Bamboo grove naturally oxygenates and cools the local microclimate",
                "🎐 Soothing acoustic resonance of bamboo wind chimes",
                "🪨 Heated cedar wood platforms for morning and sunset meditation"
            ),
            suitableMoods = listOf(Mood.MEDITATE, Mood.RELAX),
            openHours = "6:00 AM – 9:00 PM",
            crowdLevel = "Very Low",
            greenDensityPercent = 96
        ),
        Place(
            id = "place_7",
            name = "Whistling Cypress Arboretum",
            category = "Parks",
            categoryIcon = "🌳",
            latitude = 37.7780,
            longitude = -122.4700,
            distanceKm = 3.8,
            peaceScore = 87,
            aqi = 29,
            noiseDb = 39,
            temperatureC = 21,
            weatherCondition = "Muted Sunbeams & Forest Scent",
            imageUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=1200&q=80",
            address = "550 Evergreen Loop, Cypress Valley",
            description = "A sprawling 40-acre collection of century-old cypress, redwood, and cedar trees crisscrossed with meandering cobblestone footpaths.",
            recommendationReasons = listOf(
                "🌲 Dense canopy blocking 90% of highway audio frequencies",
                "🍃 Natural phytoncide emissions proven to reduce cortisol",
                "🌿 Abundant shaded hammock zones"
            ),
            suitableMoods = listOf(Mood.FRESH_AIR, Mood.RELAX, Mood.EXERCISE),
            openHours = "6:00 AM – Sunset",
            crowdLevel = "Low",
            greenDensityPercent = 92
        ),
        Place(
            id = "place_8",
            name = "Highland Viewpoint Solitude Pavilions",
            category = "Parks",
            categoryIcon = "🏞️",
            latitude = 37.7500,
            longitude = -122.4300,
            distanceKm = 5.1,
            peaceScore = 90,
            aqi = 25,
            noiseDb = 34,
            temperatureC = 19,
            weatherCondition = "Panoramic Sunset Horizon",
            imageUrl = "https://images.unsplash.com/photo-1519331379826-f10be5486c6f?auto=format&fit=crop&w=1200&q=80",
            address = "1 Ridge Lookout Boulevard",
            description = "Covered wooden pavilions perched along the western bluffs, offering 180-degree unobstructed sunset views and cool ocean air currents.",
            recommendationReasons = listOf(
                "🌅 Premier sunset tranquility spot away from traffic arteries",
                "🌬️ Pure maritime airflow with AQI 25",
                "☕ Quiet picnic benches with panoramic valley overlook"
            ),
            suitableMoods = listOf(Mood.QUIET_TIME, Mood.RELAX),
            openHours = "5:00 AM – 10:00 PM",
            crowdLevel = "Low",
            greenDensityPercent = 80
        )
    )

    val categories: List<String> = listOf(
        "All",
        "Parks",
        "Lakes",
        "Cafes",
        "Libraries",
        "Meditation",
        "Fitness"
    )

    val environmentalSummary = com.calmpath.ai.data.model.EnvironmentalSummary(
        aqi = 24,
        noiseDb = 34,
        peaceScore = 93,
        temperatureC = 22,
        weatherCondition = "Gentle Breeze & Mild",
        humidityPercent = 58
    )

    val heatmapZones: List<HeatmapZone> = listOf(
        HeatmapZone(
            id = "zone_botanical",
            title = "Zenith Conservatory & Gardens",
            relativeX = 0.28f,
            relativeY = 0.35f,
            radiusPx = 95f,
            calmnessLevel = CalmnessLevel.EXCELLENT,
            avgDecibels = 32,
            avgAqi = 22,
            associatedPlaceId = "place_1"
        ),
        HeatmapZone(
            id = "zone_mirror_lake",
            title = "Mirror Lake Aquatic Basin",
            relativeX = 0.65f,
            relativeY = 0.25f,
            radiusPx = 110f,
            calmnessLevel = CalmnessLevel.EXCELLENT,
            avgDecibels = 36,
            avgAqi = 28,
            associatedPlaceId = "place_2"
        ),
        HeatmapZone(
            id = "zone_academic",
            title = "Heritage Library Square",
            relativeX = 0.78f,
            relativeY = 0.58f,
            radiusPx = 80f,
            calmnessLevel = CalmnessLevel.GOOD,
            avgDecibels = 30,
            avgAqi = 34,
            associatedPlaceId = "place_3"
        ),
        HeatmapZone(
            id = "zone_bamboo",
            title = "Shanti Bamboo Sanctuary",
            relativeX = 0.42f,
            relativeY = 0.52f,
            radiusPx = 85f,
            calmnessLevel = CalmnessLevel.EXCELLENT,
            avgDecibels = 29,
            avgAqi = 20,
            associatedPlaceId = "place_6"
        ),
        HeatmapZone(
            id = "zone_downtown_transit",
            title = "Central Transit Hub (Noisy/Polluted)",
            relativeX = 0.50f,
            relativeY = 0.75f,
            radiusPx = 100f,
            calmnessLevel = CalmnessLevel.POOR,
            avgDecibels = 76,
            avgAqi = 118,
            associatedPlaceId = null
        ),
        HeatmapZone(
            id = "zone_commercial_strip",
            title = "Grand Avenue Commercial Corridor",
            relativeX = 0.25f,
            relativeY = 0.72f,
            radiusPx = 75f,
            calmnessLevel = CalmnessLevel.MODERATE_POOR,
            avgDecibels = 62,
            avgAqi = 75,
            associatedPlaceId = null
        ),
        HeatmapZone(
            id = "zone_highland",
            title = "Cloud Pine Ridge Trail",
            relativeX = 0.18f,
            relativeY = 0.18f,
            radiusPx = 90f,
            calmnessLevel = CalmnessLevel.EXCELLENT,
            avgDecibels = 35,
            avgAqi = 18,
            associatedPlaceId = "place_5"
        )
    )
}
