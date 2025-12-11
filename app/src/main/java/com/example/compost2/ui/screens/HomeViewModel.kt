package com.example.compost2.ui.screens

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.compost2.data.SettingsDataStore
import com.example.compost2.domain.RecordingItem
import com.example.compost2.domain.RecordingStatus
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class HomeViewModel(private val context: Context) : ViewModel() {

    private val dataStore = SettingsDataStore(context)
    private val gson = Gson()

    var recordings by mutableStateOf<List<RecordingItem>>(emptyList())
        private set

    var itemToDelete by mutableStateOf<RecordingItem?>(null)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var isDarkTheme by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            isDarkTheme = dataStore.isDarkTheme.first() ?: false
        }
    }

    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
        viewModelScope.launch {
            dataStore.saveTheme(isDarkTheme)
        }
    }

    fun loadRecordings() {
        val dir = context.cacheDir
        val files = dir.listFiles { file ->
            file.extension == "m4a" && file.name != "temp_recording.m4a"
        } ?: emptyArray()

        val sortedFiles = files.sortedByDescending { it.lastModified() }
        val currentItemsMap = recordings.associateBy { it.id }

        recordings = sortedFiles.map { file ->
            val metaFile = File(file.path + ".json")
            if (metaFile.exists()) {
                try {
                    val reader = FileReader(metaFile)
                    val savedItem = gson.fromJson(reader, RecordingItem::class.java)
                    reader.close()
                    savedItem.copy(filePath = file.absolutePath)
                } catch (e: Exception) {
                    e.printStackTrace()
                    createDefaultItem(file)
                }
            } else {
                val existingItem = currentItemsMap[file.name]
                existingItem?.copy(filePath = file.absolutePath) ?: createDefaultItem(file)
            }
        }
    }

    private fun createDefaultItem(file: File): RecordingItem {
        return RecordingItem(
            id = file.name,
            name = parseFileNameToDisplay(file.name),
            status = RecordingStatus.SAVED,
            filePath = file.absolutePath
        )
    }

    // --- Обновление данных из Редактора ---
    fun updateArticleData(id: String, title: String, content: String) {
        val index = recordings.indexOfFirst { it.id == id }
        if (index != -1) {
            val oldItem = recordings[index]
            val newItem = oldItem.copy(
                articleTitle = title,
                articleContent = content,
                status = if (oldItem.status == RecordingStatus.SAVED) RecordingStatus.READY else oldItem.status
            )
            updateItemInList(newItem)
        }
    }

    // --- Результат от AI ---
    fun onAiResultReceived(item: RecordingItem, title: String, content: String) {
        val newItem = item.copy(
            status = RecordingStatus.READY,
            articleTitle = title,
            articleContent = content
        )
        updateItemInList(newItem)
    }

    // --- Успешная публикация (Обновлено с ID) ---
    fun onPublishedSuccess(item: RecordingItem, url: String, wpId: Int) {
        val newItem = item.copy(
            status = RecordingStatus.PUBLISHED,
            publicUrl = url,
            wordpressId = wpId // Сохраняем ID, чтобы потом делать Update
        )
        updateItemInList(newItem)
    }

    // --- Симуляция (для тестов, если нужно) ---
    fun mockFinishProcessing(item: RecordingItem) {
        val newItem = item.copy(
            status = RecordingStatus.READY,
            articleTitle = "AI Generated Article Title",
            promptName = "Default Persona"
        )
        updateItemInList(newItem)
    }

    fun mockPublish(item: RecordingItem) {
        // Устаревшая заглушка
        updateItemInList(item.copy(status = RecordingStatus.PUBLISHED))
    }

    // --- Статусы ---
    fun sendToSTT(item: RecordingItem) {
        updateItemStatus(item, RecordingStatus.PROCESSING)
    }

    fun cancelProcessing(item: RecordingItem) {
        updateItemStatus(item, RecordingStatus.SAVED)
    }

    private fun updateItemStatus(item: RecordingItem, newStatus: RecordingStatus) {
        val newItem = item.copy(status = newStatus)
        updateItemInList(newItem)
    }

    private fun updateItemInList(newItem: RecordingItem) {
        recordings = recordings.map {
            if (it.id == newItem.id) newItem else it
        }
        saveMetadataToDisk(newItem)
    }

    private fun saveMetadataToDisk(item: RecordingItem) {
        try {
            val metaFile = File(item.filePath + ".json")
            val writer = FileWriter(metaFile)
            gson.toJson(item, writer)
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Удаление ---
    fun refresh() {
        isRefreshing = true
        loadRecordings()
        isRefreshing = false
    }

    fun requestDelete(item: RecordingItem) {
        itemToDelete = item
    }

    fun confirmDelete() {
        itemToDelete?.let { item ->
            val file = File(item.filePath)
            if (file.exists()) file.delete()

            val metaFile = File(item.filePath + ".json")
            if (metaFile.exists()) metaFile.delete()

            recordings = recordings.filter { it.id != item.id }
        }
        itemToDelete = null
    }

    fun cancelDelete() {
        itemToDelete = null
    }

    private fun parseFileNameToDisplay(fileName: String): String {
        return try {
            val nameWithoutExt = fileName.substringBeforeLast(".")
            if (!nameWithoutExt.contains("-")) return nameWithoutExt
            val parts = nameWithoutExt.split("_")
            val dateTimePart = parts[0]
            val durationPart = parts[1]
            val dateComponents = dateTimePart.split("-")
            "${dateComponents[0]}.${dateComponents[1]}.${dateComponents[2]} ${dateComponents[3]}:${dateComponents[4]}   ${durationPart.replace("-", ":")}"
        } catch (e: Exception) {
            fileName
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(context)
            }
        }
    }
}