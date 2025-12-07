package com.example.compost2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.compost2.ui.screens.EditorScreen
import com.example.compost2.ui.screens.HomeScreen
import com.example.compost2.ui.screens.PlayerScreen
import com.example.compost2.ui.screens.RecorderScreen
import com.example.compost2.ui.screens.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        // Главный экран
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToRecorder = {
                    navController.navigate(Screen.Recorder.route)
                },
                onNavigateToPlayer = { fileName ->
                    // Передаем имя файла в плеер
                    navController.navigate(Screen.Player.createRoute(fileName))
                }
            )
        }

        // Экран записи
        composable(Screen.Recorder.route) {
            RecorderScreen(
                onNavigateToHome = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }

        // Экран Плеера (теперь принимает аргумент)
        composable(
            route = Screen.Player.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            // Достаем имя файла из аргументов
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            PlayerScreen(
                fileName = fileName,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Экран редактора
        composable(Screen.Editor.route) {
            EditorScreen()
        }

        // Экран настроек
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}