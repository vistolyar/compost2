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
import com.example.compost2.ui.screens.DetailScreen
import com.example.compost2.ui.screens.HomeScreen
import com.example.compost2.ui.screens.HomeViewModel
import com.example.compost2.ui.screens.PromptEditScreen
import com.example.compost2.ui.screens.PromptSettingsScreen
import com.example.compost2.ui.screens.RecorderScreen
import com.example.compost2.ui.screens.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.provideFactory(context))

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        // --- ГЛАВНЫЙ ЭКРАН ---
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
                    navController.navigate("detail/$fileName")
                },
                onNavigateToSendSTT = { fileName ->
                    navController.navigate("detail/$fileName")
                },
                onNavigateToPublish = { fileName ->
                    navController.navigate("detail/$fileName")
                },
                onNavigateToPrompts = {
                    navController.navigate("prompt_settings")
                },
                onNavigateToApiKey = { type ->
                    navController.navigate(Screen.ApiKeySettings.createRoute(type))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // --- ЗАПИСЬ ---
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

        // --- ЕДИНЫЙ ЭКРАН ПРОСМОТРА (DETAIL) ---
        composable(
            route = "detail/{fileName}",
            arguments = listOf(navArgument("fileName") { type = NavType.StringType }),
            // ИЗМЕНЕНИЕ: Теперь анимация горизонтальная (слайд справа)
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
            },
            // ИЗМЕНЕНИЕ: При выходе (Back) экран уезжает вправо (как при свайпе)
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
            }
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            DetailScreen(
                fileName = fileName,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- СПИСОК ПРОМПТОВ ---
        composable("prompt_settings") {
            PromptSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditPrompt = { promptId ->
                    val route = if (promptId != null) "prompt_edit?id=$promptId" else "prompt_edit"
                    navController.navigate(route)
                }
            )
        }

        // --- РЕДАКТОР ПРОМПТА ---
        composable(
            route = "prompt_edit?id={id}",
            arguments = listOf(navArgument("id") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val promptId = backStackEntry.arguments?.getString("id")
            PromptEditScreen(
                promptId = promptId,
                onNavigateBack = { navController.popBackStack() }
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

        // --- ЭКРАН НАСТРОЕК GOOGLE ---
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}