package com.example.compost2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector // ДОБАВЛЕН ИМПОРТ
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compost2.domain.IntegrationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegrationsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: IntegrationsViewModel = viewModel(factory = IntegrationsViewModel.provideFactory(context))

    val integrationList = IntegrationType.values().toList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Integrations", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(integrationList) { type ->
                IntegrationItemCard(
                    type = type,
                    isEnabled = viewModel.enabledIntegrations[type] ?: true,
                    onToggle = { viewModel.toggleIntegration(type) }
                )
            }
        }
    }
}

@Composable
fun IntegrationItemCard(
    type: IntegrationType,
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    val (icon, color) = getIntegrationIconAndColor(type)
    val isLocked = type == IntegrationType.NONE

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Явно указываем imageVector, чтобы убрать неоднозначность
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getIntegrationTitle(type),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (isEnabled) "Active" else "Disabled",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggle() },
                enabled = !isLocked
            )
        }
    }
}

fun getIntegrationTitle(type: IntegrationType): String {
    return when(type) {
        IntegrationType.NONE -> "Text Processing"
        IntegrationType.CALENDAR -> "Google Calendar"
        IntegrationType.GMAIL -> "Gmail"
        IntegrationType.TASKS -> "Google Tasks"
        IntegrationType.WORDPRESS -> "WordPress"
    }
}

// Дублируем хелпер здесь или выносим в общий файл Utils.kt
// Важно возвращать ImageVector явно
fun getIntegrationIconAndColor(type: IntegrationType): Pair<ImageVector, Color> {
    return when (type) {
        IntegrationType.CALENDAR -> Icons.Default.DateRange to Color(0xFF34A853)
        IntegrationType.GMAIL -> Icons.Default.Email to Color(0xFFEA4335)
        IntegrationType.TASKS -> Icons.Default.CheckCircle to Color(0xFF4285F4)
        IntegrationType.WORDPRESS -> Icons.Default.Public to Color(0xFF21759B)
        else -> Icons.Default.Description to Color.Black
    }
}