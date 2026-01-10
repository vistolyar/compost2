package com.example.compost2.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color // <--- ВОТ ЭТОГО НЕ ХВАТАЛО
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Дизайн преимущественно светлый (Dashboard), кроме Рекордера
private val LightColorScheme = lightColorScheme(
    primary = ActionPurple,
    onPrimary = TextWhite,
    background = BgDashboard,
    surface = TextWhite,
    onSurface = TextBlack,
    surfaceVariant = Color(0xFFE0E0E5), // Для серых плашек
    error = StatusError
)

@Composable
fun ComPostTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Пока форсируем светлую тему для Дашборда, так как дизайн фиксированный
    // (Рекордер будет сам перекрашиваться в черный)
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Статус бар прозрачный, иконки темные
            window.statusBarColor = BgDashboard.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}