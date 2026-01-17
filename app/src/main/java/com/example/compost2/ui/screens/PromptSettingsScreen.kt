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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compost2.domain.IntegrationType
import com.example.compost2.domain.PromptItem
import com.example.compost2.ui.components.*
import com.example.compost2.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptSettingsScreen(
    onNavigateBack: () -> Unit,
    onEditPrompt: (String?) -> Unit
) {
    val context = LocalContext.current
    val viewModel: PromptSettingsViewModel = viewModel(factory = PromptSettingsViewModel.provideFactory(context))
    val lifecycleOwner = LocalLifecycleOwner.current

    var showSortDialog by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadPrompts()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Prompts",
                onBackClick = onNavigateBack
            )
        },
        containerColor = AppScreenBg,
        bottomBar = {
            AnimatedVisibility(
                visible = viewModel.isSelectionMode,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                // НИЖНЯЯ ПАНЕЛЬ С ДЕЙСТВИЯМИ
                BottomAppBar(containerColor = AppWhite, tonalElevation = 10.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Слева - Отмена
                        AppGhostButton("Cancel", onClick = { viewModel.toggleSelectionMode() })

                        // Справа - Ряд действий
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Activate (Зеленый текст)
                            AppGhostButton(
                                text = "Activate",
                                onClick = { viewModel.updateSelectedStatus(true) },
                                textColor = AppSuccess
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Draft (Желтый текст)
                            AppGhostButton(
                                text = "Draft",
                                onClick = { viewModel.updateSelectedStatus(false) },
                                textColor = AppWarning
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Delete (Красный текст)
                            AppGhostButton(
                                text = "Delete",
                                onClick = { viewModel.deleteSelected() },
                                textColor = AppDanger
                            )
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
                // Header (Add / Sort)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onEditPrompt(null) }.padding(vertical = 8.dp)
                        ) {
                            RoundedPentagonBox(
                                modifier = Modifier.size(48.dp),
                                color = Color(0xFF333333),
                                cornerRadius = 8.dp
                            ) {
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    val strokeWidth = 4.dp.toPx()
                                    val center = size.width / 2
                                    drawLine(Color.White, androidx.compose.ui.geometry.Offset(center, 0f), androidx.compose.ui.geometry.Offset(center, size.height), strokeWidth, StrokeCap.Round)
                                    drawLine(Color.White, androidx.compose.ui.geometry.Offset(0f, center), androidx.compose.ui.geometry.Offset(size.width, center), strokeWidth, StrokeCap.Round)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("ADD PROMPT", style = MaterialTheme.typography.labelSmall, fontSize = 12.sp, color = AppTextPrimary)
                        }

                        Box(
                            modifier = Modifier
                                .border(1.dp, AppInactive, RoundedCornerShape(12.dp))
                                .background(AppWhite, RoundedCornerShape(12.dp))
                                .clickable { showSortDialog = true }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Sort, null, modifier = Modifier.size(16.dp), tint = AppTextPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sort", style = MaterialTheme.typography.labelSmall, color = AppTextPrimary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(viewModel.prompts, key = { it.id }) { prompt ->
                    val isSelected = viewModel.selectedIds.contains(prompt.id)

                    AppCard(
                        onClick = {
                            if (viewModel.isSelectionMode) viewModel.toggleSelection(prompt.id)
                            else onEditPrompt(prompt.id)
                        },
                        onLongClick = {
                            if (!viewModel.isSelectionMode) {
                                viewModel.toggleSelectionMode()
                                viewModel.toggleSelection(prompt.id)
                            }
                        },
                        isSelected = isSelected
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            AnimatedVisibility(
                                visible = viewModel.isSelectionMode,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Box(
                                    modifier = Modifier.padding(end = 12.dp).size(24.dp).clip(CircleShape)
                                        .background(if (isSelected) AppPrimary else AppWhite)
                                        .border(2.dp, if (isSelected) AppPrimary else AppInactive, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) Icon(Icons.Default.Check, null, tint = AppWhite, modifier = Modifier.size(16.dp))
                                }
                            }

                            RoundedPentagonBox(
                                modifier = Modifier.size(48.dp),
                                color = AppScreenBg,
                                cornerRadius = 8.dp
                            ) {
                                val (icon, color) = getIntegrationIconAndColor(prompt.integrationType)
                                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(text = prompt.title, style = MaterialTheme.typography.titleMedium, color = AppTextPrimary, modifier = Modifier.weight(1f))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusIndicatorDot(isActive = prompt.isActive)
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = prompt.content,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppTextPrimary.copy(alpha = 0.6f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "USED ${prompt.usageCount} TIMES", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = AppInactive)
                                    if (prompt.lastUsed != null) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "•  LAST: ${prompt.lastUsed}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = AppInactive)
                                    }
                                }
                            }
                        }
                    }
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
            dismissButton = { TextButton(onClick = { showSortDialog = false }) { Text("Close") } },
            containerColor = AppWhite
        )
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
    Divider(color = AppInactive.copy(alpha = 0.3f))
}

@Composable
fun SortArrowButton(isUp: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(32.dp).background(AppScreenBg, RoundedCornerShape(8.dp)).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = if (isUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp), tint = AppInactive)
    }
}