package com.example.compost2.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compost2.domain.RecordingStatus
import com.example.compost2.ui.components.ActionLensButton
import com.example.compost2.ui.components.PlayerWidget
import com.example.compost2.ui.theme.* // Импортируем все цвета (AppPrimary и др.)
import kotlin.math.abs

const val LOREM_IPSUM = """
Lorem ipsum dolor sit amet...
"""

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {

        // 1. ВЕРХНИЙ БАР
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(60.dp)
                .background(Color.White)
                .padding(horizontal = 16.dp)
                .zIndex(10f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = viewModel.title.ifBlank { "New Project" },
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .basicMarquee(iterations = Int.MAX_VALUE, initialDelayMillis = 1000)
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (viewModel.hasRawText) {
                IconButton(onClick = { viewModel.restoreRawText() }) {
                    // ИСПРАВЛЕНО: ActionPurple -> AppPrimary
                    Icon(Icons.Default.History, contentDescription = "Restore", tint = AppPrimary)
                }
            }
        }

        // 2. ОСНОВНОЙ СКРОЛЛ
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            // --- СЕКЦИЯ ПЛЕЕРА ---
            item {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 25.dp, vertical = 10.dp)
                        .enableSwipeNavigation(
                            onSwipeRight = onNavigateBack,
                            onSwipeLeft = { viewModel.onOpenLens() }
                        )
                ) {
                    PlayerWidget(
                        fileName = viewModel.item?.name ?: "audio.m4a",
                        dateTimestamp = viewModel.recordingDateTimestamp,
                        duration = viewModel.formatDuration(viewModel.totalDuration),
                        currentPos = viewModel.formatDuration(viewModel.currentPosition),
                        isPlaying = viewModel.isPlaying,
                        progress = viewModel.sliderPosition,
                        status = viewModel.item?.status ?: RecordingStatus.SAVED,
                        onTogglePlay = { viewModel.togglePlay() },
                        onSeek = { viewModel.seekTo(it) },
                        onActionClick = { viewModel.onOpenLens() },
                        onStatusActionClick = { viewModel.onStatusAction() }
                    )
                }
            }

            // --- СЕКЦИЯ ИНТЕГРАЦИЙ ---
            item {
                val hasEditableText = viewModel.content.isNotBlank()
                if (hasEditableText) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 25.dp, vertical = 4.dp)
                            .enableSwipeNavigation(
                                onSwipeRight = onNavigateBack,
                                onSwipeLeft = { viewModel.onOpenLens() }
                            )
                    ) {
                        IntegrationsWidget(showTextIcon = true)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // --- STICKY HEADER ---
            stickyHeader {
                Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 25.dp)
                            .height(10.dp)
                            .border(1.dp, Color(0xFFF0F0F2), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                            .background(Color.White)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 25.dp)
                            .border(width = 1.dp, color = Color(0xFFF0F0F2))
                            .background(Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 25.dp, top = 15.dp, bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Text("TEXT", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                            Text("HTML", style = MaterialTheme.typography.labelSmall, color = Color.Black.copy(alpha = 0.3f))
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8F8FA))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(15.dp)
                        ) {
                            Icon(Icons.Default.FormatBold, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                            Icon(Icons.Default.FormatItalic, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ContentCopy, null, tint = Color.Gray, modifier = Modifier.size(18.dp).clickable { viewModel.copyToClipboard() })
                        }
                    }
                }
            }

            // --- ТЕКСТ ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp)
                        .border(1.dp, Color(0xFFF0F0F2))
                        .background(Color.White)
                        .padding(20.dp)
                ) {
                    if (viewModel.content.isNotBlank()) {
                        BasicTextField(
                            value = viewModel.content,
                            onValueChange = { viewModel.updateContent(viewModel.title, it) },
                            textStyle = TextStyle(
                                fontFamily = MontserratFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                lineHeight = 26.sp,
                                color = Color.Black
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("Ready to transcribe...", color = Color.LightGray, fontFamily = MontserratFontFamily)
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Button(
                            onClick = { viewModel.reTranscribe() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RE-TRANSCRIBE", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
            }
        }

        // 3. RAW TEXT MODAL
        AnimatedVisibility(
            visible = viewModel.showRawTextModal,
            enter = scaleIn(
                animationSpec = tween(300),
                transformOrigin = TransformOrigin(0.3f, 0.25f)
            ) + fadeIn(tween(300)),
            exit = scaleOut(
                animationSpec = tween(250),
                transformOrigin = TransformOrigin(0.3f, 0.25f)
            ) + fadeOut(tween(250)),
            modifier = Modifier.zIndex(50f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { viewModel.showRawTextModal = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 25.dp)
                        .fillMaxWidth()
                        .heightIn(max = 600.dp)
                        .clickable(enabled = false) {}
                        .enableSwipeToDismiss { viewModel.showRawTextModal = false },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        // ЗАГОЛОВОК
                        Text(
                            text = "Raw Transcription",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val scroll = rememberScrollState()
                        Text(
                            text = if (viewModel.rawText.isNotBlank()) viewModel.rawText else LOREM_IPSUM,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                            color = Color.DarkGray,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(scroll)
                                .padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // ФУТЕР
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Кнопка пересоздания (Фиолетовая -> AppPrimary)
                            Button(
                                onClick = {
                                    viewModel.reTranscribe()
                                    viewModel.showRawTextModal = false
                                },
                                modifier = Modifier.weight(1f),
                                // ИСПРАВЛЕНО: ActionPurple -> AppPrimary
                                colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
                            ) {
                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Re-transcribe", fontSize = 11.sp)
                            }

                            // Кнопка закрытия
                            Button(
                                onClick = { viewModel.showRawTextModal = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
                            ) {
                                Text("Close", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // 4. ACTION LENS
        AnimatedVisibility(
            visible = viewModel.showLens,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
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
                        state = com.example.compost2.ui.screens.ActionState.SELECTION,
                        prompts = viewModel.prompts,
                        amplitude = 0,
                        onResume = { viewModel.showLens = false },
                        onSelect = { prompt -> viewModel.onPromptSelected(prompt) }
                    )
                }
            }
        }

        if (viewModel.isBusy) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // ИСПРАВЛЕНО: ActionPurple -> AppPrimary
                CircularProgressIndicator(color = AppPrimary)
            }
        }
    }
}

// ВИДЖЕТ ИНТЕГРАЦИЙ
@Composable
fun IntegrationsWidget(showTextIcon: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F5)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("LINKED:", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.width(12.dp))
            if (showTextIcon) AppliedIntegrationBadge(Icons.Default.Description, Color.Black)
        }
    }
}

@Composable
fun AppliedIntegrationBadge(icon: ImageVector, color: Color) {
    Box(
        modifier = Modifier.size(28.dp).background(color.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
    }
}

// ХЕЛПЕРЫ ДЛЯ ЖЕСТОВ
private fun Modifier.enableSwipeNavigation(
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit
): Modifier = composed {
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 80.dp.toPx() }

    pointerInput(Unit) {
        detectHorizontalDragGestures(
            onDragStart = { dragOffset = 0f },
            onDragEnd = {
                if (dragOffset > swipeThreshold) onSwipeRight()
                else if (dragOffset < -swipeThreshold) onSwipeLeft()
                dragOffset = 0f
            },
            onDragCancel = { dragOffset = 0f }
        ) { _, dragAmount -> dragOffset += dragAmount }
    }
}

private fun Modifier.enableSwipeToDismiss(onDismiss: () -> Unit): Modifier = composed {
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 100.dp.toPx() }

    pointerInput(Unit) {
        detectHorizontalDragGestures(
            onDragStart = { dragOffset = 0f },
            onDragEnd = {
                if (abs(dragOffset) > swipeThreshold) onDismiss()
                dragOffset = 0f
            },
            onDragCancel = { dragOffset = 0f }
        ) { _, dragAmount -> dragOffset += dragAmount }
    }
}