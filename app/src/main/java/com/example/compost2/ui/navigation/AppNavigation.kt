package com.example.compost2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.compost2.ui.screens.EditorScreen
import com.example.compost2.ui.screens.HomeScreen
import com.example.compost2.ui.screens.HomeViewModel
import com.example.compost2.ui.screens.PlayerScreen
import com.example.compost2.ui.screens.RecorderScreen
import com.example.compost2.ui.screens.SendToSTTScreen
import com.example.compost2.ui.screens.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // ВАЖНО: Создаем ViewModel ЗДЕСЬ, чтобы она была одна на всё приложение
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.provideFactory(context))

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        // Главный экран
        composable(Screen.Home.route) {
            // Передаем уже созданную viewModel
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToRecorder = {
                    navController.navigate(Screen.Recorder.route)
                },
                onNavigateToPlayer = { fileName ->
                    navController.navigate(Screen.Player.createRoute(fileName))
                },
                onNavigateToSendSTT = { fileName ->
                    navController.navigate(Screen.SendToSTT.createRoute(fileName))
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
        composable(
            route = Screen.Player.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            PlayerScreen(
                fileName = fileName,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Экран Отправки в STT
        composable(
            route = Screen.SendToSTT.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""

            SendToSTTScreen(
                fileName = fileName,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = { file ->
                    navController.navigate(Screen.Player.createRoute(file))
                },
                onProcessStarted = {
                    // 1. Находим нужную запись в памяти ОБЩЕЙ ViewModel
                    val item = homeViewModel.recordings.find { it.id == fileName }
                    if (item != null) {
                        // 2. Меняем статус на PROCESSING
                        homeViewModel.sendToSTT(item)
                    }
                    // 3. Возвращаемся. HomeScreen увидит изменение, так как VM общая.
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Editor.route) { EditorScreen() }
        composable(Screen.Settings.route) { SettingsScreen() }
    }
}