package com.example.compost2.ui.screens

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.compost2.domain.RecordingItem
import com.example.compost2.domain.RecordingStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

class SendToSTTViewModel(private val context: Context) : ViewModel() {

    var recordingItem by mutableStateOf<RecordingItem?>(null)
        private set

    // Список промптов (пока заглушка, позже будем брать из WP/Settings) [cite: 190]
    var prompts by mutableStateOf(listOf(
        "Default: Просто транскрибация",
        "SEO Copywriter: Пиши как эксперт",
        "Summary: Краткое содержание",
        "LinkedIn Post: Деловой стиль"
    ))
        private set

    var selectedPrompt by mutableStateOf(prompts[0])

    // Состояния процесса [cite: 192]
    var isUploading by mutableStateOf(false)
    var uploadProgress by mutableFloatStateOf(0f)

    // Флаг, что процесс завершен (чтобы UI знал, что пора закрываться)
    var isFinished by mutableStateOf(false)

    private var uploadJob: Job? = null

    fun loadRecording(fileName: String) {
        val file = File(context.cacheDir, fileName)
        if (file.exists()) {
            // Создаем временный объект для отображения
            recordingItem = RecordingItem(
                id = file.name,
                name = parseFileNameToDisplay(file.name),
                status = RecordingStatus.SAVED,
                filePath = file.absolutePath
            )
        }
    }

    fun selectPrompt(prompt: String) {
        selectedPrompt = prompt
    }

    fun startProcessing() {
        if (isUploading) return

        isUploading = true
        uploadProgress = 0f

        // Симуляция загрузки и обработки (5 секунд)
        uploadJob = viewModelScope.launch {
            while (uploadProgress < 1f) {
                delay(100)
                uploadProgress += 0.02f // +2% каждые 100мс
            }
            isFinished = true
            isUploading = false
        }
    }

    fun cancelProcessing() {
        uploadJob?.cancel()
        isUploading = false
        uploadProgress = 0f
    }

    // Хелпер для имени файла (дубликат логики, можно вынести в Utils, но пока так)
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
                SendToSTTViewModel(context)
            }
        }
    }
}