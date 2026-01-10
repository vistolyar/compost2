package com.example.compost2.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ВРЕМЕННО: Используем системный шрифт, чтобы приложение собралось без файлов ресурсов.
// Позже, когда ты добавишь файлы .ttf в папку res/font, мы вернем сюда Montserrat.
val Montserrat = FontFamily.Default

val Typography = Typography(
    // Заголовки (как в .app-title)
    headlineSmall = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Black,
        fontSize = 20.sp,
        letterSpacing = (-0.5).sp,
        color = TextBlack
    ),
    // Заголовки карточек
    titleMedium = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        color = TextBlack
    ),
    // Обычный текст
    bodyLarge = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 24.sp,
        color = TextBlack
    ),
    // Мелкие подписи (статусы)
    labelSmall = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 1.sp
    )
)