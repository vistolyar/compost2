package com.example.compost2.ui.components

import android.graphics.CornerPathEffect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RoundedPentagonBox(
    modifier: Modifier = Modifier,
    color: Color,
    cornerRadius: Dp = 4.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Создаем путь (Pentagon)
            val path = Path().apply {
                moveTo(w * 0.50f, h * 0.05f) // Top
                lineTo(w * 0.95f, h * 0.38f) // Right Top
                lineTo(w * 0.78f, h * 0.92f) // Right Bottom
                lineTo(w * 0.22f, h * 0.92f) // Left Bottom
                lineTo(w * 0.05f, h * 0.38f) // Left Top
                close()
            }

            // Используем Native Canvas для CornerPathEffect (скругление углов заливки)
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    this.color = color.toArgb()
                    this.style = android.graphics.Paint.Style.FILL
                    this.isAntiAlias = true
                    // Магия скругления углов
                    this.pathEffect = CornerPathEffect(cornerRadius.toPx())
                }
                canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
            }
        }

        // Контент (иконка)
        content()
    }
}