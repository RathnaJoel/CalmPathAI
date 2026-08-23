package com.calmpath.ai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.calmpath.ai.data.local.dao.AppSettingsDao
import com.calmpath.ai.data.local.dao.EnvironmentalSnapshotDao
import com.calmpath.ai.data.local.dao.FavoritePlaceDao
import com.calmpath.ai.data.local.dao.MoodHistoryDao
import com.calmpath.ai.data.local.dao.PlaceDao
import com.calmpath.ai.data.local.dao.PlaceHistoryDao
import com.calmpath.ai.data.local.dao.UserPreferencesDao
import com.calmpath.ai.data.local.dao.UserProfileDao
import com.calmpath.ai.data.local.entities.AppSettingsEntity
import com.calmpath.ai.data.local.entities.EnvironmentalSnapshotEntity
import com.calmpath.ai.data.local.entities.FavoritePlaceEntity
import com.calmpath.ai.data.local.entities.MoodHistoryEntity
import com.calmpath.ai.data.local.entities.PlaceEntity
import com.calmpath.ai.data.local.entities.PlaceHistoryEntity
import com.calmpath.ai.data.local.entities.UserPreferencesEntity
import com.calmpath.ai.data.local.entities.UserProfileEntity

/**
 * Complete Room Database for CalmPath AI (CO3: Local Database Implementation).
 * Contains exactly 8 entities:
 * 1. UserProfileEntity
 * 2. UserPreferencesEntity
 * 3. PlaceEntity
 * 4. FavoritePlaceEntity
 * 5. PlaceHistoryEntity
 * 6. MoodHistoryEntity
 * 7. EnvironmentalSnapshotEntity
 * 8. AppSettingsEntity
 */
@Database(
    entities = [
        UserProfileEntity::class,
        UserPreferencesEntity::class,
        PlaceEntity::class,
        FavoritePlaceEntity::class,
        PlaceHistoryEntity::class,
        MoodHistoryEntity::class,
        EnvironmentalSnapshotEntity::class,
        AppSettingsEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CalmPathDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun placeDao(): PlaceDao
    abstract fun favoritePlaceDao(): FavoritePlaceDao
    abstract fun placeHistoryDao(): PlaceHistoryDao
    abstract fun moodHistoryDao(): MoodHistoryDao
    abstract fun environmentalSnapshotDao(): EnvironmentalSnapshotDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: CalmPathDatabase? = null

        fun getDatabase(context: Context): CalmPathDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalmPathDatabase::class.java,
                    "calmpath_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getInstance(context: Context): CalmPathDatabase = getDatabase(context)
    }
}
