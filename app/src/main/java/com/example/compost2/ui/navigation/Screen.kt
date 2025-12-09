package com.example.compost2.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Recorder : Screen("recorder")
    object Editor : Screen("editor")
    object Settings : Screen("settings")

    // Экран плеера
    object Player : Screen("player/{fileName}") {
        fun createRoute(fileName: String) = "player/$fileName"
    }

    // НОВЫЙ ЭКРАН: Отправка в STT
    object SendToSTT : Screen("send_stt/{fileName}") {
        fun createRoute(fileName: String) = "send_stt/$fileName"
    }
}