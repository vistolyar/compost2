package com.example.compost2.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.compost2.R

// 1. ОПРЕДЕЛЕНИЕ ШРИФТА (Mapping всех весов через XML)
// Используем XML драйвер, так как это самый надежный способ для Android
val MontserratFontFamily = FontFamily(
    Font(R.font.montserrat_family)
)

// 2. БАЗОВЫЙ СТИЛЬ
private val BaseTextStyle = TextStyle(
    fontFamily = MontserratFontFamily,
    // ИСПРАВЛЕНО: TextBlack -> AppTextPrimary (из новой палитры)
    color = AppTextPrimary
)

// 3. ФИНАЛЬНАЯ ТИПОГРАФИКА
val AppTypography = Typography(
    displayLarge = BaseTextStyle.copy(
        fontWeight = FontWeight.Black, fontSize = 57.sp, lineHeight = 64.sp
    ),
    displayMedium = BaseTextStyle.copy(
        fontWeight = FontWeight.ExtraBold, fontSize = 45.sp, lineHeight = 52.sp
    ),
    displaySmall = BaseTextStyle.copy(
        fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp
    ),

    headlineLarge = BaseTextStyle.copy(
        fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp
    ),
    headlineMedium = BaseTextStyle.copy(
        fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp
    ),
    headlineSmall = BaseTextStyle.copy(
        fontWeight = FontWeight.Black, fontSize = 24.sp, lineHeight = 32.sp
    ),

    titleLarge = BaseTextStyle.copy(
        fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp
    ),
    titleMedium = BaseTextStyle.copy(
        fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp
    ),
    titleSmall = BaseTextStyle.copy(
        fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp
    ),

    bodyLarge = BaseTextStyle.copy(
        fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp
    ),
    bodyMedium = BaseTextStyle.copy(
        fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp
    ),
    bodySmall = BaseTextStyle.copy(
        fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp
    ),

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