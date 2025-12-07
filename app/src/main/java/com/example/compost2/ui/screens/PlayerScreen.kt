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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.random.Random

@Composable
fun PlayerScreen(
    fileName: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: PlayerViewModel = viewModel(factory = PlayerViewModel.provideFactory(context))

    LaunchedEffect(fileName) {
        viewModel.loadFile(fileName)
    }

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
                    text = "Voice record",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = viewModel.fileName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- 2. ОСЦИЛЛОГРАММА ---
        val waveColor = MaterialTheme.colorScheme.primary

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clipToBounds()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            ScrollableWaveform(
                progress = viewModel.sliderPosition,
                totalDurationMillis = if (viewModel.totalDurationMillis > 0) viewModel.totalDurationMillis else 1000L,
                color = waveColor
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- 3. Слайдер и Тайм-коды ---
        Column(modifier = Modifier.fillMaxWidth()) {

            Slider(
                value = viewModel.sliderPosition,
                onValueChange = { viewModel.seekTo(it) },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = viewModel.formatTime(viewModel.currentPositionMillis),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = viewModel.formatTime(viewModel.totalDurationMillis),
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
                onClick = { viewModel.changeSpeed() },
                shape = CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Text("${viewModel.currentSpeed}x")
            }

            // Play / Pause
            IconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    imageVector = if (viewModel.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }

            // Кнопка Replay (ПЕРЕМОТКА В НАЧАЛО)
            IconButton(onClick = { viewModel.seekTo(0f) }) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Replay",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun ScrollableWaveform(
    progress: Float,
    totalDurationMillis: Long,
    color: Color
) {
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