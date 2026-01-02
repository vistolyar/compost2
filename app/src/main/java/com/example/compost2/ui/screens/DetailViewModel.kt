package com.example.compost2.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.compost2.data.PromptsRepository
import com.example.compost2.data.SettingsDataStore
import com.example.compost2.data.auth.GoogleAuthClient
import com.example.compost2.data.auth.GoogleServicesHelper
import com.example.compost2.data.network.AiActionResponse
import com.example.compost2.data.network.ArticleRequest
import com.example.compost2.data.network.ChatRequest
import com.example.compost2.data.network.Message
import com.example.compost2.data.network.OpenAiClient
import com.example.compost2.data.network.RetrofitClient
import com.example.compost2.domain.IntegrationType
import com.example.compost2.domain.PromptItem
import com.example.compost2.domain.RecordingItem
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class DetailViewModel(private val context: Context) : ViewModel() {

    private val gson = Gson()
    private val promptsRepository = PromptsRepository(context)
    private val authClient = GoogleAuthClient(context)
    private val dataStore = SettingsDataStore(context)

    // --- ДАННЫЕ ЗАПИСИ ---
    var item by mutableStateOf<RecordingItem?>(null)
        private set
    var title by mutableStateOf("")
    var content by mutableStateOf("")

    // --- ПЛЕЕР ---
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    var isPlaying by mutableStateOf(false)
    var currentPosition by mutableLongStateOf(0L)
    var totalDuration by mutableLongStateOf(0L)
    var sliderPosition by mutableFloatStateOf(0f)

    // --- ЛИНЗА И ПРОМПТЫ ---
    var showLens by mutableStateOf(false)
    var prompts by mutableStateOf<List<PromptItem>>(emptyList())

    // Флаг загрузки (для AI операций)
    var isBusy by mutableStateOf(false)

    // --- ИНИЦИАЛИЗАЦИЯ ---
    fun loadItem(id: String) {
        prompts = promptsRepository.getPrompts()
        val file = File(context.cacheDir, id)
        if (file.exists()) {
            val metaFile = File(file.path + ".json")
            if (metaFile.exists()) {
                try {
                    val reader = FileReader(metaFile)
                    val loadedItem = gson.fromJson(reader, RecordingItem::class.java)
                    reader.close()

                    item = loadedItem.copy(filePath = file.absolutePath)
                    title = loadedItem.articleTitle ?: loadedItem.name
                    content = loadedItem.articleContent ?: ""

                    initPlayer(file)
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    // --- ЛОГИКА ПЛЕЕРА ---
    private fun initPlayer(file: File) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, Uri.fromFile(file))
            prepare()
            this@DetailViewModel.totalDuration = duration.toLong()
            setOnCompletionListener {
                this@DetailViewModel.isPlaying = false
                this@DetailViewModel.sliderPosition = 0f
            }
        }
    }

    fun togglePlay() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.pause()
                isPlaying = false
                progressJob?.cancel()
            } else {
                mp.start()
                isPlaying = true
                startProgressLoop()
            }
        }
    }

    fun seekTo(position: Float) {
        mediaPlayer?.let { mp ->
            val newPos = (totalDuration * position).toLong()
            mp.seekTo(newPos.toInt())
            sliderPosition = position
            currentPosition = newPos
        }
    }

    private fun startProgressLoop() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive && isPlaying) {
                mediaPlayer?.let { mp ->
                    currentPosition = mp.currentPosition.toLong()
                    if (totalDuration > 0) sliderPosition = currentPosition.toFloat() / totalDuration
                }
                delay(50)
            }
        }
    }

    // --- РЕДАКТИРОВАНИЕ ТЕКСТА ---
    fun updateContent(newTitle: String, newContent: String) {
        title = newTitle
        content = newContent
        // Автосохранение
        item?.let { current ->
            val updated = current.copy(articleTitle = newTitle, articleContent = newContent)
            item = updated
            saveMetadata(updated)
        }
    }

    // --- ЛИНЗА / AI ---
    fun onOpenLens() {
        if (content.isBlank()) {
            Toast.makeText(context, "No text to process. Transcribe first.", Toast.LENGTH_SHORT).show()
            return
        }
        showLens = true
    }

    fun onPromptSelected(prompt: PromptItem) {
        showLens = false
        processTextWithAi(prompt)
    }

    private fun processTextWithAi(prompt: PromptItem) {
        isBusy = true
        viewModelScope.launch {
            try {
                val apiKey = dataStore.openAiKey.first()
                if (apiKey.isNullOrBlank()) {
                    Toast.makeText(context, "No OpenAI Key in Settings!", Toast.LENGTH_SHORT).show()
                    isBusy = false
                    return@launch
                }

                // 1. Формируем СИСТЕМНЫЙ ПРОМПТ для JSON
                val systemInstruction = """
                    You are an Action Extractor API. 
                    Your goal is to analyze the user text and the user's specific request, then output a JSON object.
                    
                    OUTPUT FORMAT:
                    {
                      "action_type": "CALENDAR" or "GMAIL" or "NONE",
                      "title": "A short summary title",
                      "body": "The main content formatted for the destination",
                      "date": "YYYY-MM-DD HH:MM" (only if Calendar action, estimated from text)
                    }
                    
                    USER'S REQUEST: ${prompt.content}
                    
                    If the user request implies creating a meeting/event -> action_type = CALENDAR.
                    If the user request implies sending an email -> action_type = GMAIL.
                    Otherwise -> action_type = NONE.
                """.trimIndent()

                // 2. Отправляем запрос
                val request = ChatRequest(
                    messages = listOf(
                        Message("system", systemInstruction),
                        Message("user", content) // Отправляем текущий текст транскрипции
                    )
                )

                val response = OpenAiClient.api.generateResponse("Bearer $apiKey", request)
                val jsonContent = response.choices.firstOrNull()?.message?.content

                if (jsonContent != null) {
                    // 3. Парсим JSON
                    val actionData = gson.fromJson(jsonContent, AiActionResponse::class.java)

                    // 4. Применяем действие
                    handleAiAction(actionData)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "AI Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isBusy = false
            }
        }
    }

    private fun handleAiAction(data: AiActionResponse) {
        when (data.actionType) {
            "CALENDAR" -> {
                // Обновляем UI данными от AI (чтобы пользователь видел, что создается)
                title = data.title ?: title
                content = "${data.body}\n\n[Date: ${data.date}]"
                triggerIntegration(IntegrationType.CALENDAR)
            }
            "GMAIL" -> {
                title = data.title ?: title
                content = data.body ?: content
                triggerIntegration(IntegrationType.GMAIL)
            }
            else -> {
                // Если просто текст - обновляем поля
                title = data.title ?: title
                content = data.body ?: content
                Toast.makeText(context, "Text processed", Toast.LENGTH_SHORT).show()
            }
        }
        // Сохраняем изменения
        updateContent(title, content)
    }

    // --- ПОВТОРНАЯ ТРАНСКРИБАЦИЯ (RAW) ---
    fun reTranscribe() {
        if (isBusy || item == null) return
        isBusy = true

        viewModelScope.launch {
            try {
                val apiKey = dataStore.openAiKey.first()
                if (apiKey.isNullOrBlank()) return@launch

                val file = File(item!!.filePath)
                val audioBytes = withContext(Dispatchers.IO) { file.readBytes() }
                val base64 = Base64.encodeToString(audioBytes, Base64.NO_WRAP)

                val request = ArticleRequest(
                    audioBase64 = base64,
                    prompt = "Transcribe exactly what is said. No formatting.",
                    openaiKey = apiKey
                )

                val response = RetrofitClient.api.uploadAudio(request)

                title = response.title
                content = response.content
                updateContent(title, content)

                Toast.makeText(context, "Transcribed", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isBusy = false
            }
        }
    }

    fun copyToClipboard() {
        if (title.isBlank() && content.isBlank()) return
        val fullText = "$title\n\n$content"
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("ComPost", fullText)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
    }

    fun triggerIntegration(type: IntegrationType) {
        val googleAccount = authClient.getSignedInAccount()
        if (googleAccount == null && (type == IntegrationType.CALENDAR || type == IntegrationType.GMAIL)) {
            Toast.makeText(context, "Sign in to Google first", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            isBusy = true
            val helper = GoogleServicesHelper(context, googleAccount!!)

            val success = when (type) {
                IntegrationType.CALENDAR -> {
                    // В будущем сюда будем передавать дату из AI
                    helper.createCalendarEvent(title, content) != null
                }
                IntegrationType.GMAIL -> {
                    helper.createDraft(title, content) != null
                }
                else -> false
            }

            isBusy = false
            if (success) {
                Toast.makeText(context, "Sent to ${type.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveMetadata(item: RecordingItem) {
        try {
            val metaFile = File(item.filePath + ".json")
            FileWriter(metaFile).use { gson.toJson(item, it) }
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun onCleared() {
        mediaPlayer?.release()
        super.onCleared()
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer { DetailViewModel(context) }
        }
    }
}