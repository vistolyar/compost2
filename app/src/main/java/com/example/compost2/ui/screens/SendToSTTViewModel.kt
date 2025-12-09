package com.example.compost2.ui.screens

import android.content.Context
import android.util.Base64
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.compost2.data.network.ArticleRequest
import com.example.compost2.data.network.RetrofitClient
import com.example.compost2.domain.RecordingItem
import com.example.compost2.domain.RecordingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SendToSTTViewModel(private val context: Context) : ViewModel() {

    // Убедись, что твой ключ тут остался!
    private val TEMP_OPENAI_KEY = "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"

    var recordingItem by mutableStateOf<RecordingItem?>(null)
        private set

    var prompts by mutableStateOf(listOf(
        "Default: Just transcribe text",
        "SEO Copywriter: Create a structured blog post with headers",
        "LinkedIn Expert: Viral business post style",
        "Summary: Bullet points only"
    ))
        private set

    var selectedPrompt by mutableStateOf(prompts[0])

    var isUploading by mutableStateOf(false)
    var uploadProgress by mutableFloatStateOf(0f)
    var isFinished by mutableStateOf(false)

    var resultTitle: String = ""
    var resultBody: String = ""

    private var uploadJob: Job? = null

    fun loadRecording(fileName: String) {
        val file = File(context.cacheDir, fileName)
        if (file.exists()) {
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
        val item = recordingItem ?: return
        if (isUploading) return

        isUploading = true
        uploadProgress = 0.1f

        uploadJob = viewModelScope.launch {
            try {
                // Мы убрали блок PING - он больше не нужен

                val file = File(item.filePath)

                // 1. Конвертируем в Base64
                val base64Audio = withContext(Dispatchers.IO) {
                    val bytes = file.readBytes()
                    Base64.encodeToString(bytes, Base64.NO_WRAP)
                }

                uploadProgress = 0.3f

                // 2. Создаем запрос
                val request = ArticleRequest(
                    audioBase64 = base64Audio,
                    prompt = selectedPrompt,
                    openaiKey = TEMP_OPENAI_KEY
                )

                // 3. Отправляем
                val response = RetrofitClient.api.uploadAudio(request)

                uploadProgress = 0.9f

                resultTitle = response.title
                resultBody = response.content

                uploadProgress = 1.0f
                isFinished = true

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                isUploading = false
                uploadProgress = 0f
            }
        }
    }

    fun cancelProcessing() {
        uploadJob?.cancel()
        isUploading = false
        uploadProgress = 0f
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
                SendToSTTViewModel(context)
            }
        }
    }
}