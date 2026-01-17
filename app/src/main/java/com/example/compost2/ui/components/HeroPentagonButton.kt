package com.example.compost2.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun HeroPentagonButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Размеры из дизайна (72px)
    val size = 72.dp

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Убираем стандартную рябь
                onClick = onClick
            )
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // 1. Рисуем Пятиугольник (Контур)
            // HTML Path: M50 5 L95 38 L78 92 L22 92 L5 38 Z
            val path = Path().apply {
                moveTo(w * 0.50f, h * 0.05f)
                lineTo(w * 0.95f, h * 0.38f)
                lineTo(w * 0.78f, h * 0.92f)
                lineTo(w * 0.22f, h * 0.92f)
                lineTo(w * 0.05f, h * 0.38f)
                close()
            }

            drawPath(
                path = path,
                color = Color.Black, // ИСПРАВЛЕНО: TextBlack -> Color.Black
                style = Stroke(
                    width = 6.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )

            // 2. Рисуем Плюсик внутри
            // Горизонтальная
            drawLine(
                color = Color.Black, // ИСПРАВЛЕНО
                start = Offset(w * 0.35f, h * 0.53f),
                end = Offset(w * 0.65f, h * 0.53f),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Вертикальная
            drawLine(
                color = Color.Black, // ИСПРАВЛЕНО
                start = Offset(w * 0.50f, h * 0.38f),
                end = Offset(w * 0.50f, h * 0.68f),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}