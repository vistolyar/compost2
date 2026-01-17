package com.example.compost2.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compost2.ui.components.ActionLensButton
// Убрали импорт MontserratFontFamily, он теперь должен приходить из темы
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun RecorderScreen(onNavigateToHome: () -> Unit) {
    val context = LocalContext.current
    val viewModel: RecorderViewModel = viewModel(factory = RecorderViewModel.provideFactory(context))

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val cancelThresholdPx = with(density) { 60.dp.toPx() }
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val backGestureThresholdPx = screenWidthPx * 0.4f

    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isCancelUIActive by remember { mutableStateOf(false) }
    var cancelProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isCancelUIActive) {
        if (isCancelUIActive) {
            val startTime = System.currentTimeMillis()
            val duration = 1000L
            while (isActive && cancelProgress < 1f) {
                val elapsed = System.currentTimeMillis() - startTime
                cancelProgress = (elapsed / duration.toFloat()).coerceIn(0f, 1f)
                delay(16)
            }
            if (cancelProgress >= 1f) {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    v.vibrate(50)
                }
                if (viewModel.isRecording) viewModel.pauseCapture(context)
                onNavigateToHome()
            }
        } else {
            cancelProgress = 0f
        }
    }

    LaunchedEffect(viewModel.isReadyForSTT) {
        if (viewModel.isReadyForSTT) onNavigateToHome()
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) viewModel.startCapture(context, false)
    }

    val checkPermissionAndStart = { dictaphone: Boolean ->
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            viewModel.startCapture(context, dictaphone)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { dragOffset = 0f; isCancelUIActive = false },
                    onDragEnd = {
                        if (!viewModel.hasRecordingSession && dragOffset > backGestureThresholdPx) onNavigateToHome()
                        dragOffset = 0f; isCancelUIActive = false; cancelProgress = 0f
                    },
                    onDragCancel = { dragOffset = 0f; isCancelUIActive = false; cancelProgress = 0f }
                ) { change, dragAmount ->
                    change.consume()
                    dragOffset += dragAmount.x
                    if (viewModel.hasRecordingSession) {
                        if (dragOffset > cancelThresholdPx) { if (!isCancelUIActive) isCancelUIActive = true }
                        else { if (isCancelUIActive) isCancelUIActive = false }
                    }
                }
            }
    ) {
        // ТАЙМЕР
        Column(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ИСПРАВЛЕНИЕ: Убрали явный fontFamily. Теперь берется displayLarge из Theme -> Type.kt
            Text(
                text = viewModel.formattedTime,
                style = MaterialTheme.typography.displayLarge,
                color = Color.White
            )
            Text(
                text = if (viewModel.controlState == ActionState.RECORDING) "RECORDING"
                else if (viewModel.hasRecordingSession) "PAUSED" else "READY",
                style = MaterialTheme.typography.labelLarge,
                color = if (viewModel.isRecording) Color.Red else Color.White.copy(alpha = 0.5f),
                letterSpacing = 4.sp
            )
        }

        AnimatedVisibility(
            visible = !isCancelUIActive,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(360.dp)
                    .pointerInput(viewModel.controlState) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (viewModel.controlState == ActionState.RECORDING) viewModel.stopForSelection(context)
                                else if (viewModel.controlState == ActionState.SELECTION) viewModel.startCapture(context, true)
                            },
                            onTap = {
                                if (viewModel.controlState != ActionState.SELECTION) {
                                    if (viewModel.isRecording) viewModel.pauseCapture(context)
                                    else checkPermissionAndStart(true)
                                }
                            },
                            onLongPress = {
                                if (viewModel.controlState != ActionState.SELECTION && !viewModel.isRecording) checkPermissionAndStart(false)
                            },
                            onPress = {
                                val startTime = System.currentTimeMillis()
                                tryAwaitRelease()
                                if (System.currentTimeMillis() - startTime > 500 && viewModel.isRecording && !viewModel.isDictaphoneMode) viewModel.pauseCapture(context)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                ActionLensButton(
                    state = viewModel.controlState,
                    prompts = viewModel.prompts,
                    amplitude = viewModel.currentAmplitude,
                    onResume = { viewModel.startCapture(context, true) },
                    onSelect = { prompt -> viewModel.onPromptSelected(context, prompt) }
                )
            }
        }

        AnimatedVisibility(
            visible = !isCancelUIActive,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp),
            exit = fadeOut()
        ) {
            Text(
                text = if (viewModel.controlState == ActionState.SELECTION) "CHOOSE ACTION TO FINISH"
                else if (viewModel.hasRecordingSession) "SWIPE RIGHT TO CANCEL"
                else "DOUBLE TAP TO OPEN LENS",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.3f),
                letterSpacing = 1.sp
            )
        }

        if (viewModel.hasRecordingSession) {
            CancelWidget(
                isVisible = isCancelUIActive,
                progress = cancelProgress,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 40.dp)
            )
        }
    }
}

@Composable
fun CancelWidget(isVisible: Boolean, progress: Float, modifier: Modifier = Modifier) {
    val errorColor = MaterialTheme.colorScheme.error
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it }),
        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it }),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(70.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = Color.White.copy(alpha = 0.1f), style = Stroke(width = 4.dp.toPx()))
                    drawArc(color = errorColor, startAngle = -90f, sweepAngle = progress * 360f, useCenter = false, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
                }
                Text(text = "✕", fontSize = 24.sp, color = errorColor, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(text = "cancel", color = errorColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = if (progress < 1f) "HOLD 2s" else "DONE", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}