package com.example.compost2.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.compost2.R

// 1. ОПРЕДЕЛЕНИЕ СТАТИЧЕСКИХ ШРИФТОВ
// Мы прямо указываем файл для каждого веса.
val MontserratFontFamily = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_regular, FontWeight.W400),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_medium, FontWeight.W500),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_semibold, FontWeight.W600),
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_bold, FontWeight.W700),
    Font(R.font.montserrat_black, FontWeight.Black),
    Font(R.font.montserrat_black, FontWeight.W900)
)

// 2. БАЗОВЫЙ СТИЛЬ
private val BaseTextStyle = TextStyle(
    fontFamily = MontserratFontFamily,
    color = TextBlack
)

// 3. ТИПОГРАФИКА
val AppTypography = Typography(
    // КРУПНЫЕ ЭЛЕМЕНТЫ
    displayLarge = BaseTextStyle.copy(
        fontWeight = FontWeight.Black, fontSize = 57.sp, lineHeight = 64.sp
    ),
    displayMedium = BaseTextStyle.copy(
        fontWeight = FontWeight.Black, fontSize = 45.sp, lineHeight = 52.sp
    ),
    displaySmall = BaseTextStyle.copy(
        fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp
    ),

    // ЗАГОЛОВКИ ЭКРАНОВ
    headlineLarge = BaseTextStyle.copy(
        fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp
    ),
    headlineMedium = BaseTextStyle.copy(
        fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp
    ),
    headlineSmall = BaseTextStyle.copy(
        fontWeight = FontWeight.Black, fontSize = 24.sp, lineHeight = 32.sp
    ),

    // ЗАГОЛОВКИ КАРТОЧЕК
    titleLarge = BaseTextStyle.copy(
        fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp
    ),
    titleMedium = BaseTextStyle.copy(
        fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp
    ),
    titleSmall = BaseTextStyle.copy(
        fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp
    ),

    // ОСНОВНОЙ ТЕКСТ
    bodyLarge = BaseTextStyle.copy(
        fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp
    ),
    bodyMedium = BaseTextStyle.copy(
        fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp
    ),
    bodySmall = BaseTextStyle.copy(
        fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp
    ),

    // КНОПКИ
    labelLarge = BaseTextStyle.copy(
        fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp
    ),
    labelMedium = BaseTextStyle.copy(
        fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp
    ),
    labelSmall = BaseTextStyle.copy(
        fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 1.sp
    )
)