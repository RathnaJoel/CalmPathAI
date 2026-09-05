package com.calmpath.ai.data.location

/**
 * Represents a selectable Indian city/state location for environmental telemetry and peace scoring.
 */
data class IndianLocation(
    val id: String,
    val cityName: String,
    val stateName: String,
    val latitude: Double,
    val longitude: Double,
    val region: String,
    val badgeEmoji: String = "📍"
) {
    val displayName: String
        get() = "$cityName, $stateName"
}

/**
 * Curated registry of 30 prominent Indian cities across diverse states and climatic zones.
 * Enables users to test and inspect live environmental conditions anywhere in India.
 */
object IndianLocationsRegistry {

    val defaultLocation = IndianLocation(
        id = "mumbai",
        cityName = "Mumbai",
        stateName = "Maharashtra",
        latitude = 19.0760,
        longitude = 72.8777,
        region = "Western India",
        badgeEmoji = "🌊"
    )

    val allLocations: List<IndianLocation> = listOf(
        // Western India
        defaultLocation,
        IndianLocation("pune", "Pune", "Maharashtra", 18.5204, 73.8567, "Western India", "🏛️"),
        IndianLocation("nagpur", "Nagpur", "Maharashtra", 21.1458, 79.0882, "Western India", "🍊"),
        IndianLocation("ahmedabad", "Ahmedabad", "Gujarat", 23.0225, 72.5714, "Western India", "🪁"),
        IndianLocation("surat", "Surat", "Gujarat", 21.1702, 72.8311, "Western India", "💎"),
        IndianLocation("panaji", "Panaji", "Goa", 15.4909, 73.8278, "Western India", "🏖️"),

        // Northern India
        IndianLocation("delhi", "New Delhi", "Delhi NCR", 28.6139, 77.2090, "Northern India", "🏛️"),
        IndianLocation("jaipur", "Jaipur", "Rajasthan", 26.9124, 75.7873, "Northern India", "🏰"),
        IndianLocation("udaipur", "Udaipur", "Rajasthan", 24.5854, 73.7125, "Northern India", "⛵"),
        IndianLocation("shimla", "Shimla", "Himachal Pradesh", 31.1048, 77.1734, "Northern India", "🏔️"),
        IndianLocation("manali", "Manali", "Himachal Pradesh", 32.2432, 77.1892, "Northern India", "❄️"),
        IndianLocation("dehradun", "Dehradun", "Uttarakhand", 30.3165, 78.0322, "Northern India", "🌲"),
        IndianLocation("rishikesh", "Rishikesh", "Uttarakhand", 30.0869, 78.2676, "Northern India", "🧘"),
        IndianLocation("srinagar", "Srinagar", "Jammu & Kashmir", 34.0837, 74.7973, "Northern India", "🌸"),
        IndianLocation("chandigarh", "Chandigarh", "Punjab / Haryana", 30.7333, 76.7794, "Northern India", "🌿"),
        IndianLocation("lucknow", "Lucknow", "Uttar Pradesh", 26.8467, 80.9462, "Northern India", "🪶"),
        IndianLocation("varanasi", "Varanasi", "Uttar Pradesh", 25.3176, 82.9739, "Northern India", "🪔"),

        // Southern India
        IndianLocation("bengaluru", "Bengaluru", "Karnataka", 12.9716, 77.5946, "Southern India", "🌳"),
        IndianLocation("mysuru", "Mysuru", "Karnataka", 12.2958, 76.6394, "Southern India", "👑"),
        IndianLocation("chennai", "Chennai", "Tamil Nadu", 13.0827, 80.2707, "Southern India", "🌊"),
        IndianLocation("coimbatore", "Coimbatore", "Tamil Nadu", 11.0168, 76.9558, "Southern India", "🌄"),
        IndianLocation("hyderabad", "Hyderabad", "Telangana", 17.3850, 78.4867, "Southern India", "💎"),
        IndianLocation("kochi", "Kochi", "Kerala", 9.9312, 76.2673, "Southern India", "🌴"),
        IndianLocation("thiruvananthapuram", "Thiruvananthapuram", "Kerala", 8.5241, 76.9366, "Southern India", "🥥"),

        // Eastern & North-Eastern India
        IndianLocation("kolkata", "Kolkata", "West Bengal", 22.5726, 88.3639, "Eastern India", "🚋"),
        IndianLocation("darjeeling", "Darjeeling", "West Bengal", 27.0410, 88.2663, "Eastern India", "🍵"),
        IndianLocation("bhubaneswar", "Bhubaneswar", "Odisha", 20.2961, 85.8245, "Eastern India", "🛕"),
        IndianLocation("guwahati", "Guwahati", "Assam", 26.1445, 91.7362, "North-East India", "🦏"),

        // Central India
        IndianLocation("bhopal", "Bhopal", "Madhya Pradesh", 23.2599, 77.4126, "Central India", "🏞️"),
        IndianLocation("indore", "Indore", "Madhya Pradesh", 22.7196, 75.8577, "Central India", "✨")
    )

    fun findById(id: String): IndianLocation {
        return allLocations.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: defaultLocation
    }

    fun searchLocations(query: String): List<IndianLocation> {
        if (query.isBlank()) return allLocations
        val q = query.trim().lowercase()
        return allLocations.filter {
            it.cityName.lowercase().contains(q) ||
            it.stateName.lowercase().contains(q) ||
            it.region.lowercase().contains(q)
        }
    }
}
