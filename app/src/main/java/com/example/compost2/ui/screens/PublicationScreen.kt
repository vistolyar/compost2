package com.example.compost2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicationScreen(
    fileName: String,
    onNavigateBack: () -> Unit,
    onPublished: (String, Int) -> Unit,
    onDeleted: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: PublicationViewModel = viewModel(factory = PublicationViewModel.provideFactory(context))
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(fileName) {
        viewModel.loadData(fileName)
    }

    LaunchedEffect(viewModel.isSuccess) {
        if (viewModel.isSuccess) {
            viewModel.publishedUrl?.let { url ->
                viewModel.publishedId?.let { id ->
                    onPublished(url, id)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Publish & Export") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    }

                    // Кнопка действия (WordPress)
                    Button(
                        onClick = { viewModel.submitPost() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)), // Blue for WP
                        enabled = !viewModel.isPublishing,
                        modifier = Modifier.weight(1f).padding(start = 16.dp)
                    ) {
                        if (viewModel.isPublishing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            val isUpdate = viewModel.publishedId != null
                            Icon(if (isUpdate) Icons.Default.Update else Icons.Default.Publish, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isUpdate) "Update WP" else "Post to WordPress")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                // Заголовок
                Text("Article:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(
                    text = viewModel.recordingItem?.articleTitle ?: "Loading...",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                // --- БЛОК GOOGLE EXPORT ---
                Text("Google Integrations:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (viewModel.isGoogleConnected) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Calendar
                        OutlinedButton(
                            onClick = { viewModel.exportToCalendar() },
                            modifier = Modifier.weight(1f),
                            enabled = !viewModel.isPublishing
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.DateRange, null, tint = Color(0xFFDB4437))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Event", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        // Gmail
                        OutlinedButton(
                            onClick = { viewModel.exportToGmail() },
                            modifier = Modifier.weight(1f),
                            enabled = !viewModel.isPublishing
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Email, null, tint = Color(0xFFEA4335))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Draft", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Connect Google Account in Settings to enable export features.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                // --- БЛОК WORDPRESS SETTINGS ---
                Text("WordPress Options:", style = MaterialTheme.typography.titleMedium)
                StatusOption(
                    label = "Publish (Public)",
                    value = "publish",
                    current = viewModel.selectedStatus,
                    icon = Icons.Default.Public,
                    onSelect = { viewModel.setStatus(it) }
                )
                StatusOption(
                    label = "Draft (Hidden)",
                    value = "draft",
                    current = viewModel.selectedStatus,
                    icon = Icons.Default.Edit,
                    onSelect = { viewModel.setStatus(it) }
                )
                StatusOption(
                    label = "Private (Only me)",
                    value = "private",
                    current = viewModel.selectedStatus,
                    icon = Icons.Default.VisibilityOff,
                    onSelect = { viewModel.setStatus(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Categories:", style = MaterialTheme.typography.titleMedium)
            }

            // Список категорий
            if (viewModel.isLoading) {
                item { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
            } else if (viewModel.categories.isEmpty()) {
                item { Text("No categories found (or WP not connected).", color = Color.Gray) }
            } else {
                items(viewModel.categories) { category ->
                    val isSelected = viewModel.selectedCategoryIds.contains(category.id)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleCategory(category.id) }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { viewModel.toggleCategory(category.id) },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Data?") },
            text = { Text("This will remove the recording from your phone. It will NOT delete the post from WordPress.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleted()
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun StatusOption(
    label: String,
    value: String,
    current: String,
    icon: ImageVector,
    onSelect: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(value) }
            .padding(vertical = 4.dp)
    ) {
        RadioButton(
            selected = (value == current),
            onClick = { onSelect(value) }
        )
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}