package com.example.compost2.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Recorder : Screen("recorder")
    object Editor : Screen("editor")
    object Settings : Screen("settings")

    // Изменяем маршрут: теперь он ждет параметр {fileName}
    object Player : Screen("player/{fileName}") {
        // Вспомогательная функция для создания ссылки
        fun createRoute(fileName: String) = "player/$fileName"
    }
}