package com.example.compost2.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Recorder : Screen("recorder")

    // ИЗМЕНЕНО: Editor теперь тоже принимает fileName
    object Editor : Screen("editor/{fileName}") {
        fun createRoute(fileName: String) = "editor/$fileName"
    }

    object Settings : Screen("settings")

    object Player : Screen("player/{fileName}") {
        fun createRoute(fileName: String) = "player/$fileName"
    }

    object SendToSTT : Screen("send_stt/{fileName}") {
        fun createRoute(fileName: String) = "send_stt/$fileName"
    }

    object Publication : Screen("publish/{fileName}") {
        fun createRoute(fileName: String) = "publish/$fileName"
    }
}