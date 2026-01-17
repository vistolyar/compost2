package com.example.compost2.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppColorScheme = lightColorScheme(
    primary = AppPrimary,
    onPrimary = AppWhite,
    background = AppScreenBg,

    // ИСПРАВЛЕНИЕ: surface теперь белый (для Меню, Дроверов, Диалогов)
    // Карточки мы покрасим отдельно в их компоненте.
    surface = AppWhite,

    onSurface = AppTextPrimary,
    error = AppDanger,
    outline = AppInactive
)

@Composable
fun ComPostTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AppScreenBg.toArgb()
            window.navigationBarColor = AppScreenBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = AppTypography,
        content = content
    )
}