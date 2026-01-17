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
import com.example.compost2.ui.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.provideFactory(context))

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        // ... HOME ...
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToRecorder = { navController.navigate(Screen.Recorder.route) },
                onNavigateToPlayer = { fileName -> navController.navigate("detail/$fileName") },
                onNavigateToSendSTT = { fileName -> navController.navigate("detail/$fileName") },
                onNavigateToPublish = { fileName -> navController.navigate("detail/$fileName") },
                onNavigateToPrompts = { navController.navigate("prompt_settings") },
                onNavigateToApiKey = { type -> navController.navigate(Screen.ApiKeySettings.createRoute(type)) },
                // onNavigateToSettings теперь ведет на Интеграции
                onNavigateToSettings = { navController.navigate(Screen.Integrations.route) }
            )
        }

        // ... ДРУГИЕ ЭКРАНЫ (Recorder, Detail, PromptSettings) ...
        composable(Screen.Recorder.route) { RecorderScreen { navController.popBackStack(Screen.Home.route, false) } }

        composable(
            route = "detail/{fileName}",
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            DetailScreen(
                fileName = backStackEntry.arguments?.getString("fileName") ?: "",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("prompt_settings") {
            PromptSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditPrompt = { promptId ->
                    val route = if (promptId != null) "prompt_edit?id=$promptId" else "prompt_edit"
                    navController.navigate(route)
                }
            )
        }

        // РЕДАКТОР ПРОМПТА
        composable(
            route = "prompt_edit?id={id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { backStackEntry ->
            val promptId = backStackEntry.arguments?.getString("id")
            PromptEditScreen(
                promptId = promptId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToIntegrations = { navController.navigate(Screen.Integrations.route) } // Переход
            )
        }

        // ЭКРАН ИНТЕГРАЦИЙ
        composable(Screen.Integrations.route) {
            IntegrationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // API KEY
        composable(
            route = Screen.ApiKeySettings.route,
            arguments = listOf(navArgument("serviceType") { type = NavType.StringType })
        ) { backStackEntry ->
            ApiKeyScreen(
                serviceType = backStackEntry.arguments?.getString("serviceType") ?: "openai",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Settings (Старый экран настроек Google, теперь может быть переименован или удален,
        // но пока оставим его, если он используется где-то еще, или переиспользуем для Google Sign In)
        // Если "onNavigateToSettings" из Home ведет на Integrations, то этот роут временно "сирота".
        // Оставим для совместимости
        composable(Screen.Settings.route) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}