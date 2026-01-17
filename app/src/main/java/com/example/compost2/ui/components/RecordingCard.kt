package com.example.compost2.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.compost2.domain.RecordingItem
import com.example.compost2.domain.RecordingStatus
import com.example.compost2.ui.theme.AppCardBg // Импортируем наш цвет
import com.example.compost2.ui.theme.AppPrimary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecordingCard(
    item: RecordingItem,
    onClick: () -> Unit,
    onSendToSTT: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit, // Это действие повесим на долгий тап
    onPublish: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onSettings: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            // Добавляем поддержку долгого тапа для удаления
            .combinedClickable(
                onClick = onClick,
                onLongClick = onDelete
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp), // Унифицированное скругление
        colors = CardDefaults.cardColors(
            containerColor = AppCardBg // ИСПРАВЛЕНИЕ: Явно используем #E2E1E6
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp) // Унифицированный отступ
        ) {
            // Заголовок
            val displayTitle = item.articleTitle ?: item.name
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Статус
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
            Divider(color = MaterialTheme.colorScheme.surfaceVariant) // Разделитель
            Spacer(modifier = Modifier.height(12.dp))

            // Нижний ряд
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AppPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "VOICE",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val hasIntegrations = item.wordpressId != null || item.publicUrl != null

                    if (hasIntegrations) {
                        if (item.wordpressId != null) {
                            IntegrationBadge(Icons.Default.Public, Color(0xFF2196F3))
                        }
                        if (item.publicUrl != null) {
                            IntegrationBadge(Icons.Default.Link, Color.Gray)
                        }
                    } else {
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