package com.skylens.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.skylens.presentation.ui.screens.about.AboutScreen
import com.skylens.presentation.ui.screens.about.LicensesScreen
import com.skylens.presentation.ui.screens.auth.AuthScreen
import com.skylens.presentation.ui.screens.chat.AskAIScreen
import com.skylens.presentation.ui.screens.download.PackDownloadScreen
import com.skylens.presentation.ui.screens.flight.FlightMapScreen
import com.skylens.presentation.ui.screens.history.TripHistoryScreen
import com.skylens.presentation.ui.screens.history.TripReplayScreen
import com.skylens.presentation.ui.screens.landmark.LandmarkDetailScreen
import com.skylens.presentation.ui.screens.onboarding.OnboardingScreen
import com.skylens.presentation.ui.screens.permissions.PermissionsScreen
import com.skylens.presentation.ui.screens.planning.FlightPlanningScreen
import com.skylens.presentation.ui.screens.settings.SettingsScreen
import com.skylens.presentation.ui.screens.splash.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Permissions : Screen("permissions")
    object Auth : Screen("auth")
    object Planning : Screen("planning")
    object PackDownload : Screen("pack_download/{departure}/{arrival}") {
        fun createRoute(departure: String, arrival: String) = "pack_download/$departure/$arrival"
    }
    object FlightMap : Screen("flight_map/{departure}/{arrival}") {
        fun createRoute(departure: String, arrival: String) = "flight_map/$departure/$arrival"
    }
    object History : Screen("history")
    object Settings : Screen("settings")
    object AskAI : Screen("ask_ai")
    object LandmarkDetail : Screen("landmark_detail/{landmarkId}?landmarkIds={landmarkIds}") {
        fun createRoute(landmarkId: String, landmarkIds: List<String> = emptyList()) =
            if (landmarkIds.isEmpty()) {
                "landmark_detail/$landmarkId"
            } else {
                "landmark_detail/$landmarkId?landmarkIds=${landmarkIds.joinToString(",")}"
            }
    }
    object TripReplay : Screen("trip_replay/{tripId}") {
        fun createRoute(tripId: String) = "trip_replay/$tripId"
    }
    object About : Screen("about")
    object Licenses : Screen("licenses")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToPermissions = { navController.navigate(Screen.Permissions.route) },
                onNavigateToAuth = { navController.navigate(Screen.Auth.route) },
                onNavigateToPlanning = { navController.navigate(Screen.Planning.route) },
                onNavigateToOnboarding = { navController.navigate(Screen.Onboarding.route) }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Permissions.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Permissions.route) {
            PermissionsScreen(
                onPermissionsGranted = {
                    navController.navigate(Screen.Planning.route) {
                        popUpTo(Screen.Permissions.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Auth.route) {
            AuthScreen(
                onNavigateToPlanning = {
                    navController.navigate(Screen.Planning.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Planning.route) {
            FlightPlanningScreen(
                onNavigateToMap = { departure, arrival ->
                    navController.navigate(Screen.PackDownload.createRoute(departure, arrival))
                },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.PackDownload.route,
            arguments = listOf(
                navArgument("departure") { type = NavType.StringType },
                navArgument("arrival") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val departure = backStackEntry.arguments?.getString("departure") ?: ""
            val arrival = backStackEntry.arguments?.getString("arrival") ?: ""
            PackDownloadScreen(
                departure = departure,
                arrival = arrival,
                onNavigateBack = { navController.popBackStack() },
                onDownloadComplete = {
                    navController.navigate(Screen.FlightMap.createRoute(departure, arrival)) {
                        popUpTo(Screen.PackDownload.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.FlightMap.route,
            arguments = listOf(
                navArgument("departure") { type = NavType.StringType },
                navArgument("arrival") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val departure = backStackEntry.arguments?.getString("departure") ?: ""
            val arrival = backStackEntry.arguments?.getString("arrival") ?: ""
            FlightMapScreen(
                departure = departure,
                arrival = arrival,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAskAI = { navController.navigate(Screen.AskAI.route) },
                onNavigateToLandmarkDetail = { landmarkId, landmarkIds ->
                    navController.navigate(Screen.LandmarkDetail.createRoute(landmarkId, landmarkIds))
                }
            )
        }

        composable(Screen.History.route) {
            TripHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onTripClick = { tripId, departure, arrival ->
                    // Navigate to trip replay when clicking the card
                    android.util.Log.d("NavGraph", "Trip clicked: $tripId, navigating to replay")
                    navController.navigate(Screen.TripReplay.createRoute(tripId))
                },
                onTripReplay = { tripId ->
                    android.util.Log.d("NavGraph", "Trip replay button clicked: $tripId")
                    navController.navigate(Screen.TripReplay.createRoute(tripId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }

        composable(Screen.AskAI.route) {
            AskAIScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.LandmarkDetail.route,
            arguments = listOf(
                navArgument("landmarkId") { type = NavType.StringType },
                navArgument("landmarkIds") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val landmarkId = backStackEntry.arguments?.getString("landmarkId") ?: ""
            val landmarkIdsString = backStackEntry.arguments?.getString("landmarkIds")
            val landmarkIds = landmarkIdsString?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

            LandmarkDetailScreen(
                landmarkId = landmarkId,
                landmarkIds = landmarkIds,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLandmark = { newLandmarkId ->
                    navController.navigate(Screen.LandmarkDetail.createRoute(newLandmarkId, landmarkIds)) {
                        popUpTo(Screen.LandmarkDetail.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.TripReplay.route,
            arguments = listOf(
                navArgument("tripId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            TripReplayScreen(
                tripId = tripId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLandmarkDetail = { landmarkId, landmarkIds ->
                    navController.navigate(Screen.LandmarkDetail.createRoute(landmarkId, landmarkIds))
                }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLicenses = { navController.navigate(Screen.Licenses.route) }
            )
        }

        composable(Screen.Licenses.route) {
            LicensesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
