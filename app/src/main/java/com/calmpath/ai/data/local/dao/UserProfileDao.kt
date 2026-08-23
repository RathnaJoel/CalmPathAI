package com.calmpath.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.calmpath.ai.data.local.entities.UserProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for UserProfileEntity operations.
 */
@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfileEntity)

    @Update
    suspend fun updateUser(user: UserProfileEntity)

    @Delete
    suspend fun deleteUser(user: UserProfileEntity)

    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    fun getUserFlow(userId: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles ORDER BY lastLogin DESC LIMIT 1")
    fun getActiveUserFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles ORDER BY lastLogin DESC LIMIT 1")
    suspend fun getActiveUser(): UserProfileEntity?

    @Query("UPDATE user_profiles SET lastLogin = :timestamp WHERE userId = :userId")
    suspend fun updateLastLogin(userId: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM user_profiles")
    fun getAllUsersFlow(): Flow<List<UserProfileEntity>>
}
