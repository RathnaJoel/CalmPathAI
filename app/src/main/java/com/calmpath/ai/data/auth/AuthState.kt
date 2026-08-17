package com.calmpath.ai.data.auth

import com.calmpath.ai.data.model.UserProfile

/**
 * Authentication states for CalmPath AI (CO4).
 */
sealed interface AuthState {
    data object Initial : AuthState
    data object Loading : AuthState
    data class Authenticated(val user: UserProfile) : AuthState
    data object Unauthenticated : AuthState
    data class Error(val message: String) : AuthState
}
