package com.calmpath.ai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.calmpath.ai.data.local.dao.FavoriteDao
import com.calmpath.ai.data.local.dao.HistoryDao
import com.calmpath.ai.data.local.dao.UserPreferencesDao
import com.calmpath.ai.data.local.entities.FavoritePlaceEntity
import com.calmpath.ai.data.local.entities.HistoryEntity
import com.calmpath.ai.data.local.entities.UserPreferencesEntity

/**
 * Main Room Database for CalmPath AI (CO3).
 */
@Database(
    entities = [
        FavoritePlaceEntity::class,
        HistoryEntity::class,
        UserPreferencesEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CalmPathDatabase : RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao
    abstract fun historyDao(): HistoryDao
    abstract fun userPreferencesDao(): UserPreferencesDao

    companion object {
        @Volatile
        private var INSTANCE: CalmPathDatabase? = null

        fun getInstance(context: Context): CalmPathDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalmPathDatabase::class.java,
                    "calmpath_ai_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
