package com.example.compost2.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compost2.domain.IntegrationType
import com.example.compost2.ui.components.ActionLensButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    fileName: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: DetailViewModel = viewModel(factory = DetailViewModel.provideFactory(context))

    LaunchedEffect(fileName) {
        viewModel.loadItem(fileName)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (viewModel.isBusy) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 16.dp).size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                )
            },
            bottomBar = {
                // НИЖНЯЯ ПАНЕЛЬ ДЕЙСТВИЙ (ФИКСИРОВАННАЯ)
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 1. CHOOSE PROMPT (Линза)
                        IconButton(onClick = { viewModel.onOpenLens() }) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI Actions", tint = MaterialTheme.colorScheme.primary)
                        }

                        // 2. RE-TRANSCRIBE
                        IconButton(onClick = { viewModel.reTranscribe() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Re-Transcribe")
                        }

                        // 3. COPY TEXT
                        IconButton(onClick = { viewModel.copyToClipboard() }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // 1. КОМПАКТНЫЙ ПЛЕЕР
                AudioPlayerCompact(
                    isPlaying = viewModel.isPlaying,
                    sliderPos = viewModel.sliderPosition,
                    duration = viewModel.totalDuration,
                    currentPos = viewModel.currentPosition,
                    onTogglePlay = { viewModel.togglePlay() },
                    onSeek = { viewModel.seekTo(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 2. ПАНЕЛЬ ИНТЕГРАЦИЙ
                Text("Integrations", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IntegrationButton(
                        icon = Icons.Default.DateRange,
                        label = "Calendar",
                        isActive = false,
                        onClick = { viewModel.triggerIntegration(IntegrationType.CALENDAR) }
                    )
                    IntegrationButton(
                        icon = Icons.Default.Email,
                        label = "Gmail",
                        isActive = false,
                        onClick = { viewModel.triggerIntegration(IntegrationType.GMAIL) }
                    )
                    IntegrationButton(
                        icon = Icons.Default.Public,
                        label = "Web",
                        isActive = viewModel.item?.wordpressId != null,
                        onClick = { viewModel.triggerIntegration(IntegrationType.WORDPRESS) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(24.dp))

                // 3. ТЕКСТ (Показываем только если он есть)
                if (viewModel.content.isNotBlank()) {
                    OutlinedTextField(
                        value = viewModel.title,
                        onValueChange = { viewModel.updateContent(it, viewModel.content) },
                        label = { Text("Title") },
                        textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = viewModel.content,
                        onValueChange = { viewModel.updateContent(viewModel.title, it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                } else {
                    // ЗАГЛУШКА, ЕСЛИ ТЕКСТА НЕТ
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.GraphicEq, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No transcript available.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                            Text(
                                "Tap 'Refresh' below to transcribe.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // 4. ACTION LENS OVERLAY
        AnimatedVisibility(
            visible = viewModel.showLens,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable { viewModel.showLens = false },
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(360.dp)) {
                    ActionLensButton(
                        state = ActionState.SELECTION,
                        prompts = viewModel.prompts,
                        amplitude = 0,
                        onResume = { viewModel.showLens = false },
                        onSelect = { prompt -> viewModel.onPromptSelected(prompt) }
                    )
                }
            }
        }
    }
}

@Composable
fun AudioPlayerCompact(
    isPlaying: Boolean,
    sliderPos: Float,
    duration: Long,
    currentPos: Long,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        IconButton(
            onClick = onTogglePlay,
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Slider(
            value = sliderPos,
            onValueChange = onSeek,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun IntegrationButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val color = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}