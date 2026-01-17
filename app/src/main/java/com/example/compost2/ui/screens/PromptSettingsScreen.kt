package com.example.compost2.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compost2.domain.IntegrationType
import com.example.compost2.domain.PromptItem
import com.example.compost2.ui.components.RoundedPentagonBox // Используем новый компонент

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PromptSettingsScreen(
    onNavigateBack: () -> Unit,
    onEditPrompt: (String?) -> Unit
) {
    val context = LocalContext.current
    val viewModel: PromptSettingsViewModel = viewModel(factory = PromptSettingsViewModel.provideFactory(context))

    var showSortDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prompts", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = viewModel.isSelectionMode,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                BottomAppBar(containerColor = Color.White, tonalElevation = 10.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { viewModel.toggleSelectionMode() }) {
                            Text("Cancel", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        Text("${viewModel.selectedIds.size} selected", style = MaterialTheme.typography.titleSmall)
                        Button(
                            onClick = { viewModel.deleteSelected() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // --- HEADER ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // КНОПКА ADD PROMPT
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { onEditPrompt(null) }
                                .padding(vertical = 8.dp)
                        ) {
                            // ИСПРАВЛЕНИЕ: Используем RoundedPentagonBox с темно-серым фоном
                            RoundedPentagonBox(
                                modifier = Modifier.size(48.dp),
                                color = Color(0xFF333333), // Темно-серый
                                cornerRadius = 8.dp // Скругление
                            ) {
                                // Жирный белый плюс
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    val strokeWidth = 4.dp.toPx()
                                    val center = size.width / 2
                                    drawLine(Color.White, androidx.compose.ui.geometry.Offset(center, 0f), androidx.compose.ui.geometry.Offset(center, size.height), strokeWidth, StrokeCap.Round)
                                    drawLine(Color.White, androidx.compose.ui.geometry.Offset(0f, center), androidx.compose.ui.geometry.Offset(size.width, center), strokeWidth, StrokeCap.Round)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "ADD PROMPT",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 12.sp,
                                color = Color(0xFF333333)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .border(1.dp, Color(0xFFE0E0E5), RoundedCornerShape(12.dp))
                                .clickable { showSortDialog = true }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Sort, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sort", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // --- СПИСОК ---
                items(viewModel.prompts, key = { it.id }) { prompt ->
                    val isSelected = viewModel.selectedIds.contains(prompt.id)
                    PromptLibraryCard(
                        prompt = prompt,
                        isSelectionMode = viewModel.isSelectionMode,
                        isSelected = isSelected,
                        onClick = {
                            if (viewModel.isSelectionMode) viewModel.toggleSelection(prompt.id)
                            else onEditPrompt(prompt.id)
                        },
                        onLongClick = {
                            if (!viewModel.isSelectionMode) {
                                viewModel.toggleSelectionMode()
                                viewModel.toggleSelection(prompt.id)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("Sort by", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column {
                    SortOptionRow("Last use date", { viewModel.sortPrompts("date"); showSortDialog = false }, { viewModel.sortPrompts("date"); showSortDialog = false })
                    SortOptionRow("Alphabetical", { viewModel.sortPrompts("alpha"); showSortDialog = false }, { viewModel.sortPrompts("alpha"); showSortDialog = false })
                    SortOptionRow("Use count", { viewModel.sortPrompts("usage"); showSortDialog = false }, { viewModel.sortPrompts("usage"); showSortDialog = false })
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showSortDialog = false }) { Text("Close") } }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PromptLibraryCard(
    prompt: PromptItem,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val backgroundColor = if (isSelected) Color(0xFFF0F0FF) else Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .border(2.dp, borderColor, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp).size(24.dp).clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.White)
                        .border(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            // ИСПРАВЛЕНИЕ: Используем RoundedPentagonBox как подложку (рамку)
            RoundedPentagonBox(
                modifier = Modifier.size(48.dp),
                color = Color(0xFFF4F4F6), // Светло-серый фон
                cornerRadius = 8.dp
            ) {
                val (icon, color) = when (prompt.integrationType) {
                    IntegrationType.CALENDAR -> Icons.Default.DateRange to Color(0xFF34A853)
                    IntegrationType.GMAIL -> Icons.Default.Email to Color(0xFFEA4335)
                    IntegrationType.TASKS -> Icons.Default.CheckCircle to Color(0xFF4285F4)
                    IntegrationType.WORDPRESS -> Icons.Default.Public to Color(0xFF21759B)
                    else -> Icons.Default.Description to Color.Black
                }
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = prompt.title, style = MaterialTheme.typography.titleSmall, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = prompt.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888888),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "USED ${prompt.usageCount} TIMES", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color(0xFFCCCCCC))
                    if (prompt.lastUsed != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "•  LAST: ${prompt.lastUsed}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color(0xFFCCCCCC))
                    }
                }
            }
        }
    }
}

@Composable
fun SortOptionRow(label: String, onAsc: () -> Unit, onDesc: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SortArrowButton(isUp = true, onClick = onAsc)
            SortArrowButton(isUp = false, onClick = onDesc)
        }
    }
    Divider(color = Color(0xFFF0F0F2))
}

@Composable
fun SortArrowButton(isUp: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(32.dp).background(Color(0xFFF2F2F5), RoundedCornerShape(8.dp)).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = if (isUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
    }
}