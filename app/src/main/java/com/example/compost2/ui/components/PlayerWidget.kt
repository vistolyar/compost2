package com.example.compost2.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compost2.domain.RecordingStatus
import com.example.compost2.ui.theme.* // Импортируем новую палитру

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerWidget(
    fileName: String,
    dateTimestamp: Long,
    duration: String,
    currentPos: String,
    isPlaying: Boolean,
    progress: Float,
    status: RecordingStatus,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onActionClick: () -> Unit,
    onStatusActionClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val endPadding by animateDpAsState(
        targetValue = if (isExpanded) 0.dp else 60.dp,
        animationSpec = tween(500),
        label = "shutterAnimation"
    )

    val dateText = remember(dateTimestamp, isExpanded) {
        val date = java.util.Date(dateTimestamp)
        if (isExpanded) {
            java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault()).format(date)
        } else {
            java.text.SimpleDateFormat("dd-MMM", java.util.Locale.US).format(date)
        }
    }

    // ИСПОЛЬЗУЕМ НОВЫЕ ЦВЕТА
    val (statusColor, statusText, actionLabel) = when (status) {
        RecordingStatus.TRANSCRIBED, RecordingStatus.PUBLISHED, RecordingStatus.READY ->
            Triple(AppSuccess, "TRANSCRIBED", "Show")
        RecordingStatus.PROCESSING ->
            Triple(AppWarning, "PROCESSING...", "Cancel")
        else ->
            Triple(AppDanger, "RECORDED", "Transcribe")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(AppPrimary) // Был ActionPurple
    ) {
        // КНОПКА SEND
        Column(
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .clickable { onActionClick() },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(32.dp)) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val path = Path().apply {
                        moveTo(w * 0.50f, h * 0.05f)
                        lineTo(w * 0.95f, h * 0.38f)
                        lineTo(w * 0.78f, h * 0.92f)
                        lineTo(w * 0.22f, h * 0.92f)
                        lineTo(w * 0.05f, h * 0.38f)
                        close()
                    }
                    drawPath(path = path, color = AppWhite, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)) // Был TextWhite
                    val arrowPath = Path().apply {
                        moveTo(w * 0.35f, h * 0.58f)
                        lineTo(w * 0.50f, h * 0.43f)
                        lineTo(w * 0.65f, h * 0.58f)
                        moveTo(w * 0.50f, h * 0.43f)
                        lineTo(w * 0.50f, h * 0.75f)
                    }
                    drawPath(path = arrowPath, color = AppWhite, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)) // Был TextWhite
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("SEND", color = AppWhite, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) // Был TextWhite
        }

        // ШТОРКА (MAIN INFO)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = endPadding)
                .clip(RoundedCornerShape(
                    topStart = 32.dp, bottomStart = 32.dp,
                    topEnd = if (isExpanded) 32.dp else 0.dp,
                    bottomEnd = if (isExpanded) 32.dp else 0.dp
                ))
                .background(Color(0xFFE2E2E7)) // Цвет шторки можно вынести в AppColors, но пока оставим как в дизайне
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { isExpanded = !isExpanded }
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 15.dp)
        ) {

            Column(
                modifier = Modifier.align(Alignment.CenterStart).fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                // ВЕРХНЯЯ ЧАСТЬ
                if (isExpanded) {
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = fileName,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black.copy(alpha = 0.7f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black.copy(alpha = 0.3f),
                            textAlign = TextAlign.End,
                            maxLines = 1
                        )
                    }
                }

                // ПЛЕЕР
                if (isExpanded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PlayButton(isPlaying, onTogglePlay)
                        Spacer(modifier = Modifier.width(12.dp))
                        Slider(
                            value = progress,
                            onValueChange = onSeek,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.Black,
                                activeTrackColor = Color.Black,
                                inactiveTrackColor = Color(0xFFC7C7CC)
                            ),
                            modifier = Modifier.weight(1f).height(20.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 52.dp, top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(currentPos, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(duration, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusIndicator(statusColor, statusText)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = actionLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = AppPrimary, // Был ActionPurple
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.clickable { onStatusActionClick() }
                            )
                        }

                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = Color.Black.copy(alpha = 0.3f),
                            fontWeight = FontWeight.Bold
                        )
                    }

                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PlayButton(isPlaying) {
                            isExpanded = true
                            onTogglePlay()
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                text = "Duration: $duration",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = Color.Black.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            StatusIndicator(statusColor, statusText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayButton(isPlaying: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = "Play",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun StatusIndicator(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = Color.Black
        )
    }
}