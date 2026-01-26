package com.example.compost2.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.example.compost2.domain.RecordingStatus
import com.example.compost2.ui.components.HeroPentagonButton
import com.example.compost2.ui.components.PentagonShape
import com.example.compost2.ui.components.RecordingCard
import com.example.compost2.ui.theme.*
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
            title = { Text("Delete Recording", style = MaterialTheme.typography.titleLarge) },
            text = { Text("Are you sure you want to delete this?", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) { Text("Cancel", style = MaterialTheme.typography.labelLarge) }
            },
            containerColor = AppWhite
        )
    }

    // Фон дашборда
    val backgroundColor = AppScreenBg

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                // --- HEADER С КНОПКОЙ ЗАКРЫТИЯ ---
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Кнопка закрытия (Крестик)
                    IconButton(
                        onClick = { scope.launch { drawerState.close() } },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Menu", tint = Color.Gray)
                    }

                    // Профиль
                    Column(modifier = Modifier.padding(start = 30.dp, end = 30.dp, top = 60.dp, bottom = 30.dp)) {
                        if (googleAccount == null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE0E0E5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, null, tint = Color.Gray)
                                }
                                Spacer(modifier = Modifier.width(15.dp))
                                Text(
                                    text = "Sign In",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.clickable {
                                        scope.launch { drawerState.close() }
                                        onNavigateToSettings()
                                    }
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (googleAccount?.photoUrl != null) {
                                    AsyncImage(
                                        model = googleAccount!!.photoUrl,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.size(50.dp).clip(CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE0E0E5)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = googleAccount?.displayName?.take(1) ?: "U",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(15.dp))
                                Column {
                                    Text(
                                        text = googleAccount?.displayName ?: "User",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = googleAccount?.email ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Divider(color = Color(0xFFF0F0F2), thickness = 1.dp)

                // Menu Items
                Column(modifier = Modifier.padding(horizontal = 15.dp)) {
                    NavigationDrawerItem(
                        label = { Text("Prompts", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold) },
                        selected = false,
                        icon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp)) },
                        onClick = { scope.launch { drawerState.close() }; onNavigateToPrompts() },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    NavigationDrawerItem(
                        label = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Theme", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Switch(
                                    checked = viewModel.isDarkTheme,
                                    onCheckedChange = { viewModel.toggleTheme() },
                                    modifier = Modifier.scale(0.8f)
                                )
                            }
                        },
                        selected = false,
                        icon = { Icon(Icons.Default.DarkMode, null, modifier = Modifier.size(20.dp)) },
                        onClick = { viewModel.toggleTheme() },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                }

                // Integrations
                Column(modifier = Modifier.padding(start = 30.dp, end = 30.dp, top = 25.dp)) {
                    Text(
                        text = "INTEGRATIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF999999),
                        modifier = Modifier.padding(bottom = 15.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        DrawerPentagonIcon(
                            icon = Icons.Default.Email,
                            color = Color(0xFFEA4335),
                            onClick = { }
                        )
                        DrawerPentagonIcon(
                            icon = Icons.Default.DateRange,
                            color = Color(0xFF34A853),
                            onClick = { }
                        )
                        DrawerPentagonIcon(
                            icon = Icons.Default.CheckCircle,
                            color = Color(0xFF4285F4),
                            onClick = { }
                        )

                        // Кнопка ADD (Простой плюс)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable {
                                    scope.launch { drawerState.close() }
                                    onNavigateToSettings()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Integration",
                                tint = Color.DarkGray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(25.dp))

                // API Settings
                Column(modifier = Modifier.padding(horizontal = 15.dp)) {
                    Text(
                        text = "API SETTINGS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF999999),
                        modifier = Modifier.padding(start = 15.dp, bottom = 8.dp)
                    )

                    NavigationDrawerItem(
                        label = { Text("OpenAI API Key", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold) },
                        selected = false,
                        icon = { Icon(Icons.Default.Key, null, modifier = Modifier.size(20.dp)) },
                        onClick = { scope.launch { drawerState.close() }; onNavigateToApiKey("openai") },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                    NavigationDrawerItem(
                        label = { Text("WordPress Settings", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold) },
                        selected = false,
                        icon = { Icon(Icons.Default.Public, null, modifier = Modifier.size(20.dp)) },
                        onClick = { scope.launch { drawerState.close() }; onNavigateToApiKey("wordpress") },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                }
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
            floatingActionButtonPosition = FabPosition.Center,
            containerColor = backgroundColor
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(pullRefreshState.nestedScrollConnection)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            change.consume()
                            if (dragAmount < -20) onNavigateToRecorder()
                            if (dragAmount > 20) scope.launch { drawerState.open() }
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
                                Text("No recordings yet.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
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

                PullToRefreshContainer(state = pullRefreshState, modifier = Modifier.align(Alignment.TopCenter))
            }
        }
    }
}

@Composable
fun DrawerPentagonIcon(
    icon: ImageVector,
    color: Color,
    iconTint: Color? = null,
    onClick: () -> Unit
) {
    val isAddButton = iconTint != null
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(PentagonShape())
            .background(if (isAddButton) color else Color(0xFFF4F4F6))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint ?: color,
            modifier = Modifier.size(24.dp)
        )
    }
}

fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
)