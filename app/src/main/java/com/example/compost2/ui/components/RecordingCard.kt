package com.example.compost2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compost2.domain.IntegrationType
import com.example.compost2.domain.RecordingItem
import com.example.compost2.domain.RecordingStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingCard(
    item: RecordingItem,
    onClick: () -> Unit,
    // Эти параметры пока оставляем для совместимости, но визуально они уходят в DetailScreen
    onSendToSTT: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onPublish: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onSettings: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 1. ЗАГОЛОВОК (Крупно)
            val displayTitle = item.articleTitle ?: item.name
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. СТАТУС ТРАНСКРИПЦИИ (Мелко)
            // Пример: "Transcribed • 10:45 AM" (Дату добавим позже, пока статус)
            val statusText = when (item.status) {
                RecordingStatus.PROCESSING -> "Processing..."
                RecordingStatus.SAVED -> "Audio Saved"
                else -> "Transcribed"
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // 3. НИЖНИЙ РЯД (Badge Row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // ЛЕВАЯ ЧАСТЬ: Тип исходника (Микрофон)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Voice",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // ПРАВАЯ ЧАСТЬ: Иконки интеграций
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Пока мы не внедрили логику сохранения интеграций в JSON,
                    // визуализируем логику "на лету" для демонстрации.
                    // В будущем тут будет item.completedIntegrations.forEach { ... }

                    val hasIntegrations = item.wordpressId != null || item.publicUrl != null

                    if (hasIntegrations) {
                        // Если есть WP
                        if (item.wordpressId != null) {
                            IntegrationBadge(Icons.Default.Public, Color(0xFF2196F3)) // Blue
                        }
                        // Если есть ссылка (считаем это пока признаком публикации)
                        if (item.publicUrl != null) {
                            IntegrationBadge(Icons.Default.Link, Color.Gray)
                        }
                    } else {
                        // Если ничего нет - серая иконка текста
                        IntegrationBadge(Icons.Default.Description, Color.LightGray)
                    }
                }
            }
        }
    }
}

@Composable
fun IntegrationBadge(icon: ImageVector, color: Color) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color
        )
    }
}