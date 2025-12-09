package com.example.compost2.ui.screens

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.compost2.domain.RecordingItem
import com.example.compost2.domain.RecordingStatus
import java.io.File

class HomeViewModel(private val context: Context) : ViewModel() {

    var recordings by mutableStateOf<List<RecordingItem>>(emptyList())
        private set

    var itemToDelete by mutableStateOf<RecordingItem?>(null)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    fun loadRecordings() {
        val dir = context.cacheDir
        val files = dir.listFiles { file ->
            file.extension == "m4a" && file.name != "temp_recording.m4a"
        } ?: emptyArray()

        val sortedFiles = files.sortedByDescending { it.lastModified() }
        val currentItemsMap = recordings.associateBy { it.id }

        recordings = sortedFiles.map { file ->
            val existingItem = currentItemsMap[file.name]
            if (existingItem != null) {
                existingItem
            } else {
                RecordingItem(
                    id = file.name,
                    name = parseFileNameToDisplay(file.name),
                    status = RecordingStatus.SAVED,
                    filePath = file.absolutePath
                )
            }
        }
    }

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
            recordings = recordings.filter { it.id != item.id }
        }
        itemToDelete = null
    }

    fun cancelDelete() {
        itemToDelete = null
    }

    // --- Методы для смены статусов (используются в навигации) ---

    fun sendToSTT(item: RecordingItem) {
        updateItemStatus(item, RecordingStatus.PROCESSING)
    }

    fun cancelProcessing(item: RecordingItem) {
        updateItemStatus(item, RecordingStatus.SAVED)
    }

    fun mockFinishProcessing(item: RecordingItem) {
        val newItem = item.copy(
            status = RecordingStatus.READY,
            articleTitle = "AI Generated Article Title", // Заглушка
            promptName = "Default Persona"
        )
        updateItemInList(newItem)
    }

    fun mockPublish(item: RecordingItem) {
        val newItem = item.copy(
            status = RecordingStatus.PUBLISHED,
            publicUrl = "https://wordpress.com/post/123" // Заглушка
        )
        updateItemInList(newItem)
    }

    private fun updateItemStatus(item: RecordingItem, newStatus: RecordingStatus) {
        val newItem = item.copy(status = newStatus)
        updateItemInList(newItem)
    }

    private fun updateItemInList(newItem: RecordingItem) {
        recordings = recordings.map {
            if (it.id == newItem.id) newItem else it
        }
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