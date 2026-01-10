package com.example.compost2.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.example.compost2.domain.RecordingStatus
import com.example.compost2.ui.components.HeroPentagonButton
import com.example.compost2.ui.components.RecordingCard
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
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
    onNavigateToApiKey: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var googleAccount by remember { mutableStateOf<GoogleSignInAccount?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadRecordings()
                googleAccount = GoogleSignIn.getLastSignedInAccount(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
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

                if (googleAccount == null) {
                    NavigationDrawerItem(
                        label = { Text("Sign in with Google") },
                        selected = false,
                        icon = { Icon(Icons.Default.AccountCircle, null) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToSettings()
                        }
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { drawerState.close() }
                                onNavigateToSettings()
                            }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (googleAccount?.photoUrl != null) {
                            AsyncImage(
                                model = googleAccount!!.photoUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier.size(32.dp).clip(CircleShape)
                            )
                        } else {
                            Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = googleAccount?.displayName ?: "User",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

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
                Text("API Settings", modifier = Modifier.padding(start = 16.dp, bottom = 8.dp), style = MaterialTheme.typography.titleSmall)
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
                    title = { Text("ComPost", style = MaterialTheme.typography.headlineSmall) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                HeroPentagonButton(
                    onClick = { onNavigateToRecorder() },
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            },
            floatingActionButtonPosition = FabPosition.Center
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(pullRefreshState.nestedScrollConnection)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            change.consume()
                            if (dragAmount < -20) {
                                onNavigateToRecorder()
                            }
                            if (dragAmount > 20) {
                                scope.launch { drawerState.open() }
                            }
                        }
                    }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding() + 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 100.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
                ) {
                    if (!pullRefreshState.isRefreshing && viewModel.recordings.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No recordings yet.", color = Color.Gray)
                            }
                        }
                    }

                    items(viewModel.recordings) { item ->
                        RecordingCard(
                            item = item,
                            onClick = {
                                when (item.status) {
                                    RecordingStatus.SAVED -> onNavigateToPlayer(item.id)
                                    RecordingStatus.PROCESSING -> { }
                                    RecordingStatus.READY -> onNavigateToPublish(item.id)
                                    RecordingStatus.PUBLISHED -> onNavigateToPublish(item.id)
                                    RecordingStatus.TRANSCRIBED -> onNavigateToPublish(item.id)
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

                // ИСПРАВЛЕНИЕ:
                // Мы вернули индикатор в основной Box и УБРАЛИ padding.
                // Теперь он будет сидеть наверху (скрытый) и появляться только при свайпе вниз.
                PullToRefreshContainer(
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}