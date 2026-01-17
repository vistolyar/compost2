package com.example.compost2.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compost2.ui.navigation.AppNavigation
import com.example.compost2.ui.screens.HomeViewModel
// ВАЖНО: Импортируем нашу настроенную тему из пакета theme
import com.example.compost2.ui.theme.ComPostTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            // Создаем ViewModel
            val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.provideFactory(context))

            // Получаем состояние темы
            val darkTheme = homeViewModel.isDarkTheme

            // Используем ComPostTheme из файла Theme.kt (где подключен Montserrat),
            // а не локальную заглушку, которая была здесь раньше.
            ComPostTheme(darkTheme = darkTheme) {
                AppNavigation()
            }
        }
    }
}

// УДАЛЕНО: Функция ComPostTheme, которая была здесь внизу.
// Она перекрывала наши настройки и сбрасывала шрифт на дефолтный.