package com.example.compost2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.compost2.domain.RecordingItem
import com.example.compost2.domain.RecordingStatus

@Composable
fun RecordingCard(
    item: RecordingItem,
    onClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onCancelClick: () -> Unit = {},
    onResendClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusIcon(item.status)
                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    val labelText = if (item.status == RecordingStatus.PENDING || item.status == RecordingStatus.SAVED)
                        "Voice record"
                    else
                        "Article"

                    Text(
                        text = labelText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Status: ${getStatusText(item.status)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (item.status) {
                RecordingStatus.PENDING -> {
                    if (item.progress != null) {
                        LinearProgressIndicator(
                            progress = { item.progress ?: 0f },
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = onCancelClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Cancel")
                        }
                    }
                }

                RecordingStatus.DRAFT -> {
                    Button(
                        onClick = onEditClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit / Publish", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                RecordingStatus.PUBLISHED -> {
                    OutlinedButton(
                        onClick = onEditClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Published (View)", color = Color(0xFF4CAF50))
                    }
                }

                RecordingStatus.SAVED -> {
                    Button(
                        onClick = onResendClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Process / Resend")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusIcon(status: RecordingStatus) {
    val color = when (status) {
        RecordingStatus.PENDING -> Color.Gray
        RecordingStatus.DRAFT -> Color(0xFFFFC107)
        RecordingStatus.PUBLISHED -> Color(0xFF4CAF50)
        RecordingStatus.SAVED -> Color(0xFF2196F3)
    }

    val icon = when (status) {
        RecordingStatus.PENDING -> Icons.Default.Schedule
        RecordingStatus.DRAFT -> Icons.Default.Circle
        RecordingStatus.PUBLISHED -> Icons.Default.CheckCircle
        RecordingStatus.SAVED -> Icons.Default.Mic
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        if (status == RecordingStatus.PENDING) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = color)
        } else {
            Icon(icon, contentDescription = null, tint = color)
        }
    }
}

fun getStatusText(status: RecordingStatus): String {
    return when (status) {
        RecordingStatus.PENDING -> "Pending..."
        RecordingStatus.DRAFT -> "Draft"
        RecordingStatus.PUBLISHED -> "Published"
        RecordingStatus.SAVED -> "Saved"
    }
}