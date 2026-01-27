package com.example.compost2.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.shadow
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
import com.example.compost2.domain.IntegrationType
import com.example.compost2.domain.RecordingStatus
import com.example.compost2.ui.components.ActionLensButton
import com.example.compost2.ui.components.AppTextEditor
import com.example.compost2.ui.components.PlayerWidget
import com.example.compost2.ui.theme.*
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .enableSwipeNavigation(
                onSwipeRight = onNavigateBack,
                onSwipeLeft = { viewModel.onOpenLens() }
            )
    ) {

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

            val displayTitle = when {
                viewModel.title.isNotBlank() -> viewModel.title
                viewModel.rawText.isNotBlank() -> viewModel.rawText.take(150).replace("\n", " ") + "..."
                else -> "New Project"
            }

            Text(
                text = displayTitle,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .basicMarquee(iterations = Int.MAX_VALUE, initialDelayMillis = 1000)
            )

            Spacer(modifier = Modifier.width(8.dp))
        }

        // 2. ОСНОВНОЙ СКРОЛЛ
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {

            // --- ПЛЕЕР ---
            item {
                Box(
                    modifier = Modifier.padding(horizontal = 25.dp, vertical = 10.dp)
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

            // --- ИНТЕГРАЦИИ (LINKED) ---
            item {
                val showIntegrations = viewModel.item?.status != RecordingStatus.SAVED && viewModel.item?.status != RecordingStatus.PROCESSING

                if (showIntegrations) {
                    Box(
                        modifier = Modifier.padding(horizontal = 25.dp, vertical = 4.dp)
                    ) {
                        IntegrationsWidget(
                            integrations = viewModel.item?.completedIntegrations ?: emptyList()
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // --- STICKY HEADER (ШАПКА РЕДАКТОРА) ---
            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 25.dp)
                ) {
                    // Контейнер шапки (Минимальная высота, чтобы вместить кнопку)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp) // Небольшой отступ от блока выше
                    ) {
                        // 1. ВЕРХНЯЯ ЧАСТЬ РАМКИ
                        // Рисуем рамку, но только для шапки
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp) // Высота, покрывающая кнопку и лейбл
                                .background(Color.White, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                .border(1.dp, AppInactive, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        )

                        // "Ластик" для нижней границы рамки шапки (соединение с телом)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .align(Alignment.BottomCenter)
                                .background(Color.White)
                        )

                        // 2. ЗАГОЛОВОК "TEXT OUTPUT"
                        Text(
                            text = "TEXT OUTPUT",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppInactive,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .offset(y = (-6).dp) // Поднимаем на границу рамки
                                .background(Color.White)
                                .padding(horizontal = 4.dp)
                                .align(Alignment.TopStart)
                        )

                        // 3. КНОПКА КОПИРОВАНИЯ (Внутри шапки, справа)
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 8.dp, end = 12.dp) // Отступы внутри рамки
                                .size(34.dp)
                                .clickable { viewModel.copyToClipboard() },
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            shadowElevation = 2.dp,
                            border = BorderStroke(1.dp, Color(0xFFF0F0F2))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = AppTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- ТЕЛО ТЕКСТА ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp)
                        .background(Color.White)
                ) {
                    // Нижняя часть рамки (U-образная)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 400.dp)
                            // Стыкуем с шапкой (сдвиг вверх на 1px, чтобы перекрыть ластик)
                            .offset(y = (-1).dp)
                            .border(1.dp, AppInactive, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                            // Паддинг внутри: сверху 0 (или минимальный), чтобы текст начинался сразу под шапкой
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp)
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
                                    color = AppTextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text("Ready to transcribe...", color = AppInactive)
                        }
                    }
                }
            }
        }

        // 3. RAW TEXT MODAL
        AnimatedVisibility(
            visible = viewModel.showRawTextModal,
            enter = scaleIn(animationSpec = tween(300)) + fadeIn(),
            exit = scaleOut(animationSpec = tween(250)) + fadeOut(),
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
                        .clickable(enabled = false) {},
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    viewModel.reTranscribe()
                                    viewModel.showRawTextModal = false
                                },
                                modifier = Modifier.weight(1f).height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Re\nTranscribe",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        lineHeight = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }

                            TextButton(
                                onClick = { viewModel.showRawTextModal = false },
                                modifier = Modifier.weight(1f).height(56.dp)
                            ) {
                                Text(
                                    "Cancel",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. LENS
        AnimatedVisibility(
            visible = viewModel.showLens,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable { viewModel.showLens = false },
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(360.dp), contentAlignment = Alignment.Center) {
                    ActionLensButton(
                        state = com.example.compost2.ui.screens.ActionState.SELECTION,
                        prompts = viewModel.prompts,
                        amplitude = 0,
                        onResume = { viewModel.showLens = false },
                        onSelect = { prompt -> viewModel.onPromptSelected(prompt) }
                    )
                }

                Column(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "CHOOSE YOUR PROMPT",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "SWIPE RIGHT TO CANCEL",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }
            }
        }

        if (viewModel.isBusy) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppPrimary)
            }
        }
    }
}

// ВИДЖЕТ ИНТЕГРАЦИЙ
@Composable
fun IntegrationsWidget(integrations: List<IntegrationType>) {
    val counts = integrations.groupingBy { it }.eachCount()
    val displayCounts = if (counts.isEmpty()) mapOf(IntegrationType.NONE to 1) else counts

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

            displayCounts.forEach { (type, count) ->
                Box(modifier = Modifier.padding(end = 8.dp)) {
                    AppliedIntegrationBadge(
                        icon = getIconForType(type),
                        color = Color.Black
                    )

                    if (count > 1 || (count > 0 && type != IntegrationType.NONE)) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 4.dp, y = 4.dp)
                                .background(AppPrimary, CircleShape)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = count.toString(),
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppliedIntegrationBadge(icon: ImageVector, color: Color) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(color.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
    }
}

fun getIconForType(type: IntegrationType): ImageVector {
    return when(type) {
        IntegrationType.CALENDAR -> Icons.Default.DateRange
        IntegrationType.GMAIL -> Icons.Default.Email
        IntegrationType.TASKS -> Icons.Default.CheckCircle
        IntegrationType.WORDPRESS -> Icons.Default.Public
        else -> Icons.Default.Description
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