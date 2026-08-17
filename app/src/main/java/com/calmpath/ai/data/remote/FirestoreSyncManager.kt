package com.calmpath.ai.data.remote

import android.util.Log
import com.calmpath.ai.data.local.entities.FavoritePlaceEntity
import com.calmpath.ai.data.local.entities.HistoryEntity
import com.calmpath.ai.data.local.entities.UserPreferencesEntity
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
     * Synchronizes a single favorite place to Firestore under `/users/{userId}/favorites/{placeId}`.
     */
    suspend fun syncFavoriteToCloud(userId: String, favorite: FavoritePlaceEntity): Boolean {
        if (userId.isBlank()) return false
        return try {
            val db = firestore ?: return false
            val favMap = hashMapOf(
                "id" to favorite.id,
                "placeName" to favorite.placeName,
                "category" to favorite.category,
                "categoryIcon" to favorite.categoryIcon,
                "latitude" to favorite.latitude,
                "longitude" to favorite.longitude,
                "peaceScore" to favorite.peaceScore,
                "aqi" to favorite.aqi,
                "noiseLevel" to favorite.noiseLevel,
                "distance" to favorite.distance,
                "imageUrl" to favorite.imageUrl,
                "address" to favorite.address,
                "description" to favorite.description,
                "recommendationReasons" to favorite.recommendationReasons,
                "savedAtTimestamp" to favorite.savedAtTimestamp,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(favorite.id)
                .set(favMap, SetOptions.merge())
                .await()
            Log.d(tag, "Successfully synced favorite '${favorite.placeName}' to Firestore")
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
                "selectedMood" to preferences.selectedMood,
                "preferredCategory" to preferences.preferredCategory,
                "maxAqi" to preferences.maxAqi,
                "preferredNoiseLevel" to preferences.preferredNoiseLevel,
                "preferredDistanceKm" to preferences.preferredDistanceKm,
                "notificationsEnabled" to preferences.notificationsEnabled,
                "themeMode" to preferences.themeMode,
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
    suspend fun syncHistoryToCloud(userId: String, history: HistoryEntity): Boolean {
        if (userId.isBlank()) return false
        return try {
            val db = firestore ?: return false
            val histMap = hashMapOf(
                "placeId" to history.placeId,
                "placeName" to history.placeName,
                "category" to history.category,
                "categoryIcon" to history.categoryIcon,
                "viewedAt" to history.viewedAt,
                "peaceScore" to history.peaceScore,
                "aqi" to history.aqi,
                "noiseLevel" to history.noiseLevel,
                "imageUrl" to history.imageUrl
            )
            db.collection("users")
                .document(userId)
                .collection("history")
                .document("${history.placeId}_${history.viewedAt}")
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
                val id = doc.getString("id") ?: doc.id
                val placeName = doc.getString("placeName") ?: return@mapNotNull null
                val category = doc.getString("category") ?: "Parks"
                val categoryIcon = doc.getString("categoryIcon") ?: "🌿"
                val latitude = doc.getDouble("latitude") ?: 0.0
                val longitude = doc.getDouble("longitude") ?: 0.0
                val peaceScore = doc.getLong("peaceScore")?.toInt() ?: 85
                val aqi = doc.getLong("aqi")?.toInt() ?: 30
                val noiseLevel = doc.getLong("noiseLevel")?.toInt() ?: 40
                val distance = doc.getDouble("distance") ?: 1.0
                val imageUrl = doc.getString("imageUrl") ?: ""
                val address = doc.getString("address") ?: ""
                val description = doc.getString("description") ?: ""
                @Suppress("UNCHECKED_CAST")
                val reasons = doc.get("recommendationReasons") as? List<String> ?: emptyList()
                val savedAt = doc.getLong("savedAtTimestamp") ?: System.currentTimeMillis()

                FavoritePlaceEntity(
                    id = id,
                    placeName = placeName,
                    category = category,
                    categoryIcon = categoryIcon,
                    latitude = latitude,
                    longitude = longitude,
                    peaceScore = peaceScore,
                    aqi = aqi,
                    noiseLevel = noiseLevel,
                    distance = distance,
                    imageUrl = imageUrl,
                    address = address,
                    description = description,
                    recommendationReasons = reasons,
                    savedAtTimestamp = savedAt,
                    isSyncedWithCloud = true
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to fetch remote favorites: ${e.message}", e)
            emptyList()
        }
    }
}
