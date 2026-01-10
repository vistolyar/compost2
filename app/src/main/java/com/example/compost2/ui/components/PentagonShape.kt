package com.example.compost2.ui.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class PentagonShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            // SVG Path из HTML: M50 5 L95 38 L78 92 L22 92 L5 38 Z
            // Конвертируем относительные координаты (0..100) в реальный размер (size.width, size.height)

            val w = size.width
            val h = size.height

            // Точки (проценты / 100)
            moveTo(w * 0.50f, h * 0.05f) // Top
            lineTo(w * 0.95f, h * 0.38f) // Right Top
            lineTo(w * 0.78f, h * 0.92f) // Right Bottom
            lineTo(w * 0.22f, h * 0.92f) // Left Bottom
            lineTo(w * 0.05f, h * 0.38f) // Left Top
            close()
        }
        return Outline.Generic(path)
    }
}