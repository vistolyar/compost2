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
import com.example.compost2.data.PromptsRepository
import com.example.compost2.data.SettingsDataStore
import com.example.compost2.data.network.ArticleRequest
import com.example.compost2.data.network.RetrofitClient
import com.example.compost2.domain.PromptItem
import com.example.compost2.domain.RecordingItem
import com.example.compost2.domain.RecordingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SendToSTTViewModel(private val context: Context) : ViewModel() {

    private val dataStore = SettingsDataStore(context)
    private val promptsRepository = PromptsRepository(context)

    var recordingItem by mutableStateOf<RecordingItem?>(null)
        private set

    // Список теперь берется из репозитория
    var prompts by mutableStateOf(emptyList<PromptItem>())
        private set

    // Выбранный промпт - теперь это объект
    var selectedPrompt by mutableStateOf<PromptItem?>(null)

    // Состояния для диалога создания промпта
    var showAddDialog by mutableStateOf(false)
    var newPromptTitle by mutableStateOf("")
    var newPromptContent by mutableStateOf("")

    var isUploading by mutableStateOf(false)
    var uploadProgress by mutableFloatStateOf(0f)
    var isFinished by mutableStateOf(false)

    var resultTitle: String = ""
    var resultBody: String = ""

    private var uploadJob: Job? = null

    init {
        loadPrompts()
    }

    private fun loadPrompts() {
        prompts = promptsRepository.getPrompts()
        // По умолчанию выбираем первый, если он есть
        if (prompts.isNotEmpty() && selectedPrompt == null) {
            selectedPrompt = prompts[0]
        }
    }

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

    fun selectPrompt(prompt: PromptItem) {
        selectedPrompt = prompt
    }

    // Логика добавления нового промпта прямо с этого экрана
    fun openAddDialog() {
        newPromptTitle = ""
        newPromptContent = ""
        showAddDialog = true
    }

    fun closeAddDialog() {
        showAddDialog = false
    }

    fun saveNewPrompt() {
        if (newPromptTitle.isBlank() || newPromptContent.isBlank()) return

        val newPrompt = PromptItem(
            id = UUID.randomUUID().toString(),
            title = newPromptTitle,
            content = newPromptContent,
            isDraft = false,
            lastModified = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
        )
        promptsRepository.addPrompt(newPrompt)
        loadPrompts() // Обновляем список
        selectedPrompt = newPrompt // Сразу выбираем созданный
        showAddDialog = false
    }

    fun startProcessing() {
        val item = recordingItem ?: return
        val currentPrompt = selectedPrompt

        if (isUploading || currentPrompt == null) return

        isUploading = true
        uploadProgress = 0.1f

        uploadJob = viewModelScope.launch {
            try {
                val savedKey = dataStore.openAiKey.first()

                if (savedKey.isNullOrBlank()) {
                    Toast.makeText(context, "Error: OpenAI API Key not set!", Toast.LENGTH_LONG).show()
                    isUploading = false
                    uploadProgress = 0f
                    return@launch
                }

                val file = File(item.filePath)

                val base64Audio = withContext(Dispatchers.IO) {
                    val bytes = file.readBytes()
                    Base64.encodeToString(bytes, Base64.NO_WRAP)
                }

                uploadProgress = 0.3f

                val request = ArticleRequest(
                    audioBase64 = base64Audio,
                    // Отправляем ТЕКСТ (Content) промпта, а не заголовок
                    prompt = currentPrompt.content,
                    openaiKey = savedKey
                )

                val response = RetrofitClient.api.uploadAudio(request)

                uploadProgress = 0.9f

                resultTitle = response.title
                resultBody = response.content

                // Сохраняем имя использованного промпта в карточку (для истории, если нужно)
                // (Это логика будет в HomeViewModel при сохранении результата)

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