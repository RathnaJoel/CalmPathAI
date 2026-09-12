package com.calmpath.ai.ml

import android.content.Context
import android.util.Log
import com.calmpath.ai.data.domain.PeaceScoreCalculator
import com.calmpath.ai.data.local.entities.UserPreferencesEntity
import com.calmpath.ai.data.location.LocationHelper
import com.calmpath.ai.data.model.Mood
import com.calmpath.ai.data.model.Place

/**
 * Result of scoring a sanctuary through the ML Recommendation System (CO9).
 */
data class ScoredPlaceRecommendation(
    val place: Place,
    val mlSuitabilityScore: Int,
    val peaceScore: Int,
    val matchReason: String,
    val isBestMatch: Boolean = false,
    val isMlPredicted: Boolean = true
)

/**
 * Recommendation Engine coordinating feature building, ML inference, and place ranking (CO9).
 */
class RecommendationEngine(
    private val model: PeaceRecommendationModel
) {
    private val tag = "RecommendationEngine"

    /**
     * Evaluates and ranks candidate sanctuaries for the current user and context.
     *
     * @param places List of candidate sanctuaries (from Room / GPS query).
     * @param userMood Active mood of the user (e.g. RELAX, MEDITATE, STUDY).
     * @param preferences Stored user preferences (max distance, preferred category).
     * @param previousRatings Historical ratings given by the user for places (placeId -> rating).
     * @return Sorted list of [ScoredPlaceRecommendation] with highest suitability first.
     */
    fun rankPlaces(
        places: List<Place>,
        userMood: Mood,
        preferences: UserPreferencesEntity? = null,
        previousRatings: Map<String, Double> = emptyMap()
    ): List<ScoredPlaceRecommendation> {
        if (places.isEmpty()) return emptyList()

        // Filter places strictly within India territorial boundaries
        val validPlaces = places.filter { LocationHelper.isLocationInIndia(it.latitude, it.longitude) }
        val targetPlaces = if (validPlaces.isNotEmpty()) validPlaces else places

        val scoredList = mutableListOf<ScoredPlaceRecommendation>()

        for (place in targetPlaces) {
            try {
                val previousRating = previousRatings[place.id] ?: 0.0
                val rawFeatures = PlaceFeatureBuilder.buildFeatures(
                    place = place,
                    mood = userMood,
                    userPreferences = preferences,
                    userPreviousRating = previousRating
                )

                val featureVector = FeaturePreprocessor.preprocess(rawFeatures)
                val prediction = model.predict(featureVector)

                val reason = generateMatchReason(place, userMood, prediction.suitabilityScore)

                scoredList.add(
                    ScoredPlaceRecommendation(
                        place = place,
                        mlSuitabilityScore = prediction.suitabilityScore,
                        peaceScore = place.peaceScore,
                        matchReason = reason,
                        isMlPredicted = true
                    )
                )
            } catch (e: Exception) {
                Log.w(tag, "ML inference failed for place ${place.name}: ${e.message}. Using Peace Score fallback.")
                // Graceful fallback to Peace Score
                scoredList.add(
                    ScoredPlaceRecommendation(
                        place = place,
                        mlSuitabilityScore = place.peaceScore,
                        peaceScore = place.peaceScore,
                        matchReason = "Environmental baseline (AQI ${place.aqi}, ${place.noiseDb} dB)",
                        isMlPredicted = false
                    )
                )
            }
        }

        // Sort by ML suitability score descending, then by Peace Score
        val sorted = scoredList.sortedWith(
            compareByDescending<ScoredPlaceRecommendation> { it.mlSuitabilityScore }
                .thenByDescending { it.peaceScore }
        )

        // Mark the top item as best match
        return sorted.mapIndexed { index, item ->
            if (index == 0) item.copy(isBestMatch = true) else item
        }
    }

    private fun generateMatchReason(place: Place, mood: Mood, mlScore: Int): String {
        return when {
            mlScore >= 90 -> "Optimal synergy for ${mood.title}: pristine air (AQI ${place.aqi}) & calm acoustic atmosphere (${place.noiseDb} dB)"
            mlScore >= 80 -> "Strong match for ${mood.title} within ${place.distanceKm} km with high tranquility"
            mlScore >= 70 -> "Good sanctuary option nearby with pleasant conditions"
            else -> "Moderate fit for current mood and environmental conditions"
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: RecommendationEngine? = null

        fun getInstance(context: Context): RecommendationEngine {
            return INSTANCE ?: synchronized(this) {
                val model = PeaceRecommendationModel.getInstance(context)
                RecommendationEngine(model).also { INSTANCE = it }
            }
        }
    }
}
