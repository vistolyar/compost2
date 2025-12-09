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
import com.example.compost2.ui.screens.PublicationScreen
import com.example.compost2.ui.screens.RecorderScreen
import com.example.compost2.ui.screens.SendToSTTScreen
import com.example.compost2.ui.screens.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.provideFactory(context))

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
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
                },
                onNavigateToPublish = { fileName -> // НОВЫЙ КОЛБЭК
                    navController.navigate(Screen.Publication.createRoute(fileName))
                }
            )
        }

        composable(Screen.Recorder.route) {
            RecorderScreen(
                onNavigateToHome = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }

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
                    val item = homeViewModel.recordings.find { it.id == fileName }
                    if (item != null) {
                        homeViewModel.sendToSTT(item)
                    }
                    navController.popBackStack()
                }
            )
        }

        // НОВЫЙ МАРШРУТ: Publication Screen
        composable(
            route = Screen.Publication.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""

            PublicationScreen(
                fileName = fileName,
                onNavigateBack = { navController.popBackStack() },
                onPublished = {
                    // Обновляем статус в главной ViewModel на PUBLISHED
                    val item = homeViewModel.recordings.find { it.id == fileName }
                    if (item != null) {
                        homeViewModel.mockPublish(item) // Переводим в зеленую карточку
                    }
                    navController.popBackStack()
                },
                onDeleted = {
                    // Удаляем из главной ViewModel
                    val item = homeViewModel.recordings.find { it.id == fileName }
                    if (item != null) {
                        homeViewModel.requestDelete(item) // Сначала помечаем
                        homeViewModel.confirmDelete()     // Потом удаляем (т.к. подтверждение было внутри экрана)
                    }
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Editor.route) { EditorScreen() }
        composable(Screen.Settings.route) { SettingsScreen() }
    }
}