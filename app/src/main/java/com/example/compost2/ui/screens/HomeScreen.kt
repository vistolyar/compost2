package com.example.compost2.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.UploadFile
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.compost2.domain.RecordingItem
import com.example.compost2.domain.RecordingStatus
import com.example.compost2.ui.components.RecordingCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRecorder: () -> Unit,
    onNavigateToPlayer: () -> Unit // Новый колбэк для перехода в плеер
) {
    // Состояние бокового меню (Drawer)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Состояние нижнего меню (BottomSheet для кнопки +)
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // --- ФИКТИВНЫЕ ДАННЫЕ (как в твоем ТЗ) ---
    val dummyItems = listOf(
        RecordingItem("1", "172422112025", RecordingStatus.PENDING, "Today", progress = 0.4f),
        RecordingItem("2", "Моя первая статья...", RecordingStatus.DRAFT, "Yesterday"),
        RecordingItem("3", "Тестовая статья 2", RecordingStatus.PUBLISHED, "20 Aug"),
        RecordingItem("4", "Тестовая статья", RecordingStatus.PUBLISHED, "15 Aug")
    )

    // Боковое меню (Шторка)
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("ComPost Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                NavigationDrawerItem(
                    label = { Text("Prompt Settings") },
                    selected = false,
                    onClick = { /* TODO */ }
                )
                NavigationDrawerItem(
                    label = { Text("API Keys") },
                    selected = false,
                    onClick = { /* TODO */ }
                )
                NavigationDrawerItem(
                    label = { Text("Theme: Light/Dark") },
                    selected = false,
                    onClick = { /* TODO */ }
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
                            Icon(Icons.Default.Menu, contentDescription = "Menu") // Бургер меню
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showBottomSheet = true }) { // Кнопка +
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        ) { paddingValues ->

            // Список карточек
            LazyColumn(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                items(dummyItems) { item ->
                    RecordingCard(
                        item = item,
                        onClick = {
                            // Логика перехода:
                            // Если запись Pending или это просто аудиофайл (id=1) -> идем в Плеер
                            if (item.status == RecordingStatus.PENDING || item.id == "1") {
                                onNavigateToPlayer()
                            } else {
                                // Иначе -> идем в Редактор (пока заглушка, сделаем позже)
                                // onNavigateToEditor()
                            }
                        },
                        onEditClick = { /* Переход в редактор */ },
                        onCancelClick = { /* Отмена обработки */ }
                    )
                }
            }

            // Нижнее всплывающее меню (BottomSheet)
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = sheetState
                ) {
                    Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                        Text("Create new content", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Кнопка "Записать"
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

                        // Кнопка "Добавить файл"
                        ExtendedFloatingActionButton(
                            onClick = { /* TODO: File Picker */ },
                            icon = { Icon(Icons.Default.UploadFile, null) },
                            text = { Text("Upload File (.m4a)") },
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    }
                }
            }
        }
    }
}