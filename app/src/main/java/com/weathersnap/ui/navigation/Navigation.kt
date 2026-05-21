package com.weathersnap.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.weathersnap.ui.screens.camera.CameraScreen
import com.weathersnap.ui.screens.report.CreateReportScreen
import com.weathersnap.ui.screens.savedreports.SavedReportsScreen
import com.weathersnap.ui.screens.weather.WeatherScreen

sealed class Screen(val route: String) {
    object Weather : Screen("weather")
    object CreateReport : Screen("create_report/{cityName}/{temperature}/{condition}/{humidity}/{windSpeed}/{pressure}") {
        fun createRoute(cityName: String, temperature: Double, condition: String,
                        humidity: Int, windSpeed: Double, pressure: Double): String {
            val encodedCity = java.net.URLEncoder.encode(cityName, "UTF-8")
            val encodedCondition = java.net.URLEncoder.encode(condition, "UTF-8")
            return "create_report/$encodedCity/$temperature/$encodedCondition/$humidity/$windSpeed/$pressure"
        }
    }
    object Camera : Screen("camera")
    object SavedReports : Screen("saved_reports")
}

@Composable
fun WeatherSnapNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Weather.route) {

        composable(Screen.Weather.route) {
            WeatherScreen(
                onNavigateToReport = { cityName, temperature, condition, humidity, windSpeed, pressure ->
                    navController.navigate(
                        Screen.CreateReport.createRoute(cityName, temperature, condition, humidity, windSpeed, pressure)
                    )
                },
                onNavigateToReports = { navController.navigate(Screen.SavedReports.route) }
            )
        }

        composable(
            route = Screen.CreateReport.route,
            arguments = listOf(
                navArgument("cityName") { type = NavType.StringType },
                navArgument("temperature") { type = NavType.FloatType },
                navArgument("condition") { type = NavType.StringType },
                navArgument("humidity") { type = NavType.IntType },
                navArgument("windSpeed") { type = NavType.FloatType },
                navArgument("pressure") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val cityName = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("cityName") ?: "", "UTF-8")
            val condition = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("condition") ?: "", "UTF-8")
            CreateReportScreen(
                cityName = cityName,
                temperature = backStackEntry.arguments?.getFloat("temperature")?.toDouble() ?: 0.0,
                condition = condition,
                humidity = backStackEntry.arguments?.getInt("humidity") ?: 0,
                windSpeed = backStackEntry.arguments?.getFloat("windSpeed")?.toDouble() ?: 0.0,
                pressure = backStackEntry.arguments?.getFloat("pressure")?.toDouble() ?: 0.0,
                navBackStackEntry = backStackEntry,
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                onReportSaved = {
                    navController.navigate(Screen.SavedReports.route) {
                        popUpTo(Screen.Weather.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onImageCaptured = { imagePath ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("captured_image_path", imagePath)
                    navController.popBackStack()
                },
                onClose = { navController.popBackStack() }
            )
        }

        composable(Screen.SavedReports.route) {
            SavedReportsScreen(onBack = { navController.popBackStack() })
        }
    }
}
