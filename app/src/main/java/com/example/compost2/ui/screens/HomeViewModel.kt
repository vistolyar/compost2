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

    // Состояние для диалога удаления
    var itemToDelete by mutableStateOf<RecordingItem?>(null)
        private set

    // Состояние обновления (для спиннера)
    var isRefreshing by mutableStateOf(false)
        private set

    fun loadRecordings() {
        val dir = context.cacheDir
        val files = dir.listFiles { file ->
            file.extension == "m4a" && file.name != "temp_recording.m4a"
        } ?: emptyArray()

        val sortedFiles = files.sortedByDescending { it.lastModified() }

        // Если список пустой или мы делаем принудительное обновление - пересоздаем список
        // (Упрощенная логика: всегда перезагружаем с диска)
        val currentItemsMap = recordings.associateBy { it.id }

        recordings = sortedFiles.map { file ->
            // Если такой файл уже был в списке (например, мы меняли ему статус),
            // пытаемся сохранить старый объект, чтобы не сбросить статус на SAVED
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

    // Метод для Swipe-to-Refresh
    fun refresh() {
        isRefreshing = true
        loadRecordings()
        isRefreshing = false
    }

    // --- ЛОГИКА УДАЛЕНИЯ ---

    fun requestDelete(item: RecordingItem) {
        itemToDelete = item
    }

    fun confirmDelete() {
        itemToDelete?.let { item ->
            val file = File(item.filePath)
            if (file.exists()) {
                file.delete()
            }
            recordings = recordings.filter { it.id != item.id }
        }
        itemToDelete = null
    }

    fun cancelDelete() {
        itemToDelete = null
    }

    // --- СИМУЛЯЦИЯ ---

    fun sendToSTT(item: RecordingItem) {
        updateItemStatus(item, RecordingStatus.PROCESSING)
    }

    fun cancelProcessing(item: RecordingItem) {
        updateItemStatus(item, RecordingStatus.SAVED)
    }

    fun mockFinishProcessing(item: RecordingItem) {
        val newItem = item.copy(
            status = RecordingStatus.READY,
            articleTitle = "How to optimize Android apps using AI",
            promptName = "SEO Copywriter"
        )
        updateItemInList(newItem)
    }

    fun mockPublish(item: RecordingItem) {
        val newItem = item.copy(
            status = RecordingStatus.PUBLISHED,
            publicUrl = "https://mysite.com/blog/android-ai-optimization-guide-2025"
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
        try {
            val nameWithoutExt = fileName.substringBeforeLast(".")
            if (!nameWithoutExt.contains("-")) return nameWithoutExt
            val parts = nameWithoutExt.split("_")
            if (parts.size < 2) return nameWithoutExt

            val dateTimePart = parts[0]
            val durationPart = parts[1]

            val dateComponents = dateTimePart.split("-")
            val prettyDate = "${dateComponents[0]}.${dateComponents[1]}.${dateComponents[2]} ${dateComponents[3]}:${dateComponents[4]}"
            val prettyDuration = durationPart.replace("-", ":")

            return "$prettyDate   $prettyDuration"
        } catch (e: Exception) {
            return fileName
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