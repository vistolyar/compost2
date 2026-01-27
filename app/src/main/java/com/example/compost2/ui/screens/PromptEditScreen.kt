package com.example.compost2.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compost2.domain.IntegrationType
import com.example.compost2.ui.components.*
import com.example.compost2.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptEditScreen(
    promptId: String?,
    onNavigateBack: () -> Unit,
    onNavigateToIntegrations: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: PromptSettingsViewModel = viewModel(factory = PromptSettingsViewModel.provideFactory(context))
    val intViewModel: IntegrationsViewModel = viewModel(factory = IntegrationsViewModel.provideFactory(context))

    // Исходные данные
    var initialTitle by remember { mutableStateOf("") }
    var initialContent by remember { mutableStateOf("") }
    var initialType by remember { mutableStateOf(IntegrationType.NONE) }
    var initialIsActive by remember { mutableStateOf(true) }

    var isNewPrompt by remember { mutableStateOf(true) }

    // Текущие данные
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(IntegrationType.NONE) }

    // Состояние фокуса заголовка и ручного изменения
    var isTitleFocused by remember { mutableStateOf(false) }
    var isTitleManuallyChanged by remember { mutableStateOf(false) }

    var showIntegrationDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isModified = title != initialTitle || content != initialContent || selectedType != initialType

    // Загрузка
    LaunchedEffect(promptId) {
        if (promptId != null) {
            val existing = viewModel.getPromptById(promptId)
            if (existing != null) {
                isNewPrompt = false
                title = existing.title
                content = existing.content
                selectedType = existing.integrationType
                initialIsActive = existing.isActive

                initialTitle = existing.title
                initialContent = existing.content
                initialType = existing.integrationType

                isTitleManuallyChanged = true
            }
        }
    }

    // ЛОГИКА АВТОЗАПОЛНЕНИЯ ТАЙТЛА
    LaunchedEffect(content) {
        if (!isTitleManuallyChanged || title.trim().isEmpty()) {
            val potentialTitle = content.take(20).replace("\n", " ").trim()
            if (potentialTitle.isNotEmpty()) {
                title = potentialTitle
            }
        }
    }

    fun save(asActive: Boolean) {
        var finalTitle = title.trim()
        if (finalTitle.isEmpty()) {
            finalTitle = content.take(20).replace("\n", " ").trim()
            if (finalTitle.isEmpty()) finalTitle = "Untitled Prompt"
        }

        viewModel.savePrompt(promptId, finalTitle, content, selectedType, asActive)
        Toast.makeText(context, if (asActive) "Saved & Activated" else "Saved as Draft", Toast.LENGTH_SHORT).show()
        onNavigateBack()
    }

    // --- ЛОГИКА ТЕКСТА КНОПОК ---
    val btnCancelText: String
    val btnActiveText: String
    val btnDraftText = "Save\nDraft"

    if (isNewPrompt) {
        btnCancelText = "Cancel"
        btnActiveText = "Save &\nActivate"
    } else {
        if (initialIsActive) {
            btnCancelText = "Cancel &\nRestore"
            btnActiveText = "Save &\nRe-activate"
        } else {
            btnCancelText = "Cancel"
            btnActiveText = "Save &\nActivate"
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (isNewPrompt) "Create Prompt" else "Edit Prompt",
                onBackClick = onNavigateBack,
                actions = {
                    if (!isNewPrompt) {
                        TextButton(onClick = { showDeleteDialog = true }) {
                            Text(
                                "Delete",
                                color = AppDanger, // Всегда красный
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        },
        containerColor = AppScreenBg,
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(AppScreenBg)
                    .padding(16.dp)
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppGhostButton(
                        text = btnCancelText,
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    AppActionButton(
                        text = btnActiveText,
                        onClick = { save(true) },
                        modifier = Modifier.weight(1f),
                        isEnabled = isModified || isNewPrompt,
                        statusDotColor = AppSuccess
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    AppActionButton(
                        text = btnDraftText,
                        onClick = { save(false) },
                        modifier = Modifier.weight(1f),
                        isEnabled = isModified || isNewPrompt,
                        statusDotColor = AppWarning
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 25.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(10.dp))

            // --- TITLE BLOCK ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(AppWhite.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .border(1.dp, AppInactive, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                if (title.isEmpty() && !isTitleFocused) {
                    Text(
                        "Just start to type your prompt in text field below⇩ or add title here",
                        color = AppInactive,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = MontserratFontFamily,
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
                    )
                }

                BasicTextField(
                    value = title,
                    onValueChange = {
                        if (it.length <= 50) {
                            title = it
                            isTitleManuallyChanged = true
                        }
                    },
                    textStyle = TextStyle(
                        fontFamily = MontserratFontFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 20.sp,
                        color = AppTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .onFocusChanged { isTitleFocused = it.isFocused },
                    singleLine = true
                )

                if (!isNewPrompt) {
                    val statusLabel = if (initialIsActive) "Active" else "Draft"
                    val statusColor = if (initialIsActive) AppSuccess else AppWarning

                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 6.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontSize = 8.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(statusColor, CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- INTEGRATION CARD ---
            AppCard(onClick = { /* */ }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // ЛЕВАЯ ЧАСТЬ
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        val (icon, color) = getIntegrationIconAndColor(selectedType)
                        RoundedPentagonBox(
                            modifier = Modifier.size(32.dp),
                            color = AppScreenBg,
                            cornerRadius = 6.dp
                        ) {
                            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = if (selectedType == IntegrationType.NONE) "Text Processing" else selectedType.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            lineHeight = 16.sp
                        )
                    }

                    // ПРАВАЯ ЧАСТЬ
                    Row(
                        modifier = Modifier
                            .clickable { showIntegrationDialog = true }
                            .padding(start = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Cached,
                                contentDescription = "Change",
                                tint = AppPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Change",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppPrimary,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- EDITOR ---
            AppTextEditor(
                value = content,
                onValueChange = { content = it },
                label = "PROMPT TEXT", // ИСПРАВЛЕНО: Добавлен label, убран showJsonTab
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                placeholder = "Describe how AI should process the text...",
                onCopy = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Prompt Content", content)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // --- DIALOGS ---
    if (showIntegrationDialog) {
        val activeIntegrations = IntegrationType.values().filter {
            intViewModel.enabledIntegrations[it] == true
        }

        AlertDialog(
            onDismissRequest = { showIntegrationDialog = false },
            title = { Text("Select Action", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column {
                    LazyColumn(modifier = Modifier.weight(1f, fill = false).heightIn(max = 300.dp)) {
                        items(activeIntegrations) { type ->
                            IntegrationOptionRow(getIntegrationTitle(type), type) {
                                selectedType = it; showIntegrationDialog = false
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = AppInactive.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Manage Integrations",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppPrimary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.fillMaxWidth().clickable {
                            showIntegrationDialog = false
                            onNavigateToIntegrations()
                        },
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showIntegrationDialog = false }) { Text("Close") } },
            containerColor = AppWhite
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Prompt?", style = MaterialTheme.typography.titleMedium) },
            text = { Text("This action cannot be undone.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePrompt(promptId!!)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppDanger)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } },
            containerColor = AppWhite
        )
    }
}

// ХЕЛПЕРЫ ВНУТРИ ФАЙЛА
@Composable
private fun IntegrationOptionRow(label: String, type: IntegrationType, onClick: (IntegrationType) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(type) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (icon, color) = getIntegrationIconAndColor(type)
        Icon(imageVector = icon, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}