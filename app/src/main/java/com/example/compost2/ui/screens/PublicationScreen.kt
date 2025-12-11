package com.example.compost2.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit // ВОТ ЭТОТ ИМПОРТ БЫЛ НУЖЕН
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onPublished: (String, Int) -> Unit, // Возвращаем URL и ID
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
                title = { Text(if (viewModel.publishedId != null) "Manage Post" else "Publish Article") },
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

                    // Кнопка действия (Создать или Обновить)
                    Button(
                        onClick = { viewModel.submitPost() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        enabled = !viewModel.isPublishing,
                        modifier = Modifier.weight(1f).padding(start = 16.dp)
                    ) {
                        if (viewModel.isPublishing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            val isUpdate = viewModel.publishedId != null
                            Icon(if (isUpdate) Icons.Default.Update else Icons.Default.Publish, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isUpdate) "Update Post" else "Publish")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
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

            // --- ВЫБОР СТАТУСА ---
            Text("Status:", style = MaterialTheme.typography.titleMedium)
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
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Список категорий
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Categories:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    if (viewModel.categories.isEmpty() && !viewModel.isLoading) {
                        item {
                            Text("No categories found.", modifier = Modifier.padding(16.dp), color = Color.Gray)
                        }
                    }

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