package com.calmpath.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.calmpath.ai.data.local.entities.EnvironmentalSnapshotEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for EnvironmentalSnapshotEntity operations.
 */
@Dao
interface EnvironmentalSnapshotDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: EnvironmentalSnapshotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSnapshots(snapshots: List<EnvironmentalSnapshotEntity>)

    @Update
    suspend fun updateSnapshot(snapshot: EnvironmentalSnapshotEntity)

    @Delete
    suspend fun deleteSnapshot(snapshot: EnvironmentalSnapshotEntity)

    @Query("SELECT * FROM environmental_snapshots WHERE placeId = :placeId ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatestSnapshotForPlace(placeId: String): EnvironmentalSnapshotEntity?

    @Query("SELECT * FROM environmental_snapshots WHERE placeId = :placeId ORDER BY recordedAt DESC LIMIT 1")
    fun getLatestSnapshotFlowForPlace(placeId: String): Flow<EnvironmentalSnapshotEntity?>

    @Query("SELECT * FROM environmental_snapshots WHERE placeId = :placeId ORDER BY recordedAt DESC")
    fun getSnapshotsForPlaceFlow(placeId: String): Flow<List<EnvironmentalSnapshotEntity>>

    @Query("SELECT * FROM environmental_snapshots ORDER BY recordedAt DESC")
    fun getAllSnapshotsFlow(): Flow<List<EnvironmentalSnapshotEntity>>

    @Query("SELECT COUNT(*) FROM environmental_snapshots")
    suspend fun getSnapshotCount(): Int
}
