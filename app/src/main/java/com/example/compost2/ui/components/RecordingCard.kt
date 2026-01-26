package com.example.compost2.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.compost2.domain.IntegrationType
import com.example.compost2.domain.RecordingItem
import com.example.compost2.domain.RecordingStatus
import com.example.compost2.ui.screens.getIntegrationIconAndColor
import com.example.compost2.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecordingCard(
    item: RecordingItem,
    onClick: () -> Unit,
    onSendToSTT: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onPublish: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onSettings: () -> Unit
) {
    // ЛОГИКА СТАТУСА
    val (statusColor, statusText) = when (item.status) {
        RecordingStatus.TRANSCRIBED, RecordingStatus.PUBLISHED, RecordingStatus.READY -> AppSuccess to "Transcribed"
        RecordingStatus.PROCESSING -> AppWarning to "Processing..." // Точки можно анимировать отдельно, но пока статика
        else -> AppDanger to "Recorded"
    }

    // ЛОГИКА ИМЕНИ
    val displayName = when (item.status) {
        RecordingStatus.SAVED -> item.id // Имя файла (Recording)
        // Если процессинг, показываем имя файла (если это первая транскрибация) или Тайтл (если уже был)
        RecordingStatus.PROCESSING -> item.articleTitle ?: item.id
        else -> item.articleTitle ?: item.name // Transcribed Title
    }

    // ЛОГИКА ИКОНКИ МИКРОФОНА
    val micTint = if (item.status == RecordingStatus.SAVED) AppInactive else AppTextPrimary.copy(alpha = 0.8f)

    // Анимация для Processing (мигание)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (item.status == RecordingStatus.PROCESSING) 0.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .combinedClickable(onClick = onClick, onLongClick = onDelete),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppCardBg)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp)
        ) {
            // Название
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = AppTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Статус с кружком
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(6.dp))
                // Кружок статуса
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(statusColor, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Микрофон (мигает при процессинге)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp).alpha(alpha), // Применяем альфу
                        tint = micTint
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "VOICE",
                        style = MaterialTheme.typography.labelSmall,
                        color = micTint
                    )
                }

                // Иконки интеграций
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Если есть WP
                    if (item.wordpressId != null) {
                        IntegrationBadge(Icons.Default.Public, Color(0xFF21759B))
                    }
                    // Если есть другие (пока из списка completedIntegrations)
                    item.completedIntegrations.forEach { type ->
                        val (icon, color) = getIntegrationIconAndColorRec(type)
                        IntegrationBadge(icon, color)
                    }
                    // Если ничего нет, иконка текста
                    if (item.wordpressId == null && item.completedIntegrations.isEmpty()) {
                        IntegrationBadge(Icons.Default.Description, AppInactive)
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
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = color)
    }
}

// Дубликат хелпера, чтобы не тянуть зависимости из экранов (можно вынести в Utils)
fun getIntegrationIconAndColorRec(type: IntegrationType): Pair<ImageVector, Color> {
    return when (type) {
        IntegrationType.CALENDAR -> Icons.Default.DateRange to Color(0xFF34A853)
        IntegrationType.GMAIL -> Icons.Default.Email to Color(0xFFEA4335)
        IntegrationType.TASKS -> Icons.Default.CheckCircle to Color(0xFF4285F4)
        IntegrationType.WORDPRESS -> Icons.Default.Public to Color(0xFF21759B)
        else -> Icons.Default.Description to Color.Black
    }
}