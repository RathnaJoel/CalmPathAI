package com.calmpath.ai.data.repository

import com.calmpath.ai.data.auth.AuthManager
import com.calmpath.ai.data.auth.AuthState
import com.calmpath.ai.data.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

/**
 * Authentication Repository handling user credentials, sessions, and profile data (CO4).
 */
class AuthRepository(
    private val authManager: AuthManager
) {
    val authState: StateFlow<AuthState> = authManager.authState
    val currentUser: StateFlow<UserProfile?> = authManager.currentUser

    suspend fun signIn(email: String, pass: String): Result<UserProfile> {
        return authManager.signIn(email, pass)
    }

    suspend fun signUp(name: String, email: String, pass: String): Result<UserProfile> {
        return authManager.signUp(name, email, pass)
    }

    fun signInAsGuest(name: String = "Calm Traveler", email: String = "guest@calmpath.ai") {
        authManager.signInAsDemoGuest(name, email)
    }

    fun signOut() {
        authManager.signOut()
    }
}
