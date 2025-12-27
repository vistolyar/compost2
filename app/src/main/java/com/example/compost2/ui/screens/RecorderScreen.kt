package com.example.compost2.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RecorderScreen(onNavigateToHome: () -> Unit) {
    val context = LocalContext.current
    val viewModel: RecorderViewModel = viewModel(factory = RecorderViewModel.provideFactory(context))

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Верхняя панель (Таймер и статус)
        Column(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (viewModel.isRecording) "Запись идет..." else if (viewModel.hasRecordingSession) "На паузе" else "Готов к записи",
                style = MaterialTheme.typography.titleMedium,
                color = if (viewModel.isRecording) Color.Red else Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = viewModel.formattedTime,
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // КНОПКА "СТОП" УДАЛЕНА. ВСЁ УПРАВЛЕНИЕ ТЕПЕРЬ ТУТ:
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(280.dp), // Сделал чуть крупнее для удобства
            contentAlignment = Alignment.Center
        ) {
            // Внешнее пульсирующее кольцо (реагирует на звук)
            val targetScale = if (viewModel.isRecording) {
                1f + (viewModel.currentAmplitude / 15000f).coerceIn(0f, 0.4f)
            } else 1f
            val animScale by animateFloatAsState(targetValue = targetScale, label = "Pulse")

            Box(modifier = Modifier.fillMaxSize().scale(animScale).border(8.dp, Color.Red, CircleShape))

            // Внутренняя кнопка
            Box(
                modifier = Modifier
                    .fillMaxSize(0.85f).clip(CircleShape)
                    .background(Color.Red.copy(alpha = if (viewModel.isRecording) 0.8f else 0.2f))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (viewModel.hasRecordingSession) {
                                    viewModel.finalizeRecording(context)
                                    onNavigateToHome()
                                }
                            },
                            onTap = {
                                if (viewModel.isRecording) {
                                    viewModel.pauseCapture(context)
                                } else {
                                    // Обычный тап запускает режим "Диктофон" (dictaphone = true)
                                    viewModel.startCapture(context, true)
                                }
                            },
                            onLongPress = {
                                if (!viewModel.isRecording) {
                                    // Длинный тап запускает режим "Рация" (dictaphone = false)
                                    viewModel.startCapture(context, false)
                                }
                            },
                            onPress = {
                                val pressStartTime = System.currentTimeMillis()
                                tryAwaitRelease()
                                val duration = System.currentTimeMillis() - pressStartTime

                                // Если это была "Рация" (держали > 500мс) - ставим на паузу при отпускании
                                if (duration > 500 && viewModel.isRecording && !viewModel.isDictaphoneMode) {
                                    viewModel.pauseCapture(context)
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Если на паузе — показываем иконку паузы в центре
                if (!viewModel.isRecording && viewModel.hasRecordingSession) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Paused",
                        tint = Color.Red,
                        modifier = Modifier.size(100.dp)
                    )
                }
            }
        }

        // Инструкция внизу
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Double tap to STOP",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Red
            )
            Text(
                text = "Hold for Walkie-Talkie, Tap for Dictaphone",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}