package com.example.compost2.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Pause
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RecorderScreen(
    onNavigateToHome: () -> Unit // Колбэк для возврата на главный экран
) {
    val context = LocalContext.current
    val viewModel: RecorderViewModel = viewModel(
        factory = RecorderViewModel.provideFactory(context)
    )

    // Лаунчер для разрешений
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Если дали разрешение, начинаем как "Рация" (безопасный старт), потом разберемся
            viewModel.startCapture(context, isDictaphone = false)
        } else {
            Toast.makeText(context, "Нужно разрешение на микрофон", Toast.LENGTH_SHORT).show()
        }
    }

    // Функция проверки прав и старта
    val checkPermissionAndStart = { isDictaphone: Boolean ->
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            viewModel.startCapture(context, isDictaphone)
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

            // Таймер с десятыми долями
            Text(
                text = viewModel.formattedTime,
                fontSize = 60.sp, // Чуть уменьшил шрифт, чтобы влезли миллисекунды
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

        // --- Кнопка СТОП (справа) ---
        if (viewModel.hasRecordingSession) {
            Button(
                onClick = {
                    viewModel.finalizeRecording()
                    onNavigateToHome() // Уходим на главный экран
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 30.dp, end = 20.dp)
                    .size(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop and Save",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // --- БОЛЬШАЯ КРАСНАЯ КНОПКА ---
        BigRecordButton(
            isRecording = viewModel.isRecording,
            isDictaphoneMode = viewModel.isDictaphoneMode,
            amplitude = viewModel.currentAmplitude,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 130.dp)
                .size(240.dp),
            onStartCapture = { isDictaphone -> checkPermissionAndStart(isDictaphone) },
            onSwitchToDictaphone = { viewModel.enableDictaphoneMode() }, // Переключаем режим на лету
            onPauseCapture = { viewModel.pauseCapture() }
        )
    }
}

@Composable
fun BigRecordButton(
    isRecording: Boolean,
    isDictaphoneMode: Boolean,
    amplitude: Int,
    modifier: Modifier = Modifier,
    onStartCapture: (Boolean) -> Unit,
    onSwitchToDictaphone: () -> Unit,
    onPauseCapture: () -> Unit
) {
    // Анимация ВНЕШНЕГО кольца (расширение от громкости)
    // Чем громче, тем больше кольцо (до 1.3x)
    val targetOuterScale = if (isRecording) {
        1f + (amplitude / 15000f).coerceIn(0f, 0.3f)
    } else {
        1f
    }
    val animatedOuterScale by animateFloatAsState(targetValue = targetOuterScale, label = "OuterPulse")

    // Анимация прозрачности иконки Паузы (пульсация раз в 2 секунды)
    val infiniteTransition = rememberInfiniteTransition(label = "PauseBreathing")
    val pauseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000), // 1 секунда туда, 1 секунда обратно = 2 сек
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    var pressStartTime by remember { mutableLongStateOf(0L) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 1. ВНЕШНЕЕ КОЛЬЦО (Красный ободок) - теперь оно пульсирует
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(animatedOuterScale) // <-- Анимация здесь
                .border(6.dp, Color.Red, CircleShape)
        )

        // 2. ВНУТРЕННИЙ КРУГ (Заливка) - Статичный размер
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                // Убрали scale(animatedScale) отсюда
                .clip(CircleShape)
                .background(Color.Red.copy(alpha = if (isRecording) 0.8f else 0.2f))
            , contentAlignment = Alignment.Center) {

            // 3. ИКОНКА ПАУЗЫ
            // Показываем только если: Запись идет И режим Диктофона
            if (isRecording && isDictaphoneMode) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = Color.White,
                    modifier = Modifier
                        .size(64.dp)
                        .alpha(pauseAlpha) // <-- Плавная пульсация
                )
            }
        }

        // 4. ЖЕСТЫ (Невидимый слой сверху)
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
                                // Начинаем запись как "Рация" (false) по умолчанию.
                                // Если отпустят быстро - переключим в Диктофон.
                                onStartCapture(false)
                            }

                            tryAwaitRelease()

                            // --- ПАЛЕЦ ОТПУЩЕН ---
                            val pressDuration = System.currentTimeMillis() - pressStartTime

                            if (!wasRecordingInitially) {
                                if (pressDuration < 500) {
                                    // Короткий тап (< 0.5 сек) -> Переключаем в режим Диктофона!
                                    onSwitchToDictaphone()
                                } else {
                                    // Долгое удержание -> Это была Рация -> Пауза
                                    onPauseCapture()
                                }
                            }
                        }
                    )
                }
                // Отдельный обработчик для остановки тапом, если запись уже шла
                .pointerInput(isRecording) {
                    if (isRecording) {
                        detectTapGestures(
                            onTap = {
                                onPauseCapture()
                            }
                        )
                    }
                }
        )
    }
}