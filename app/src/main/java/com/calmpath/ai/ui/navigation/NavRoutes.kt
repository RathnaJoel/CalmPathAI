package com.calmpath.ai.ui.navigation

/**
 * Sealed routes for CalmPath AI Navigation (CO2).
 */
sealed class NavRoutes(val route: String) {
    data object Welcome : NavRoutes("welcome")
    data object Auth : NavRoutes("auth")
    data object MoodSelection : NavRoutes("mood_selection")
    data object Home : NavRoutes("home")
    data object Explore : NavRoutes("explore")
    data object Favorites : NavRoutes("favorites")
    data object Profile : NavRoutes("profile")
    data object History : NavRoutes("history")
    data object Settings : NavRoutes("settings")

    data object PlaceDetails : NavRoutes("place_details/{placeId}") {
        fun createRoute(placeId: String) = "place_details/$placeId"
    }
}
