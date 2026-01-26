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
import com.example.compost2.data.PromptsRepository
import com.example.compost2.data.SettingsDataStore
import com.example.compost2.data.network.ProcessTextRequest
import com.example.compost2.data.network.RetrofitClient
import com.example.compost2.data.network.TranscribeRequest
import com.example.compost2.domain.RecordingItem
import com.example.compost2.domain.RecordingStatus
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class HomeViewModel(private val context: Context) : ViewModel() {

    private val dataStore = SettingsDataStore(context)
    private val promptsRepository = PromptsRepository(context)
    private val gson = Gson()
    private val activeJobs = mutableMapOf<String, Job>()

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
        loadRecordings()
    }

    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
        viewModelScope.launch { dataStore.saveTheme(isDarkTheme) }
    }

    fun loadRecordings() {
        val dir = context.cacheDir
        val files = dir.listFiles { file ->
            file.extension == "m4a" && file.name != "temp_recording.m4a"
        } ?: emptyArray()

        val sortedFiles = files.sortedByDescending { it.lastModified() }

        val list = sortedFiles.map { file ->
            val metaFile = File(file.path + ".json")
            if (metaFile.exists()) {
                try {
                    val reader = FileReader(metaFile)
                    val savedItem = gson.fromJson(reader, RecordingItem::class.java)
                    reader.close()
                    savedItem.copy(filePath = file.absolutePath)
                } catch (e: Exception) {
                    createDefaultItem(file)
                }
            } else {
                createDefaultItem(file)
            }
        }

        recordings = list

        // Запуск обработки только для тех, кто в статусе PROCESSING
        // Если статус SAVED (Recorded), мы ничего не делаем, ждем действий пользователя
        list.filter { it.status == RecordingStatus.PROCESSING }.forEach { item ->
            startProcessingInBackground(item)
        }
    }

    // --- ГЛАВНАЯ ЛОГИКА V2.0 ---
    private fun startProcessingInBackground(item: RecordingItem) {
        if (activeJobs.containsKey(item.id)) return

        val job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val savedKey = dataStore.openAiKey.first() ?: return@launch

                // Получаем текст промпта
                val promptText = promptsRepository.getPrompts().find { it.title == item.promptName }?.content
                    ?: "Transcribe exactly what is said."

                val file = File(item.filePath)
                if (!file.exists()) return@launch

                // --- ШАГ 1: ЗАГРУЗКА (Upload Phase) ---
                val uploadInfo = RetrofitClient.api.getUploadUrl()
                val requestBody = RequestBody.create("audio/mp4".toMediaTypeOrNull(), file)
                val uploadResponse = RetrofitClient.api.uploadFileToS3(uploadInfo.uploadUrl, requestBody)

                if (!uploadResponse.isSuccessful) {
                    throw Exception("S3 Upload failed: ${uploadResponse.code()}")
                }

                // --- ШАГ 2: ТРАНСКРИБАЦИЯ (Transcription Phase) ---
                val transcribeRequest = TranscribeRequest(
                    fileKey = uploadInfo.fileKey,
                    openAiKey = savedKey
                )

                val transcribeResponse = RetrofitClient.api.transcribe(transcribeRequest)
                val rawText = transcribeResponse.rawText

                // Сохраняем Raw Text локально (Промежуточное сохранение)
                val itemWithRaw = item.copy(rawTranscription = rawText)
                saveMetadataToDisk(itemWithRaw)

                // Обновляем UI (можно было бы показать статус "Analyzing...")
                withContext(Dispatchers.Main) {
                    updateItemInList(itemWithRaw)
                }

                // --- ШАГ 3: ПРОЦЕССИНГ (Action Phase) ---
                val processRequest = ProcessTextRequest(
                    rawText = rawText,
                    prompt = promptText,
                    openAiKey = savedKey
                )

                val processResponse = RetrofitClient.api.processText(processRequest)

                withContext(Dispatchers.Main) {
                    onAiResultReceived(itemWithRaw, processResponse.title, processResponse.content)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    // Если ошибка, возвращаем статус SAVED (Recorded), чтобы можно было повторить
                    updateItemStatus(item, RecordingStatus.SAVED)
                }
            } finally {
                activeJobs.remove(item.id)
            }
        }
        activeJobs[item.id] = job
    }

    private fun createDefaultItem(file: File): RecordingItem {
        return RecordingItem(
            id = file.name,
            name = file.name.substringBeforeLast("."),
            status = RecordingStatus.SAVED,
            filePath = file.absolutePath
        )
    }

    fun cancelProcessing(item: RecordingItem) {
        activeJobs[item.id]?.cancel()
        activeJobs.remove(item.id)
        updateItemStatus(item, RecordingStatus.SAVED)
    }

    fun onAiResultReceived(item: RecordingItem, title: String, content: String) {
        val newItem = item.copy(
            status = RecordingStatus.TRANSCRIBED,
            articleTitle = title,
            articleContent = content
        )
        updateItemInList(newItem)
    }

    fun onPublishedSuccess(item: RecordingItem, url: String, wpId: Int) {
        val newItem = item.copy(
            status = RecordingStatus.PUBLISHED,
            publicUrl = url,
            wordpressId = wpId
        )
        updateItemInList(newItem)
    }

    fun updateArticleData(id: String, title: String, content: String) {
        val item = recordings.find { it.id == id } ?: return
        val newItem = item.copy(articleTitle = title, articleContent = content)
        updateItemInList(newItem)
    }

    private fun updateItemStatus(item: RecordingItem, newStatus: RecordingStatus) {
        updateItemInList(item.copy(status = newStatus))
    }

    private fun updateItemInList(newItem: RecordingItem) {
        recordings = recordings.map { if (it.id == newItem.id) newItem else it }
        saveMetadataToDisk(newItem)
    }

    private fun saveMetadataToDisk(item: RecordingItem) {
        try {
            val metaFile = File(item.filePath + ".json")
            FileWriter(metaFile).use { gson.toJson(item, it) }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun refresh() {
        isRefreshing = true
        loadRecordings()
        isRefreshing = false
    }

    fun requestDelete(item: RecordingItem) { itemToDelete = item }

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

    fun cancelDelete() { itemToDelete = null }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer { HomeViewModel(context) }
        }
    }
}