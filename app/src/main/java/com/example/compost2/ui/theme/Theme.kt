package com.example.compost2.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = ActionPurple,
    onPrimary = TextWhite,
    background = BgDashboard,
    surface = TextWhite,
    onSurface = TextBlack,
    surfaceVariant = Color(0xFFE0E0E5),
    error = StatusError
)

@Composable
fun ComPostTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BgDashboard.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    // ИСПРАВЛЕНИЕ: Убрали CompositionLocalProvider с ошибкой.
    // Теперь, когда Type.kt исправлен (добавлен style=Normal),
    // AppTypography должен подхватиться корректно.
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}