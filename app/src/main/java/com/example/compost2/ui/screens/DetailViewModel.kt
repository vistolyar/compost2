package com.example.compost2.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
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
import com.example.compost2.data.network.ChatRequest
import com.example.compost2.data.network.Message
import com.example.compost2.data.network.OpenAiClient
import com.example.compost2.data.network.ProcessTextRequest // НОВЫЙ ИМПОРТ
import com.example.compost2.data.network.RetrofitClient
import com.example.compost2.data.network.TranscribeRequest // НОВЫЙ ИМПОРТ
import com.example.compost2.domain.IntegrationType
import com.example.compost2.domain.PromptItem
import com.example.compost2.domain.RecordingItem
import com.example.compost2.domain.RecordingStatus
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Locale

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
    var rawText by mutableStateOf("")
    var hasRawText by mutableStateOf(false)

    var recordingDateTimestamp by mutableLongStateOf(0L)

    // --- СОСТОЯНИЕ UI ---
    var showLens by mutableStateOf(false)
    var showRawTextModal by mutableStateOf(false)
    var isBusy by mutableStateOf(false)

    // --- ПЛЕЕР ---
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    var isPlaying by mutableStateOf(false)
    var currentPosition by mutableLongStateOf(0L)
    var totalDuration by mutableLongStateOf(0L)
    var sliderPosition by mutableFloatStateOf(0f)

    var prompts by mutableStateOf<List<PromptItem>>(emptyList())

    fun loadItem(id: String) {
        prompts = promptsRepository.getPrompts()
        val file = File(context.cacheDir, id)

        recordingDateTimestamp = parseTimestampFromFilename(id)

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
                    rawText = loadedItem.rawTranscription ?: ""
                    hasRawText = rawText.isNotBlank()

                    initPlayer(file)
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    private fun parseTimestampFromFilename(fileName: String): Long {
        return try {
            val datePart = fileName.split("_")[0]
            val format = SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault())
            format.parse(datePart)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    fun onStatusAction() {
        val currentStatus = item?.status ?: return
        when (currentStatus) {
            RecordingStatus.TRANSCRIBED, RecordingStatus.PUBLISHED, RecordingStatus.READY -> {
                showRawTextModal = true
            }
            RecordingStatus.SAVED, RecordingStatus.PROCESSING -> {
                reTranscribe()
            }
        }
    }

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

    fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    // --- AI / LENS ---
    fun onOpenLens() {
        if (content.isBlank()) {
            Toast.makeText(context, "No text to process. Transcribe first.", Toast.LENGTH_SHORT).show()
            return
        }
        showLens = true
    }

    fun onPromptSelected(prompt: PromptItem) {
        showLens = false
        // Если у нас уже есть rawText, используем его как источник правды
        // Если нет (старая запись), используем content
        val sourceText = if (hasRawText) rawText else content

        // Тут мы могли бы использовать новый эндпоинт processText,
        // но пока оставим старую логику прямых запросов к OpenAI для "линзы" внутри DetailScreen,
        // или переведем на processText (лучше перевести).

        // ДАВАЙТЕ ПЕРЕВЕДЕМ НА НОВЫЙ API, так как это дешевле и быстрее
        processTextWithBackend(sourceText, prompt)
    }

    private fun processTextWithBackend(text: String, prompt: PromptItem) {
        isBusy = true
        viewModelScope.launch {
            try {
                val apiKey = dataStore.openAiKey.first()
                if (apiKey.isNullOrBlank()) {
                    Toast.makeText(context, "No OpenAI Key!", Toast.LENGTH_SHORT).show()
                    isBusy = false
                    return@launch
                }

                val request = ProcessTextRequest(
                    rawText = text,
                    prompt = prompt.content, // Здесь текст промпта
                    openAiKey = apiKey
                )

                val response = RetrofitClient.api.processText(request)

                // Обновляем UI
                title = response.title
                content = response.content
                updateContent(title, content)

                Toast.makeText(context, "Processed!", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isBusy = false
            }
        }
    }

    // --- RE-TRANSCRIBE (NEW API 2.0) ---
    fun reTranscribe() {
        if (isBusy || item == null) return
        isBusy = true

        viewModelScope.launch {
            try {
                val apiKey = dataStore.openAiKey.first()
                if (apiKey.isNullOrBlank()) { isBusy = false; return@launch }

                val file = File(item!!.filePath)
                if (!file.exists()) return@launch

                // 1. UPLOAD
                val uploadInfo = RetrofitClient.api.getUploadUrl()
                val requestBody = RequestBody.create("audio/mp4".toMediaTypeOrNull(), file)
                val uploadResponse = RetrofitClient.api.uploadFileToS3(uploadInfo.uploadUrl, requestBody)

                if (!uploadResponse.isSuccessful) throw Exception("S3 Upload Error")

                // 2. TRANSCRIBE
                val transcribeRequest = TranscribeRequest(
                    fileKey = uploadInfo.fileKey,
                    openAiKey = apiKey
                )
                val transcribeResponse = RetrofitClient.api.transcribe(transcribeRequest)
                val newRawText = transcribeResponse.rawText

                // 3. PROCESS (Default Prompt)
                val processRequest = ProcessTextRequest(
                    rawText = newRawText,
                    prompt = "Transcribe exactly what is said. No formatting.",
                    openAiKey = apiKey
                )
                val processResponse = RetrofitClient.api.processText(processRequest)

                title = processResponse.title
                content = processResponse.content
                rawText = newRawText

                item?.let { current ->
                    val updated = current.copy(
                        status = RecordingStatus.TRANSCRIBED,
                        articleTitle = title,
                        articleContent = content,
                        rawTranscription = rawText
                    )
                    item = updated
                    saveMetadata(updated)
                    hasRawText = true
                }

                Toast.makeText(context, "Transcribed", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isBusy = false
            }
        }
    }

    fun updateContent(newTitle: String, newContent: String) {
        title = newTitle
        content = newContent
        item?.let { current ->
            val updated = current.copy(articleTitle = newTitle, articleContent = newContent)
            item = updated
            saveMetadata(updated)
        }
    }

    fun restoreRawText() {
        content = rawText
        updateContent(title, content)
        Toast.makeText(context, "Restored", Toast.LENGTH_SHORT).show()
    }

    fun copyToClipboard() {
        val fullText = "$title\n\n$content"
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("ComPost", fullText)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
    }

    fun triggerIntegration(type: IntegrationType) {
        // ... (старая логика для Google Calendar/Gmail)
        // Она может остаться как есть или тоже быть перенесена на бэкенд в будущем
        val googleAccount = authClient.getSignedInAccount()
        if (googleAccount == null && (type == IntegrationType.CALENDAR || type == IntegrationType.GMAIL || type == IntegrationType.TASKS)) {
            Toast.makeText(context, "Sign in to Google first", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            isBusy = true
            val helper = GoogleServicesHelper(context, googleAccount!!)
            val success = when (type) {
                IntegrationType.CALENDAR -> helper.createCalendarEvent(title, content) != null
                IntegrationType.GMAIL -> helper.createDraft(title, content) != null
                IntegrationType.TASKS -> helper.createTask(title, content) != null
                else -> false
            }
            isBusy = false
            if (success) Toast.makeText(context, "Sent to ${type.name}", Toast.LENGTH_SHORT).show()
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