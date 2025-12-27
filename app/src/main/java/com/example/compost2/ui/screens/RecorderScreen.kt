package com.example.compost2.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compost2.ui.components.ActionLensButton

@Composable
fun RecorderScreen(onNavigateToHome: () -> Unit) {
    val context = LocalContext.current
    val viewModel: RecorderViewModel = viewModel(factory = RecorderViewModel.provideFactory(context))

    LaunchedEffect(viewModel.isReadyForSTT) {
        if (viewModel.isReadyForSTT) {
            onNavigateToHome()
        }
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

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = viewModel.formattedTime,
                fontSize = 72.sp,
                fontWeight = FontWeight.W900,
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

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(360.dp)
                .pointerInput(viewModel.controlState) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (viewModel.controlState == ActionState.RECORDING) {
                                viewModel.stopForSelection(context)
                            } else if (viewModel.controlState == ActionState.SELECTION) {
                                viewModel.startCapture(context, true)
                            }
                        },
                        onTap = {
                            if (viewModel.controlState != ActionState.SELECTION) {
                                if (viewModel.isRecording) viewModel.pauseCapture(context)
                                else checkPermissionAndStart(true)
                            }
                        },
                        onLongPress = {
                            if (viewModel.controlState != ActionState.SELECTION && !viewModel.isRecording) {
                                checkPermissionAndStart(false)
                            }
                        },
                        onPress = {
                            val startTime = System.currentTimeMillis()
                            tryAwaitRelease()
                            val duration = System.currentTimeMillis() - startTime
                            if (duration > 500 && viewModel.isRecording && !viewModel.isDictaphoneMode) {
                                viewModel.pauseCapture(context)
                            }
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
                onSelect = { prompt ->
                    viewModel.onPromptSelected(context, prompt)
                }
            )
        }

        Text(
            text = if (viewModel.controlState == ActionState.SELECTION)
                "CHOOSE ACTION TO FINISH"
            else "DOUBLE TAP TO OPEN LENS",
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.3f),
            letterSpacing = 1.sp
        )
    }
}