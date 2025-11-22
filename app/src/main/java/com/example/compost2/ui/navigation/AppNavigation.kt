package com.example.compost2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
                onNavigateToPlayer = {
                    navController.navigate(Screen.Player.route)
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

        // Экран Плеера
        composable(Screen.Player.route) {
            PlayerScreen(
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