package com.example.compost2.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector // ВАЖНЫЙ ИМПОРТ
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compost2.domain.IntegrationType
import com.example.compost2.ui.components.AppTextEditor
import com.example.compost2.ui.components.RoundedPentagonBox
import com.example.compost2.ui.theme.MontserratFontFamily

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

    var initialTitle by remember { mutableStateOf("") }
    var initialContent by remember { mutableStateOf("") }
    var initialType by remember { mutableStateOf(IntegrationType.NONE) }
    var isInitialized by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(IntegrationType.NONE) }

    var showIntegrationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(promptId) {
        if (promptId != null) {
            val existing = viewModel.getPromptById(promptId)
            if (existing != null) {
                title = existing.title
                content = existing.content
                selectedType = existing.integrationType
                initialTitle = existing.title
                initialContent = existing.content
                initialType = existing.integrationType
            }
        }
        isInitialized = true
    }

    DisposableEffect(Unit) {
        onDispose {
            val hasChanges = title != initialTitle || content != initialContent || selectedType != initialType
            if (isInitialized && hasChanges && title.isNotBlank()) {
                viewModel.savePrompt(promptId, title, content, selectedType)
                Toast.makeText(context, "Prompt Saved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Prompt", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 25.dp)
        ) {

            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                if (title.isEmpty()) {
                    Text(
                        "Prompt Name",
                        color = Color(0xFFCCCCCC),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = MontserratFontFamily,
                        modifier = Modifier.padding(horizontal = 15.dp)
                    )
                }
                BasicTextField(
                    value = title,
                    onValueChange = { if (it.length <= 25) title = it },
                    textStyle = TextStyle(
                        fontFamily = MontserratFontFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 20.sp,
                        color = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(15.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val (icon, color) = getIntegrationIconAndColor(selectedType) // Используем из IntegrationsScreen.kt (или дублируем)
                        RoundedPentagonBox(
                            modifier = Modifier.size(32.dp),
                            color = Color(0xFFF4F4F6),
                            cornerRadius = 6.dp
                        ) {
                            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (selectedType == IntegrationType.NONE) "Text Processing" else selectedType.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.clickable { showIntegrationDialog = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(32.dp).background(Color(0xFFF2F2F5), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            AppTextEditor(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.weight(1f),
                placeholder = "Describe how AI should process the text..."
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showIntegrationDialog) {
        val activeIntegrations = IntegrationType.values().filter {
            intViewModel.enabledIntegrations[it] == true
        }

        AlertDialog(
            onDismissRequest = { showIntegrationDialog = false },
            title = { Text("Select Action", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false).heightIn(max = 300.dp)
                    ) {
                        items(activeIntegrations) { type ->
                            IntegrationOptionRow(getIntegrationTitle(type), type) {
                                selectedType = it; showIntegrationDialog = false
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Manage Integrations",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showIntegrationDialog = false
                                onNavigateToIntegrations()
                            },
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showIntegrationDialog = false }) { Text("Close") } }
        )
    }
}

@Composable
fun IntegrationOptionRow(label: String, type: IntegrationType, onClick: (IntegrationType) -> Unit) {
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