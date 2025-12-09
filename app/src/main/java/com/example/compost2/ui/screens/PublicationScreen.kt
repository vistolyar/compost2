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
import androidx.compose.foundation.layout.size // ВОТ ЭТОТ ИМПОРТ БЫЛ НУЖЕН
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Publish
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicationScreen(
    fileName: String,
    onNavigateBack: () -> Unit,
    onPublished: () -> Unit,
    onDeleted: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: PublicationViewModel = viewModel(factory = PublicationViewModel.provideFactory(context))
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(fileName) {
        viewModel.loadRecording(fileName)
    }

    LaunchedEffect(viewModel.isPublishedSuccess) {
        if (viewModel.isPublishedSuccess) {
            Toast.makeText(context, "Published successfully!", Toast.LENGTH_SHORT).show()
            onPublished()
        }
    }
    LaunchedEffect(viewModel.isDeletedSuccess) {
        if (viewModel.isDeletedSuccess) {
            Toast.makeText(context, "Draft deleted", Toast.LENGTH_SHORT).show()
            onDeleted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Publish Article") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Save Draft Button
                    TextButton(
                        onClick = {
                            viewModel.saveDraft()
                            Toast.makeText(context, "Draft saved to WP", Toast.LENGTH_SHORT).show()
                        },
                        enabled = !viewModel.isSavingDraft
                    ) {
                        if (viewModel.isSavingDraft) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Save Draft", fontWeight = FontWeight.Bold)
                        }
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete", color = MaterialTheme.colorScheme.onErrorContainer)
                    }

                    Button(
                        onClick = { viewModel.publishPost() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        enabled = !viewModel.isPublishing
                    ) {
                        if (viewModel.isPublishing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.Publish, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Publish")
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
            Text("Article Title:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text(
                text = viewModel.recordingItem?.articleTitle ?: "Loading...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text("Select Categories:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    items(viewModel.categories) { category ->
                        val isSelected = viewModel.selectedCategories.contains(category)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleCategory(category) }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { viewModel.toggleCategory(category) },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = category,
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
            title = { Text("Delete Draft?") },
            text = { Text("This will permanently delete the recording and the draft article. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deletePost()
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}