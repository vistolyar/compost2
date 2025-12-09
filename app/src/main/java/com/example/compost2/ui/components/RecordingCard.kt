package com.example.compost2.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.compost2.domain.RecordingItem
import com.example.compost2.domain.RecordingStatus

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecordingCard(
    item: RecordingItem,
    onClick: () -> Unit,
    onSendToSTT: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onPublish: () -> Unit, // Симуляция публикации
    onOpenUrl: (String) -> Unit // Открытие ссылки
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box {
            Column(modifier = Modifier.padding(16.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    CardIcon(item.status)
                    Spacer(modifier = Modifier.width(12.dp))
                    CardHeader(item, onOpenUrl)
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (item.status) {
                    RecordingStatus.SAVED -> {
                        Button(
                            onClick = onSendToSTT,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send to STT")
                        }
                    }

                    RecordingStatus.PROCESSING -> {
                        // Исправлено: убрано слово Resend
                        Button(
                            onClick = onCancel,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Filled.Cancel, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancel")
                        }

                        // ВРЕМЕННАЯ КНОПКА ДЛЯ ТЕСТА
                        Button(
                            onClick = { onPublish() }, // Используем onPublish для симуляции финиша
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                        ) {
                            Text("[DEV] Simulate Finish")
                        }
                    }

                    RecordingStatus.READY -> {
                        Button(
                            onClick = onPublish,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Filled.Publish, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Publish")
                        }
                    }

                    RecordingStatus.PUBLISHED -> {
                        // Кнопок нет
                    }
                }
            }

            CardContextMenu(
                expanded = showMenu,
                onDismiss = { showMenu = false },
                status = item.status,
                onSendToSTT = { showMenu = false; onSendToSTT() },
                onPlay = { showMenu = false; onClick() },
                onCancel = { showMenu = false; onCancel() },
                onEdit = { showMenu = false; onClick() },
                onSettings = { showMenu = false; /* Навигация в настройки поста */ },
                onDelete = { showMenu = false; onDelete() }
            )
        }
    }
}

@Composable
fun CardHeader(item: RecordingItem, onOpenUrl: (String) -> Unit) {
    Column {
        val labelText = when (item.status) {
            RecordingStatus.SAVED -> "Voice record"
            RecordingStatus.PROCESSING -> "Sent to STT..."
            RecordingStatus.READY -> "Ready to Publish"
            RecordingStatus.PUBLISHED -> "Published"
        }
        val labelColor = when (item.status) {
            RecordingStatus.SAVED -> MaterialTheme.colorScheme.primary
            RecordingStatus.PROCESSING -> Color(0xFF2196F3) // Синий
            RecordingStatus.READY -> Color(0xFFFFC107)      // Желтый
            RecordingStatus.PUBLISHED -> Color(0xFF4CAF50)  // Зеленый
        }

        Text(
            text = labelText,
            style = MaterialTheme.typography.bodySmall,
            color = labelColor,
            fontWeight = FontWeight.Bold
        )

        // Основной текст
        val mainText = if (item.status == RecordingStatus.READY || item.status == RecordingStatus.PUBLISHED) {
            item.articleTitle ?: "Untitled Article"
        } else {
            item.name
        }

        Text(
            text = mainText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Для опубликованных показываем ссылку
        if (item.status == RecordingStatus.PUBLISHED && item.publicUrl != null) {
            val shortUrl = if (item.publicUrl.length > 35) item.publicUrl.take(35) + "..." else item.publicUrl

            Row(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable { onOpenUrl(item.publicUrl) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Link, null, modifier = Modifier.size(14.dp), tint = Color.Blue)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = shortUrl,
                    style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline),
                    color = Color.Blue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (item.status == RecordingStatus.SAVED) {
            Text(
                text = "Status: Saved",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun CardIcon(status: RecordingStatus) {
    // Фон иконки
    val bgColor = when (status) {
        RecordingStatus.SAVED -> MaterialTheme.colorScheme.primaryContainer
        RecordingStatus.PROCESSING -> Color(0xFFE3F2FD) // Светло-синий
        RecordingStatus.READY -> Color(0xFFFFF9C4)      // Светло-желтый
        RecordingStatus.PUBLISHED -> Color(0xFFE8F5E9)  // Светло-зеленый
    }

    // Цвет самой иконки
    val iconColor = when (status) {
        RecordingStatus.SAVED -> MaterialTheme.colorScheme.primary
        RecordingStatus.PROCESSING -> Color(0xFF2196F3) // Синий
        RecordingStatus.READY -> Color(0xFFFFC107)      // Желтый
        RecordingStatus.PUBLISHED -> Color(0xFF4CAF50)  // Зеленый
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            RecordingStatus.SAVED -> {
                Icon(Icons.Filled.Mic, null, tint = iconColor)
            }
            RecordingStatus.PROCESSING -> {
                val infiniteTransition = rememberInfiniteTransition()
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    null,
                    tint = iconColor,
                    modifier = Modifier.alpha(alpha)
                )
            }
            RecordingStatus.READY -> {
                Icon(Icons.Filled.Description, null, tint = iconColor)
            }
            RecordingStatus.PUBLISHED -> {
                Icon(Icons.Filled.Language, null, tint = iconColor)
            }
        }
    }
}

@Composable
fun CardContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    status: RecordingStatus,
    onSendToSTT: () -> Unit,
    onPlay: () -> Unit,
    onCancel: () -> Unit,
    onEdit: () -> Unit,
    onSettings: () -> Unit,
    onDelete: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        when (status) {
            RecordingStatus.SAVED -> {
                DropdownMenuItem(
                    text = { Text("Send to STT") },
                    onClick = onSendToSTT,
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, null) }
                )
                DropdownMenuItem(
                    text = { Text("Play") },
                    onClick = onPlay,
                    leadingIcon = { Icon(Icons.Filled.PlayArrow, null) }
                )
            }
            RecordingStatus.PROCESSING -> {
                DropdownMenuItem(
                    text = { Text("Cancel") },
                    onClick = onCancel,
                    leadingIcon = { Icon(Icons.Filled.Cancel, null) }
                )
            }
            RecordingStatus.READY -> {
                DropdownMenuItem(
                    text = { Text("Publish") },
                    onClick = onEdit,
                    leadingIcon = { Icon(Icons.Filled.Publish, null) }
                )
            }
            RecordingStatus.PUBLISHED -> {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = onEdit,
                    leadingIcon = { Icon(Icons.Filled.Edit, null) }
                )
                DropdownMenuItem(
                    text = { Text("Settings") },
                    onClick = onSettings,
                    leadingIcon = { Icon(Icons.Filled.Settings, null) }
                )
            }
        }
        DropdownMenuItem(
            text = { Text("Delete", color = Color.Red) },
            onClick = onDelete,
            leadingIcon = { Icon(Icons.Filled.Delete, null, tint = Color.Red) }
        )
    }
}