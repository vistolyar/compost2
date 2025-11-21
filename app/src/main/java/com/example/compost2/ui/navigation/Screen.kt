package com.example.compost2.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Recorder : Screen("recorder")
    object Editor : Screen("editor")
    object Settings : Screen("settings")
}