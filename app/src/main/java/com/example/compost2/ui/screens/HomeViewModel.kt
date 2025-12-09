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
import com.google.gson.Gson
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class HomeViewModel(private val context: Context) : ViewModel() {

    var recordings by mutableStateOf<List<RecordingItem>>(emptyList())
        private set

    var itemToDelete by mutableStateOf<RecordingItem?>(null)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    // Используем Gson для сохранения данных в JSON
    private val gson = Gson()

    fun loadRecordings() {
        val dir = context.cacheDir
        val files = dir.listFiles { file ->
            // Ищем только аудиофайлы, json'ы подгрузим отдельно
            file.extension == "m4a" && file.name != "temp_recording.m4a"
        } ?: emptyArray()

        val sortedFiles = files.sortedByDescending { it.lastModified() }

        recordings = sortedFiles.map { file ->
            // Проверяем, есть ли сохраненные данные (sidecar файл)
            val metaFile = File(file.path + ".json")
            if (metaFile.exists()) {
                try {
                    // Если есть json, читаем статус и текст оттуда
                    val reader = FileReader(metaFile)
                    val savedItem = gson.fromJson(reader, RecordingItem::class.java)
                    reader.close()
                    // Важно: путь к файлу берем реальный (на случай если он изменился), остальное из json
                    savedItem.copy(filePath = file.absolutePath)
                } catch (e: Exception) {
                    e.printStackTrace()
                    createDefaultItem(file)
                }
            } else {
                // Если json нет, создаем "чистую" запись
                createDefaultItem(file)
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

    // --- СОХРАНЕНИЕ ДАННЫХ ---

    // Этот метод будем вызывать из Редактора при каждом изменении
    fun updateArticleData(id: String, title: String, content: String) {
        val index = recordings.indexOfFirst { it.id == id }
        if (index != -1) {
            val oldItem = recordings[index]
            val newItem = oldItem.copy(
                articleTitle = title,
                articleContent = content,
                // Если мы начали править, то статус как минимум READY (или оставляем текущий)
                status = if (oldItem.status == RecordingStatus.SAVED) RecordingStatus.READY else oldItem.status
            )

            // 1. Обновляем список в памяти (для UI)
            val newList = recordings.toMutableList()
            newList[index] = newItem
            recordings = newList

            // 2. Сохраняем на диск
            saveMetadataToDisk(newItem)
        }
    }

    fun onAiResultReceived(item: RecordingItem, title: String, content: String) {
        val newItem = item.copy(
            status = RecordingStatus.READY,
            articleTitle = title,
            articleContent = content
        )
        updateItemInList(newItem)
    }

    // Служебный метод записи на диск
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

    // --- Остальные методы ---

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
            // Удаляем аудио
            val file = File(item.filePath)
            if (file.exists()) file.delete()

            // Удаляем метаданные (json)
            val metaFile = File(item.filePath + ".json")
            if (metaFile.exists()) metaFile.delete()

            recordings = recordings.filter { it.id != item.id }
        }
        itemToDelete = null
    }

    fun cancelDelete() {
        itemToDelete = null
    }

    fun sendToSTT(item: RecordingItem) {
        updateItemStatus(item, RecordingStatus.PROCESSING)
    }

    fun cancelProcessing(item: RecordingItem) {
        updateItemStatus(item, RecordingStatus.SAVED)
    }

    fun mockFinishProcessing(item: RecordingItem) {
        val newItem = item.copy(
            status = RecordingStatus.READY,
            articleTitle = "AI Generated Article Title",
            promptName = "Default Persona"
        )
        updateItemInList(newItem)
    }

    fun mockPublish(item: RecordingItem) {
        val newItem = item.copy(
            status = RecordingStatus.PUBLISHED,
            publicUrl = "https://wordpress.com/post/123"
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
        // При любом обновлении статуса - сохраняем на диск!
        saveMetadataToDisk(newItem)
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