package com.example.compost2.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.random.Random

@Composable
fun PlayerScreen(
    onNavigateBack: () -> Unit
) {
    // --- Состояния (Dummy Data) ---
    var isPlaying by remember { mutableStateOf(false) }
    var speed by remember { mutableFloatStateOf(1f) } // 1x, 2x, 3x

    // Эмуляция длинной записи (например, 20 минут 21 секунда)
    val totalDurationMillis = 20 * 60 * 1000L + 21 * 1000L

    // Текущий прогресс (0.0 - 1.0)
    var sliderPosition by remember { mutableFloatStateOf(0.0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // --- 1. Верхняя панель (Header) ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Voice record: 172422112025",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Status: Saved",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50) // Material Green
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- 2. ОСЦИЛЛОГРАММА (Scrolling Waveform) ---
        val waveColor = MaterialTheme.colorScheme.primary

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clipToBounds() // Обрезаем всё, что вылазит за границы экрана
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            ScrollableWaveform(
                progress = sliderPosition,
                totalDurationMillis = totalDurationMillis,
                color = waveColor
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- 3. Слайдер и Тайм-коды ---
        Column(modifier = Modifier.fillMaxWidth()) {

            // Слайдер (с кружком-маркером)
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Тайм-коды под слайдером
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Текущее время (слева)
                val currentMillis = (totalDurationMillis * sliderPosition).toLong()
                Text(
                    text = formatPlayerTime(currentMillis),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Общее время (справа)
                Text(
                    text = formatPlayerTime(totalDurationMillis),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 4. Кнопки управления ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Кнопка скорости
            OutlinedButton(
                onClick = {
                    speed = when (speed) {
                        1f -> 2f
                        2f -> 3f
                        else -> 1f
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Text("${speed.toInt()}x")
            }

            // Play / Pause (Большая кнопка)
            IconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }

            // Кнопка Resend
            IconButton(onClick = { /* Resend Logic */ }) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Resend",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

// --- Хелперы ---

@Composable
fun ScrollableWaveform(
    progress: Float,
    totalDurationMillis: Long,
    color: Color
) {
    // Окно видимости = 5 минут (300 000 мс)
    val visibleWindowMillis = 5 * 60 * 1000L

    val barsCount = (totalDurationMillis / 500).toInt().coerceAtLeast(10)
    val bars = remember(totalDurationMillis) { List(barsCount) { Random.nextFloat() } }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val contentWidth = if (totalDurationMillis <= visibleWindowMillis) {
            canvasWidth
        } else {
            canvasWidth * (totalDurationMillis.toFloat() / visibleWindowMillis)
        }

        val maxScroll = contentWidth - canvasWidth
        val scrollOffset = if (maxScroll > 0) -progress * maxScroll else 0f

        translate(left = scrollOffset) {
            val barWidth = contentWidth / (bars.size * 1.5f)
            val gap = barWidth / 2
            val startX = 0f

            bars.forEachIndexed { index, heightCoef ->
                val x = startX + index * (barWidth + gap)
                val absoluteX = x + scrollOffset
                if (absoluteX < -barWidth || absoluteX > canvasWidth) return@forEachIndexed

                val barHeight = canvasHeight * (0.1f + heightCoef * 0.7f)
                val y = (canvasHeight - barHeight) / 2

                drawLine(
                    color = color,
                    start = Offset(x, y),
                    end = Offset(x, y + barHeight),
                    strokeWidth = barWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

fun formatPlayerTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}