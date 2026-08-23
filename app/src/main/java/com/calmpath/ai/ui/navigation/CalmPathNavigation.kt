package com.calmpath.ai.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.calmpath.ai.data.repository.AuthRepository
import com.calmpath.ai.data.repository.CalmPathRepository
import com.calmpath.ai.ui.components.CalmPathBottomNavBar
import com.calmpath.ai.ui.screens.auth.AuthScreen
import com.calmpath.ai.ui.screens.details.PlaceDetailsScreen
import com.calmpath.ai.ui.screens.explore.ExploreScreen
import com.calmpath.ai.ui.screens.favorites.FavoritesScreen
import com.calmpath.ai.ui.screens.history.HistoryScreen
import com.calmpath.ai.ui.screens.home.HomeScreen
import com.calmpath.ai.ui.screens.mood.MoodSelectionScreen
import com.calmpath.ai.ui.screens.profile.ProfileScreen
import com.calmpath.ai.ui.screens.settings.SettingsScreen
import com.calmpath.ai.ui.screens.welcome.WelcomeScreen
import com.calmpath.ai.ui.viewmodel.AuthViewModel
import com.calmpath.ai.ui.viewmodel.AuthViewModelFactory
import com.calmpath.ai.ui.viewmodel.ExploreViewModel
import com.calmpath.ai.ui.viewmodel.ExploreViewModelFactory
import com.calmpath.ai.ui.viewmodel.FavoritesViewModel
import com.calmpath.ai.ui.viewmodel.FavoritesViewModelFactory
import com.calmpath.ai.ui.viewmodel.HistoryViewModel
import com.calmpath.ai.ui.viewmodel.HistoryViewModelFactory
import com.calmpath.ai.ui.viewmodel.HomeViewModel
import com.calmpath.ai.ui.viewmodel.HomeViewModelFactory
import com.calmpath.ai.ui.viewmodel.PlaceDetailsViewModel
import com.calmpath.ai.ui.viewmodel.PlaceDetailsViewModelFactory
import com.calmpath.ai.ui.viewmodel.ProfileViewModel
import com.calmpath.ai.ui.viewmodel.ProfileViewModelFactory
import com.calmpath.ai.ui.viewmodel.SettingsViewModel
import com.calmpath.ai.ui.viewmodel.SettingsViewModelFactory

/**
 * Main Navigation & Root Scaffold for CalmPath AI (CO2).
 */
