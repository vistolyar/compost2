package com.example.compost2.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RecorderScreen(onNavigateToHome: () -> Unit) {
    val context = LocalContext.current
    val viewModel: RecorderViewModel = viewModel(factory = RecorderViewModel.provideFactory(context))

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) viewModel.startCapture(context, false)
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier.align(Alignment.TopCenter).padding(top = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = if (viewModel.isRecording) "Запись идет..." else if (viewModel.hasRecordingSession) "На паузе" else "Готов к записи", color = if (viewModel.isRecording) Color.Red else Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = viewModel.formattedTime, fontSize = 60.sp, fontWeight = FontWeight.Bold)
        }

        if (viewModel.hasRecordingSession) {
            Button(
                onClick = { viewModel.finalizeRecording(context); onNavigateToHome() },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 30.dp, end = 20.dp).size(80.dp)
            ) { Icon(Icons.Default.Stop, null, modifier = Modifier.size(48.dp)) }
        }

        Box(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 130.dp).size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            // ВОССТАНОВЛЕННАЯ ФОРМУЛА: Реакция на амплитуду (громкость)
            val targetOuterScale = if (viewModel.isRecording) {
                1f + (viewModel.currentAmplitude / 15000f).coerceIn(0f, 0.3f)
            } else {
                1f
            }
            val animScale by animateFloatAsState(targetValue = targetOuterScale, label = "OuterPulse")

            Box(modifier = Modifier.fillMaxSize().scale(animScale).border(6.dp, Color.Red, CircleShape))

            Box(
                modifier = Modifier.fillMaxSize(0.85f).clip(CircleShape)
                    .background(Color.Red.copy(alpha = if (viewModel.isRecording) 0.8f else 0.2f))
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            val startTime = System.currentTimeMillis()

                            if (viewModel.isRecording) {
                                viewModel.pauseCapture(context)
                            } else {
                                viewModel.startCapture(context, false)
                            }

                            val up = waitForUpOrCancellation()
                            if (up != null) {
                                val duration = System.currentTimeMillis() - startTime
                                if (duration > 400 && viewModel.isRecording) {
                                    viewModel.pauseCapture(context)
                                } else if (duration <= 400 && viewModel.isRecording) {
                                    viewModel.isDictaphoneMode = true
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.isRecording && viewModel.isDictaphoneMode) Icon(Icons.Default.Pause, null, tint = Color.White, modifier = Modifier.size(64.dp))
            }
        }
    }
}