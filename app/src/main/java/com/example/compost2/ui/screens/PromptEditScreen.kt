package com.example.compost2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compost2.domain.IntegrationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptEditScreen(
    promptId: String?, // Если null - создаем новый
    onNavigateBack: () -> Unit
) {
    // В данном случае используем PromptSettingsViewModel, так как логика сохранения там
    // Но для чистоты лучше бы вынести в отдельную VM. Пока используем существующую.
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: PromptSettingsViewModel = viewModel(factory = PromptSettingsViewModel.provideFactory(context))

    // Локальное состояние формы
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(IntegrationType.NONE) }

    // Инициализация при открытии
    LaunchedEffect(promptId) {
        if (promptId != null) {
            val existing = viewModel.getPromptById(promptId)
            if (existing != null) {
                title = existing.title
                content = existing.content
                selectedType = existing.integrationType
            }
        }
    }

    // ШАБЛОНЫ ПРОМПТОВ (System Instructions)
    // Это то, что вставляется в текст при клике на иконку
    fun applyTemplate(type: IntegrationType) {
        selectedType = type
        if (content.isNotBlank()) return // Не перезаписываем, если пользователь уже что-то написал

        content = when(type) {
            IntegrationType.CALENDAR -> """
                Analyze the text and extract event details.
                Output JSON:
                {
                  "action_type": "CALENDAR",
                  "title": "Short event title",
                  "body": "Description of the event",
                  "date": "YYYY-MM-DD HH:MM" (Start time inferred from text, use today if not specified)
                }
            """.trimIndent()

            IntegrationType.GMAIL -> """
                Draft an email based on the text.
                Output JSON:
                {
                  "action_type": "GMAIL",
                  "title": "Email Subject",
                  "body": "Email Body (formatted with newlines)"
                }
            """.trimIndent()

            IntegrationType.TASKS -> """
                Extract a actionable task.
                Output JSON:
                {
                  "action_type": "TASKS",
                  "title": "Task Title",
                  "body": "Additional notes or subtasks"
                }
            """.trimIndent()

            IntegrationType.WORDPRESS -> """
                Format as a blog post.
                Output JSON:
                {
                  "action_type": "WORDPRESS",
                  "title": "Catchy Headline",
                  "body": "HTML formatted article content"
                }
            """.trimIndent()

            else -> ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (promptId == null) "New Prompt" else "Edit Prompt") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.savePrompt(promptId, title, content, selectedType)
                            onNavigateBack()
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 1. ВЫБОР ИНТЕГРАЦИИ (ИКОНКИ)
            Text("Link Integration (Optional)", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IntegrationOption(
                    icon = Icons.Default.Description,
                    label = "None",
                    color = Color.Gray,
                    isSelected = selectedType == IntegrationType.NONE,
                    onClick = { applyTemplate(IntegrationType.NONE) }
                )
                IntegrationOption(
                    icon = Icons.Default.DateRange,
                    label = "Calendar",
                    color = Color(0xFFDB4437), // Google Red
                    isSelected = selectedType == IntegrationType.CALENDAR,
                    onClick = { applyTemplate(IntegrationType.CALENDAR) }
                )
                IntegrationOption(
                    icon = Icons.Default.Email,
                    label = "Gmail",
                    color = Color(0xFFEA4335),
                    isSelected = selectedType == IntegrationType.GMAIL,
                    onClick = { applyTemplate(IntegrationType.GMAIL) }
                )
                IntegrationOption(
                    icon = Icons.Default.CheckCircle,
                    label = "Tasks",
                    color = Color(0xFF4285F4), // Google Blue
                    isSelected = selectedType == IntegrationType.TASKS,
                    onClick = { applyTemplate(IntegrationType.TASKS) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            // 2. ПОЛЯ ВВОДА
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Prompt Name (e.g. 'Create Meeting')") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("System Instructions (Hidden)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Занимает всё оставшееся место
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "This text will be sent to AI as a System Prompt to format the output.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun IntegrationOption(
    icon: ImageVector,
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) color else Color.LightGray,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) color else Color.Gray,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
            color = if (isSelected) color else Color.Gray
        )
    }
}