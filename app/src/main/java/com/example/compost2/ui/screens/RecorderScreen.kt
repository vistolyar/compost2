package com.example.compost2.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RecorderScreen() {
    val context = LocalContext.current
    val viewModel: RecorderViewModel = viewModel(
        factory = RecorderViewModel.provideFactory(context)
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.startCapture(context)
        } else {
            Toast.makeText(context, "Нужно разрешение на микрофон", Toast.LENGTH_SHORT).show()
        }
    }

    val checkPermissionAndStart = {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            viewModel.startCapture(context)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- Верхняя часть: Статус и Таймер ---
        Column(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (viewModel.isRecording) "Запись идет..." else "Готов к записи",
                style = MaterialTheme.typography.titleMedium,
                color = if (viewModel.isRecording) Color.Red else Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = viewModel.formattedTime,
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.displayLarge
            )

            if (viewModel.hasRecordingSession && !viewModel.isRecording) {
                Text(
                    text = "На паузе",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // --- Кнопка СТОП ---
        if (viewModel.hasRecordingSession) {
            Button(
                onClick = { viewModel.finalizeRecording() },
                shape = RoundedCornerShape(16.dp), // Чуть более скругленный квадрат
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    // Опускаем кнопку стоп пониже (30.dp от низа), чтобы она была под большой кнопкой
                    .padding(bottom = 30.dp, end = 20.dp)
                    .size(80.dp) // Увеличили саму кнопку (было 64)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop and Save",
                    // Увеличили сам квадрат внутри до 48dp (было по умолчанию 24dp)
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // --- БОЛЬШАЯ КРАСНАЯ КНОПКА ---
        BigRecordButton(
            isRecording = viewModel.isRecording,
            amplitude = viewModel.currentAmplitude,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                // Подняли кнопку выше (padding bottom = 130.dp), чтобы освободить место для кнопки Стоп
                .padding(bottom = 130.dp)
                .size(240.dp),
            onStartRecording = { checkPermissionAndStart() },
            onPauseRecording = { viewModel.pauseCapture() }
        )
    }
}

@Composable
fun BigRecordButton(
    isRecording: Boolean,
    amplitude: Int,
    modifier: Modifier = Modifier,
    onStartRecording: () -> Unit,
    onPauseRecording: () -> Unit
) {
    val targetScale = if (isRecording) {
        1f + (amplitude / 10000f).coerceIn(0f, 0.5f)
    } else {
        1f
    }

    val animatedScale by animateFloatAsState(targetValue = targetScale, label = "Pulse")

    var pressStartTime by remember { mutableLongStateOf(0L) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Ободок
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(6.dp, Color.Red, CircleShape)
        )

        // Пульсирующий круг
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .scale(animatedScale)
                .clip(CircleShape)
                .background(Color.Red.copy(alpha = if (isRecording) 0.8f else 0.2f))
        )

        // Жесты
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            pressStartTime = System.currentTimeMillis()
                            val wasRecordingInitially = isRecording

                            if (!wasRecordingInitially) {
                                onStartRecording()
                            }

                            tryAwaitRelease()

                            val pressDuration = System.currentTimeMillis() - pressStartTime

                            if (!wasRecordingInitially) {
                                if (pressDuration > 500) {
                                    onPauseRecording()
                                }
                            }
                        }
                    )
                }
                .pointerInput(isRecording) {
                    if (isRecording) {
                        detectTapGestures(
                            onTap = {
                                onPauseRecording()
                            }
                        )
                    }
                }
        )
    }
}