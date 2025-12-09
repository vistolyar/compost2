package com.example.compost2.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.compost2.domain.RecordingStatus
import com.example.compost2.ui.components.RecordingCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToRecorder: () -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    onNavigateToSendSTT: (String) -> Unit,
    onNavigateToPublish: (String) -> Unit,
    onNavigateToPrompts: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Обновляем список при возврате на экран
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadRecordings()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val pullRefreshState = rememberPullToRefreshState()

    // Логика Pull-to-Refresh
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            delay(1000)
            viewModel.refresh()
            pullRefreshState.endRefresh()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadRecordings()
    }

    // Диалог удаления
    if (viewModel.itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Delete Recording") },
            text = { Text("Are you sure you want to delete this? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("ComPost Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall)
                Divider()
                NavigationDrawerItem(
                    label = { Text("Prompt Settings") }, selected = false,
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToPrompts()
                    }
                )
                NavigationDrawerItem(label = { Text("Theme: Light/Dark") }, selected = false, icon = { Icon(Icons.Default.DarkMode, null) }, onClick = { })
                NavigationDrawerItem(label = { Text("Language") }, selected = false, icon = { Icon(Icons.Default.Language, null) }, onClick = { })
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(label = { Text("OpenAI API Key") }, selected = false, icon = { Icon(Icons.Default.Key, null) }, onClick = { })
                NavigationDrawerItem(label = { Text("WordPress API Key") }, selected = false, icon = { Icon(Icons.Default.Key, null) }, onClick = { })
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ComPost") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showBottomSheet = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .nestedScroll(pullRefreshState.nestedScrollConnection)
            ) {
                // --- ГЛАВНОЕ ИСПРАВЛЕНИЕ: Используем viewModel.recordings ---
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    if (!pullRefreshState.isRefreshing && viewModel.recordings.isEmpty()) {
                        item {
                            Text(
                                text = "No recordings yet. Pull down to refresh or press + to start.",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(viewModel.recordings) { item ->
                        RecordingCard(
                            item = item,
                            onClick = {
                                when (item.status) {
                                    RecordingStatus.SAVED -> onNavigateToPlayer(item.id)
                                    RecordingStatus.PROCESSING -> onNavigateToSendSTT(item.id)
                                    RecordingStatus.READY -> onNavigateToPublish(item.id) // Ведет в Редактор
                                    RecordingStatus.PUBLISHED -> onNavigateToPublish(item.id)
                                }
                            },
                            onSendToSTT = { onNavigateToSendSTT(item.id) },
                            onCancel = { viewModel.cancelProcessing(item) },
                            onDelete = { viewModel.requestDelete(item) },
                            onPublish = {
                                if(item.status == RecordingStatus.PROCESSING) viewModel.mockFinishProcessing(item)
                                else if(item.status == RecordingStatus.READY) onNavigateToPublish(item.id)
                            },
                            onOpenUrl = { url ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                PullToRefreshContainer(state = pullRefreshState, modifier = Modifier.align(Alignment.TopCenter))
            }

            if (showBottomSheet) {
                ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState) {
                    Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                showBottomSheet = false
                                onNavigateToRecorder()
                            },
                            icon = { Icon(Icons.Default.Mic, null) },
                            text = { Text("Record Voice") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ExtendedFloatingActionButton(
                            onClick = { },
                            icon = { Icon(Icons.Default.UploadFile, null) },
                            text = { Text("Upload File") },
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    }
                }
            }
        }
    }
}