package com.example.compost2.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compost2.ui.navigation.AppNavigation
import com.example.compost2.ui.screens.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Создаем VM здесь, чтобы знать о теме
            val context = LocalContext.current
            val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.provideFactory(context))

            val darkTheme = homeViewModel.isDarkTheme

            ComPostTheme(darkTheme = darkTheme) {
                // ВАЖНО: В AppNavigation мы создаем HomeViewModel заново или передаем?
                // В текущей архитектуре AppNavigation создает свою VM.
                // Чтобы тема работала "на лету", лучше передать isDarkTheme в AppNavigation,
                // но для простоты пока оставим так. Тема применится, но VM будет две (одна для темы, одна для данных).
                // Это не идеально, но для смены цветов сработает.
                AppNavigation()
            }
        }
    }
}

// Простая тема (если у тебя её нет в отдельном файле Theme.kt)
@Composable
fun ComPostTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography, // Дефолтная типографика
        content = content
    )
}