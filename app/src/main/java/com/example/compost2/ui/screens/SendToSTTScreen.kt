package com.example.compost2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compost2.ui.components.RecordingCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendToSTTScreen(
    fileName: String,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    onProcessStarted: () -> Unit // Колбэк, когда нажали "Отправить" и всё прошло ок
) {
    val context = LocalContext.current
    val viewModel: SendToSTTViewModel = viewModel(factory = SendToSTTViewModel.provideFactory(context))

    LaunchedEffect(fileName) {
        viewModel.loadRecording(fileName)
    }

    // Если процесс завершился успешно, сообщаем HomeScreen и уходим назад
    LaunchedEffect(viewModel.isFinished) {
        if (viewModel.isFinished) {
            onProcessStarted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send to AI") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            // Блок кнопок внизу [cite: 192]
            Column(modifier = Modifier.padding(16.dp)) {
                if (viewModel.isUploading) {
                    Text(
                        text = "Processing... ${(viewModel.uploadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LinearProgressIndicator(
                        progress = { viewModel.uploadProgress },
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.cancelProcessing() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Cancel, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel")
                    }
                } else {
                    Button(
                        onClick = { viewModel.startProcessing() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = viewModel.recordingItem != null
                    ) {
                        Icon(Icons.Default.CloudUpload, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Process with AI")
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
            // 1. Техническая информация (Карточка) [cite: 189]
            Text("Recording Info", style = MaterialTheme.typography.titleSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            val item = viewModel.recordingItem
            if (item != null) {
                // Используем наш компонент, но отключаем кнопки, делаем только клик
                RecordingCard(
                    item = item,
                    onClick = { onNavigateToPlayer(item.id) },
                    onSendToSTT = {},
                    onCancel = {},
                    onDelete = {},
                    onPublish = {},
                    onOpenUrl = {}
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Выбор промпта [cite: 190]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Persona / Prompt", style = MaterialTheme.typography.titleSmall, color = Color.Gray, modifier = Modifier.weight(1f))
                IconButton(onClick = { /* TODO: Add prompt */ }) { // [cite: 191]
                    Icon(Icons.Default.Add, contentDescription = "Add Prompt")
                }
            }

            Divider()

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(viewModel.prompts) { prompt ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectPrompt(prompt) }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = (prompt == viewModel.selectedPrompt),
                            onClick = { viewModel.selectPrompt(prompt) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}