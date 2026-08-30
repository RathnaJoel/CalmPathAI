package com.calmpath.ai.data.remote

import android.util.Log
import com.calmpath.ai.data.local.entities.FavoritePlaceEntity
import com.calmpath.ai.data.local.entities.MoodHistoryEntity
import com.calmpath.ai.data.local.entities.PlaceHistoryEntity
import com.calmpath.ai.data.local.entities.UserPreferencesEntity
import com.calmpath.ai.data.local.entities.UserProfileEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Manages Cloud Synchronization with Firebase Firestore (CO4).
 * Handles bidirectional synchronization between local Room DB and Cloud Firestore.
 */
class FirestoreSyncManager {
    private val tag = "FirestoreSyncManager"

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(tag, "Firestore not available or offline: ${e.message}")
            null
        }
    }

    /**
     * Synchronizes user profile to Firestore `/users/{userId}/profile/info`.
     */
    suspend fun syncUserProfileToCloud(userId: String, profile: UserProfileEntity): Boolean {
        if (userId.isBlank()) return false
        return try {
            val db = firestore ?: return false
            val profileMap = hashMapOf(
                "userId" to profile.userId,
                "name" to profile.name,
                "email" to profile.email,
                "profileImage" to (profile.profileImage ?: ""),
                "createdAt" to profile.createdAt,
                "lastLogin" to profile.lastLogin,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("users")
                .document(userId)
                .collection("profile")
                .document("info")
                .set(profileMap, SetOptions.merge())
                .await()
            Log.d(tag, "Successfully synced user profile to Firestore")
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to sync user profile to Firestore: ${e.message}", e)
            false
        }
    }

    /**
     * Synchronizes a single favorite place to Firestore under `/users/{userId}/favorites/{placeId}`.
     */
    suspend fun syncFavoriteToCloud(userId: String, favorite: FavoritePlaceEntity): Boolean {
        if (userId.isBlank()) return false
        return try {
            val db = firestore ?: return false
            val favMap = hashMapOf(
                "favoriteId" to favorite.favoriteId,
                "userId" to favorite.userId,
                "placeId" to favorite.placeId,
                "savedAt" to favorite.savedAt,
                "userRating" to favorite.userRating,
                "personalNote" to favorite.personalNote,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(favorite.placeId)
                .set(favMap, SetOptions.merge())
                .await()
            Log.d(tag, "Successfully synced favorite '${favorite.placeId}' to Firestore")
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to sync favorite to Firestore: ${e.message}", e)
            false
        }
    }

    /**
     * Removes a favorite place from Firestore when deleted locally.
     */
    suspend fun deleteFavoriteFromCloud(userId: String, placeId: String): Boolean {
        if (userId.isBlank()) return false
        return try {
            val db = firestore ?: return false
            db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(placeId)
                .delete()
                .await()
            Log.d(tag, "Deleted favorite '$placeId' from Firestore")
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to delete favorite from Firestore: ${e.message}", e)
            false
        }
    }

    /**
     * Synchronizes user preferences to Firestore `/users/{userId}/preferences/user_settings`.
     */
    suspend fun syncPreferencesToCloud(userId: String, preferences: UserPreferencesEntity): Boolean {
        if (userId.isBlank()) return false
        return try {
            val db = firestore ?: return false
            val prefMap = hashMapOf(
                "preferenceId" to preferences.preferenceId,
                "userId" to preferences.userId,
                "preferredMood" to preferences.preferredMood,
                "preferredCategory" to preferences.preferredCategory,
                "maxDistance" to preferences.maxDistance,
                "maxAQI" to preferences.maxAQI,
                "maxNoiseLevel" to preferences.maxNoiseLevel,
                "preferredTemperature" to preferences.preferredTemperature,
                "lastSyncedTimestamp" to System.currentTimeMillis()
            )
            db.collection("users")
                .document(userId)
                .collection("preferences")
                .document("user_settings")
                .set(prefMap, SetOptions.merge())
                .await()
            Log.d(tag, "Successfully synced preferences to Firestore")
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to sync preferences to Firestore: ${e.message}", e)
            false
        }
    }

    /**
     * Synchronizes a history view entry to Firestore `/users/{userId}/history/{historyId}`.
     */
    suspend fun syncHistoryToCloud(userId: String, history: PlaceHistoryEntity): Boolean {
        if (userId.isBlank()) return false
        return try {
            val db = firestore ?: return false
            val histMap = hashMapOf(
                "historyId" to history.historyId,
                "userId" to history.userId,
                "placeId" to history.placeId,
                "viewedAt" to history.viewedAt,
                "peaceScoreAtVisit" to history.peaceScoreAtVisit,
                "aqiAtVisit" to history.aqiAtVisit,
                "noiseLevelAtVisit" to history.noiseLevelAtVisit
            )
            db.collection("users")
                .document(userId)
                .collection("history")
                .document(history.historyId)
                .set(histMap, SetOptions.merge())
                .await()
            Log.d(tag, "Synced history entry to Firestore")
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to sync history to Firestore: ${e.message}", e)
            false
        }
    }

    /**
     * Synchronizes a mood log entry to Firestore `/users/{userId}/moods/{moodHistoryId}`.
     */
    suspend fun syncMoodToCloud(userId: String, mood: MoodHistoryEntity): Boolean {
        if (userId.isBlank()) return false
        return try {
            val db = firestore ?: return false
            val moodMap = hashMapOf(
                "moodHistoryId" to mood.moodHistoryId,
                "userId" to mood.userId,
                "mood" to mood.mood,
                "selectedAt" to mood.selectedAt,
                "recommendedPlaceId" to (mood.recommendedPlaceId ?: ""),
                "selectedPlaceId" to (mood.selectedPlaceId ?: "")
            )
            db.collection("users")
                .document(userId)
                .collection("moods")
                .document(mood.moodHistoryId)
                .set(moodMap, SetOptions.merge())
                .await()
            Log.d(tag, "Synced mood log to Firestore")
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to sync mood log to Firestore: ${e.message}", e)
            false
        }
    }

    /**
     * Downloads remote favorites from Firestore to synchronize down to Room.
     */
    suspend fun fetchRemoteFavorites(userId: String): List<FavoritePlaceEntity> {
        if (userId.isBlank()) return emptyList()
        return try {
            val db = firestore ?: return emptyList()
            val querySnapshot = db.collection("users")
                .document(userId)
                .collection("favorites")
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                val favId = doc.getString("favoriteId") ?: doc.id
                val placeId = doc.getString("placeId") ?: doc.id
                val savedAt = doc.getLong("savedAt") ?: System.currentTimeMillis()
                val userRating = doc.getLong("userRating")?.toInt() ?: 5
                val personalNote = doc.getString("personalNote") ?: ""

                FavoritePlaceEntity(
                    favoriteId = favId,
                    userId = userId,
                    placeId = placeId,
                    savedAt = savedAt,
                    userRating = userRating,
                    personalNote = personalNote
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to fetch remote favorites: ${e.message}", e)
            emptyList()
        }
    }
}
