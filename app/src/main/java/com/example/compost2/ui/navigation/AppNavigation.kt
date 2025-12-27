package com.example.compost2.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.compost2.ui.screens.ApiKeyScreen
import com.example.compost2.ui.screens.EditorScreen
import com.example.compost2.ui.screens.HomeScreen
import com.example.compost2.ui.screens.HomeViewModel
import com.example.compost2.ui.screens.PlayerScreen
import com.example.compost2.ui.screens.PromptSettingsScreen
import com.example.compost2.ui.screens.PublicationScreen
import com.example.compost2.ui.screens.RecorderScreen
import com.example.compost2.ui.screens.SendToSTTScreen
import com.example.compost2.ui.screens.SendToSTTViewModel
import com.example.compost2.ui.screens.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Создаем ViewModel здесь, так как изменения в MainActivity не применялись
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.provideFactory(context))

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        // --- ГЛАВНЫЙ ЭКРАН (С АНИМАЦИЕЙ) ---
        composable(
            route = Screen.Home.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400))
            }
        ) {
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
                onNavigateToPublish = { fileName ->
                    navController.navigate(Screen.Editor.createRoute(fileName))
                },
                onNavigateToPrompts = {
                    navController.navigate("prompt_settings")
                },
                onNavigateToApiKey = { type ->
                    navController.navigate(Screen.ApiKeySettings.createRoute(type))
                }
            )
        }

        // --- ЗАПИСЬ (С АНИМАЦИЕЙ) ---
        composable(
            route = Screen.Recorder.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400))
            }
        ) {
            RecorderScreen(
                onNavigateToHome = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }

        // --- ПЛЕЕР ---
        composable(
            route = Screen.Player.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            PlayerScreen(
                fileName = fileName,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSendSTT = {
                    navController.navigate(Screen.SendToSTT.createRoute(fileName))
                }
            )
        }

        // --- ОТПРАВКА В AI ---
        composable(
            route = Screen.SendToSTT.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""

            val sendToSTTViewModel: SendToSTTViewModel = viewModel(factory = SendToSTTViewModel.provideFactory(context))

            SendToSTTScreen(
                viewModel = sendToSTTViewModel,
                fileName = fileName,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = { file ->
                    navController.navigate(Screen.Player.createRoute(file))
                },
                onProcessStarted = {
                    val item = homeViewModel.recordings.find { it.id == fileName }
                    if (item != null) {
                        val title = sendToSTTViewModel.resultTitle
                        val content = sendToSTTViewModel.resultBody
                        homeViewModel.onAiResultReceived(item, title, content)
                    }
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }

        // --- РЕДАКТОР ---
        composable(
            route = Screen.Editor.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            val item = homeViewModel.recordings.find { it.id == fileName }

            EditorScreen(
                item = item,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPublish = {
                    navController.navigate(Screen.Publication.createRoute(fileName))
                },
                onRecreate = {
                    navController.navigate(Screen.SendToSTT.createRoute(fileName))
                },
                onUpdateContent = { title, content ->
                    homeViewModel.updateArticleData(fileName, title, content)
                }
            )
        }

        // --- ПУБЛИКАЦИЯ ---
        composable(
            route = Screen.Publication.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""

            PublicationScreen(
                fileName = fileName,
                onNavigateBack = { navController.popBackStack() },
                onPublished = { url, wpId ->
                    val item = homeViewModel.recordings.find { it.id == fileName }
                    if (item != null) {
                        homeViewModel.onPublishedSuccess(item, url, wpId)
                    }
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                },
                onDeleted = {
                    val item = homeViewModel.recordings.find { it.id == fileName }
                    if (item != null) {
                        homeViewModel.requestDelete(item)
                        homeViewModel.confirmDelete()
                    }
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }

        // --- НАСТРОЙКИ КЛЮЧЕЙ ---
        composable(
            route = Screen.ApiKeySettings.route,
            arguments = listOf(navArgument("serviceType") { type = NavType.StringType })
        ) { backStackEntry ->
            val serviceType = backStackEntry.arguments?.getString("serviceType") ?: "openai"
            ApiKeyScreen(
                serviceType = serviceType,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- НАСТРОЙКИ ПРОМПТОВ ---
        composable("prompt_settings") {
            PromptSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) { SettingsScreen() }
    }
}