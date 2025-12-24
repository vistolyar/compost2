package com.example.compost2.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compost2.ui.components.ControlState
import com.example.compost2.ui.components.PolyhedronControl

@Composable
fun RecorderScreen(onNavigateToHome: () -> Unit) {
    val context = LocalContext.current
    val viewModel: RecorderViewModel = viewModel(factory = RecorderViewModel.provideFactory(context))

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Таймер и статус
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

        // Центральная фигура
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .aspectRatio(1f)
                .pointerInput(viewModel.controlState) {
                    if (viewModel.controlState == ControlState.RECORDING) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (viewModel.hasRecordingSession) viewModel.finalizeRecording()
                            },
                            onPress = {
                                val startTime = System.currentTimeMillis()
                                val wasAlreadyRecording = viewModel.isRecording

                                if (wasAlreadyRecording) {
                                    viewModel.pauseCapture()
                                } else {
                                    viewModel.startCapture(context, isDictaphone = false)
                                    val released = tryAwaitRelease()
                                    if (released) {
                                        val duration = System.currentTimeMillis() - startTime
                                        if (duration > 500) {
                                            viewModel.pauseCapture()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
        ) {
            PolyhedronControl(
                isRecording = viewModel.isRecording,
                isPaused = viewModel.hasRecordingSession && !viewModel.isRecording,
                amplitude = viewModel.currentAmplitude,
                state = viewModel.controlState,
                prompts = viewModel.prompts,
                onPromptSelected = { }
            )
        }

        // Подсказки
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (viewModel.controlState == ControlState.RECORDING)
                    "Double tap to stop. Hold or tap for pause"
                else "Tap to choose prompt",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            if (viewModel.controlState == ControlState.SELECTION) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "← Back to recording",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { viewModel.resetToRecording() }
                        .padding(12.dp)
                )
            }
        }
    }
}
