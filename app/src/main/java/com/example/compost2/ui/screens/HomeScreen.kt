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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compost2.domain.RecordingStatus
import com.example.compost2.ui.components.RecordingCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRecorder: () -> Unit,
    onNavigateToPlayer: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.provideFactory(context))

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // --- PULL TO REFRESH STATE ---
    val pullRefreshState = rememberPullToRefreshState()

    // Логика обновления при свайпе
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            // Искусственная задержка, чтобы было видно колесико (UX)
            delay(1000)
            viewModel.refresh()
            pullRefreshState.endRefresh()
        }
    }

    // Начальная загрузка
    LaunchedEffect(Unit) {
        viewModel.loadRecordings()
    }

    // --- ДИАЛОГ УДАЛЕНИЯ ---
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
                Text(
                    text = "ComPost Menu",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
                Divider()

                NavigationDrawerItem(
                    label = { Text("Prompt Settings") },
                    selected = false,
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    onClick = {
                        scope.launch { drawerState.close() }
                        Toast.makeText(context, "Prompt Settings clicked", Toast.LENGTH_SHORT).show()
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Theme: Light/Dark") },
                    selected = false,
                    icon = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                    onClick = { scope.launch { drawerState.close() } }
                )

                NavigationDrawerItem(
                    label = { Text("Language") },
                    selected = false,
                    icon = { Icon(Icons.Default.Language, contentDescription = null) },
                    onClick = { scope.launch { drawerState.close() } }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Integrations", modifier = Modifier.padding(start = 16.dp, bottom = 8.dp), style = MaterialTheme.typography.titleSmall)

                NavigationDrawerItem(
                    label = { Text("OpenAI API Key") },
                    selected = false,
                    icon = { Icon(Icons.Default.Key, contentDescription = null) },
                    onClick = { scope.launch { drawerState.close() } }
                )

                NavigationDrawerItem(
                    label = { Text("WordPress API Key") },
                    selected = false,
                    icon = { Icon(Icons.Default.Key, contentDescription = null) },
                    onClick = { scope.launch { drawerState.close() } }
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

            // Оборачиваем LazyColumn в Box для работы PullToRefresh
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .nestedScroll(pullRefreshState.nestedScrollConnection) // Связываем скролл с Refresh
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
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
                                    RecordingStatus.PROCESSING -> { }
                                    RecordingStatus.READY -> { }
                                    RecordingStatus.PUBLISHED -> { }
                                }
                            },
                            onSendToSTT = { viewModel.sendToSTT(item) },
                            onCancel = { viewModel.cancelProcessing(item) },
                            onDelete = { viewModel.requestDelete(item) },
                            onPublish = {
                                if(item.status == RecordingStatus.PROCESSING) viewModel.mockFinishProcessing(item)
                                else if(item.status == RecordingStatus.READY) viewModel.mockPublish(item)
                            },
                            onOpenUrl = { url ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                // Индикатор загрузки (колесико)
                PullToRefreshContainer(
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
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