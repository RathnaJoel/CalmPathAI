package com.calmpath.ai.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity 1: UserProfile
 * Stores basic local user information.
 */
@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val userId: String,
    val name: String,
    val email: String,
    val profileImage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis()
)