@Composable
fun CalmPathNavHost(
    repository: CalmPathRepository,
    authRepository: AuthRepository,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define routes that show the persistent bottom navigation bar
    val bottomBarRoutes = setOf(
        NavRoutes.Home.route,
        NavRoutes.Explore.route,
        NavRoutes.Favorites.route,
        NavRoutes.Profile.route
    )

    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CalmPathBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { targetRoute ->
                        navController.navigate(targetRoute) {
                            popUpTo(NavRoutes.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = NavRoutes.Welcome.route
            ) {
                // 1. Welcome / Onboarding
                composable(NavRoutes.Welcome.route) {
                    WelcomeScreen(
                        onGetStartedClick = {
                            navController.navigate(NavRoutes.MoodSelection.route)
                        },
                        onLoginClick = {
                            navController.navigate(NavRoutes.Auth.route)
                        },
                        onCreateAccountClick = {
                            navController.navigate(NavRoutes.Auth.route)
                        }
                    )
                }

                // 2. Authentication
                composable(NavRoutes.Auth.route) {
                    val authViewModel: AuthViewModel = viewModel(
                        factory = AuthViewModelFactory(authRepository)
                    )
                    AuthScreen(
                        viewModel = authViewModel,
                        onAuthSuccess = {
                            navController.navigate(NavRoutes.MoodSelection.route) {
                                popUpTo(NavRoutes.Welcome.route) { inclusive = true }
                            }
                        }
                    )
                }

                // 3. Mood Selection
                composable(NavRoutes.MoodSelection.route) {
                    val scope = androidx.compose.runtime.rememberCoroutineScope()
                    MoodSelectionScreen(
                        onMoodConfirmed = { selectedMood: com.calmpath.ai.data.model.Mood ->
                            scope.launch {
                                repository.saveMood(selectedMood)
                            }
                            navController.navigate(NavRoutes.Home.route) {
                                popUpTo(NavRoutes.Welcome.route) { inclusive = true }
                            }
                        }
                    )
                }

                // 4. Home Dashboard
                composable(NavRoutes.Home.route) {
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = HomeViewModelFactory(repository)
                    )
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToDetails = { placeId ->
                            navController.navigate(NavRoutes.PlaceDetails.createRoute(placeId))
                        },
                        onChangeMoodClick = {
                            navController.navigate(NavRoutes.MoodSelection.route)
                        },
                        onNavigateToExplore = {
                            navController.navigate(NavRoutes.Explore.route)
                        }
                    )
                }

                // 5. Explore & Heatmap
                composable(NavRoutes.Explore.route) {
                    val exploreViewModel: ExploreViewModel = viewModel(
                        factory = ExploreViewModelFactory(repository)
                    )
                    ExploreScreen(
                        viewModel = exploreViewModel,
                        onNavigateToDetails = { placeId ->
                            navController.navigate(NavRoutes.PlaceDetails.createRoute(placeId))
                        }
                    )
                }

                // 6. Favorites
                composable(NavRoutes.Favorites.route) {
                    val favoritesViewModel: FavoritesViewModel = viewModel(
                        factory = FavoritesViewModelFactory(repository)
                    )
                    FavoritesScreen(
                        viewModel = favoritesViewModel,
                        onNavigateToDetails = { placeId ->
                            navController.navigate(NavRoutes.PlaceDetails.createRoute(placeId))
                        },
                        onExploreClick = {
                            navController.navigate(NavRoutes.Explore.route)
                        }
                    )
                }

                // 7. Profile
                composable(NavRoutes.Profile.route) {
                    val profileViewModel: ProfileViewModel = viewModel(
                        factory = ProfileViewModelFactory(repository, authRepository)
                    )
                    ProfileScreen(
                        viewModel = profileViewModel,
                        onNavigateToFavorites = {
                            navController.navigate(NavRoutes.Favorites.route)
                        },
                        onNavigateToHistory = {
                            navController.navigate(NavRoutes.History.route)
                        },
                        onNavigateToSettings = {
                            navController.navigate(NavRoutes.Settings.route)
                        },
                        onNavigateToMoodSelection = {
                            navController.navigate(NavRoutes.MoodSelection.route)
                        },
                        onLogoutSuccess = {
                            navController.navigate(NavRoutes.Welcome.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                // 8. Place Details (with argument)
                composable(
                    route = NavRoutes.PlaceDetails.route,
                    arguments = listOf(navArgument("placeId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val placeId = backStackEntry.arguments?.getString("placeId") ?: "place_1"
                    val detailsViewModel: PlaceDetailsViewModel = viewModel(
                        factory = PlaceDetailsViewModelFactory(placeId, repository)
                    )
                    PlaceDetailsScreen(
                        viewModel = detailsViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 9. History
                composable(NavRoutes.History.route) {
                    val historyViewModel: HistoryViewModel = viewModel(
                        factory = HistoryViewModelFactory(repository)
                    )
                    HistoryScreen(
                        viewModel = historyViewModel,
                        onNavigateToDetails = { placeId ->
                            navController.navigate(NavRoutes.PlaceDetails.createRoute(placeId))
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 10. Settings
                composable(NavRoutes.Settings.route) {
                    val settingsViewModel: SettingsViewModel = viewModel(
                        factory = SettingsViewModelFactory(repository, authRepository)
                    )
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onBackClick = { navController.popBackStack() },
                        onLogoutSuccess = {
                            navController.navigate(NavRoutes.Welcome.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
