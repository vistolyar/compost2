package com.example.compost2.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
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
    onNavigateToPrompts: () -> Unit,
    onNavigateToApiKey: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadRecordings()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val pullRefreshState = rememberPullToRefreshState()

    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            delay(1000)
            viewModel.refresh()
            pullRefreshState.endRefresh()
        }
    }

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
                TextButton(onClick = { viewModel.cancelDelete() }) { Text("Cancel") }
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
                    label = { Text("Prompts") },
                    selected = false,
                    icon = { Icon(Icons.Default.Edit, null) },
                    onClick = { scope.launch { drawerState.close() }; onNavigateToPrompts() }
                )
                NavigationDrawerItem(
                    label = { Text(if (viewModel.isDarkTheme) "Theme: Dark" else "Theme: Light") },
                    selected = false,
                    icon = { Icon(Icons.Default.DarkMode, null) },
                    onClick = { viewModel.toggleTheme() }
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Integrations", modifier = Modifier.padding(start = 16.dp, bottom = 8.dp), style = MaterialTheme.typography.titleSmall)
                NavigationDrawerItem(
                    label = { Text("OpenAI API Key") },
                    selected = false,
                    icon = { Icon(Icons.Default.Key, null) },
                    onClick = { scope.launch { drawerState.close() }; onNavigateToApiKey("openai") }
                )
                NavigationDrawerItem(
                    label = { Text("WordPress Settings") },
                    selected = false,
                    icon = { Icon(Icons.Default.Key, null) },
                    onClick = { scope.launch { drawerState.close() }; onNavigateToApiKey("wordpress") }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ComPost") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, null)
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showBottomSheet = true }) {
                    Icon(Icons.Default.Add, null)
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).nestedScroll(pullRefreshState.nestedScrollConnection)) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    if (!pullRefreshState.isRefreshing && viewModel.recordings.isEmpty()) {
                        item {
                            Text("No recordings yet. Press + to start.", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    items(viewModel.recordings) { item ->
                        RecordingCard(
                            item = item,
                            onClick = {
                                when (item.status) {
                                    RecordingStatus.SAVED -> onNavigateToPlayer(item.id)
                                    RecordingStatus.PROCESSING -> { /* Ждем завершения */ }
                                    RecordingStatus.READY -> onNavigateToPublish(item.id)
                                    RecordingStatus.PUBLISHED -> onNavigateToPublish(item.id)
                                }
                            },
                            onSendToSTT = { onNavigateToSendSTT(item.id) },
                            onCancel = { viewModel.cancelProcessing(item) },
                            onDelete = { viewModel.requestDelete(item) },
                            onPublish = { if(item.status == RecordingStatus.READY) onNavigateToPublish(item.id) },
                            onOpenUrl = { url ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            onSettings = { onNavigateToPublish(item.id) }
                        )
                    }
                }
                PullToRefreshContainer(state = pullRefreshState, modifier = Modifier.align(Alignment.TopCenter))
            }

            if (showBottomSheet) {
                ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState) {
                    Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                        ExtendedFloatingActionButton(
                            onClick = { showBottomSheet = false; onNavigateToRecorder() },
                            icon = { Icon(Icons.Default.Mic, null) },
                            text = { Text("Record Voice") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}