package com.example.compost2.ui.screens

import android.content.Context
import android.content.Intent
import android.os.*
import androidx.compose.runtime.*
// ВАЖНЫЕ ИМПОРТЫ ДЛЯ РАБОТЫ ДЕЛЕГАТОВ (by mutableStateOf)
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.compost2.data.AndroidAudioRecorder
import com.example.compost2.data.AudioRecordingService
import com.example.compost2.data.PromptsRepository
import com.example.compost2.domain.AudioRecorder
import com.example.compost2.domain.PromptItem
import com.example.compost2.domain.RecordingItem
import com.example.compost2.domain.RecordingStatus
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

// Вынесли Enum наружу, чтобы он был доступен везде без ошибок
enum class ActionState { IDLE, RECORDING, SELECTION }

class RecorderViewModel(
    private val recorder: AudioRecorder,
    private val promptsRepository: PromptsRepository
) : ViewModel() {

    var controlState by mutableStateOf(ActionState.IDLE)
    var isRecording by mutableStateOf(false)
    var hasRecordingSession by mutableStateOf(false)
    var isDictaphoneMode by mutableStateOf(false)

    var currentAmplitude by mutableIntStateOf(0)
    var formattedTime by mutableStateOf("00:00.0")
    var prompts by mutableStateOf<List<PromptItem>>(emptyList())

    var isReadyForSTT by mutableStateOf(false)

    private var recordedFile: File? = null
    private var timerJob: Job? = null
    private var amplitudeJob: Job? = null
    private var startTime = 0L
    private var sessionStartTimeMillis = 0L
    private var accumulated = 0L

    init {
        prompts = promptsRepository.getPrompts()
    }

    fun startCapture(context: Context, dictaphone: Boolean) {
        if (isRecording) return
        isDictaphoneMode = dictaphone
        controlState = ActionState.RECORDING
        vibrate(context)

        val intent = Intent(context, AudioRecordingService::class.java)
        if (recordedFile == null) {
            recordedFile = File(context.cacheDir, "temp_recording.m4a")
            intent.action = "START"
            intent.putExtra("PATH", recordedFile?.absolutePath)
            context.startForegroundService(intent)
            sessionStartTimeMillis = System.currentTimeMillis()
            hasRecordingSession = true
        } else {
            intent.action = "RESUME"
            context.startService(intent)
        }

        isRecording = true
        startTime = System.currentTimeMillis()
        startTimer()
        startAmplitudePolling()
    }

    fun pauseCapture(context: Context) {
        if (!isRecording) return
        val intent = Intent(context, AudioRecordingService::class.java).apply { action = "PAUSE" }
        context.startService(intent)
        accumulated += System.currentTimeMillis() - startTime
        isRecording = false
        stopAmplitudePolling()
    }

    fun stopForSelection(context: Context) {
        if (isRecording) pauseCapture(context)
        controlState = ActionState.SELECTION
    }

    fun onPromptSelected(context: Context, prompt: PromptItem) {
        val finalDuration = accumulated
        val intent = Intent(context, AudioRecordingService::class.java).apply { action = "STOP" }
        context.startService(intent)

        recordedFile?.let { file ->
            if (file.exists()) {
                val dateDf = SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault())
                val h = finalDuration / 3600000
                val m = (finalDuration / 60000) % 60
                val s = (finalDuration / 1000) % 60
                val durationPart = String.format(Locale.getDefault(), "%02d-%02d-%02d", h, m, s)

                val newName = "${dateDf.format(Date(sessionStartTimeMillis))}_${durationPart}.m4a"
                val finalFile = File(file.parent, newName)
                file.renameTo(finalFile)

                // Сохраняем метаданные со статусом PROCESSING (Sent)
                val item = RecordingItem(
                    id = finalFile.name,
                    name = newName,
                    status = RecordingStatus.PROCESSING,
                    filePath = finalFile.absolutePath,
                    promptName = prompt.title
                )
                saveMetadata(item)
            }
        }
        isReadyForSTT = true
    }

    private fun saveMetadata(item: RecordingItem) {
        try {
            val metaFile = File(item.filePath + ".json")
            FileWriter(metaFile).use { Gson().toJson(item, it) }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && isRecording) {
                val total = accumulated + (System.currentTimeMillis() - startTime)
                val min = (total / 1000) / 60
                val sec = (total / 1000) % 60
                val ms = (total / 100) % 10
                formattedTime = String.format(Locale.getDefault(), "%02d:%02d.%d", min, sec, ms)
                delay(100)
            }
        }
    }

    private fun stopTimer() { timerJob?.cancel() }

    private fun startAmplitudePolling() {
        amplitudeJob?.cancel()
        amplitudeJob = viewModelScope.launch {
            while (isActive && isRecording) {
                currentAmplitude = AudioRecordingService.activeRecorder?.getAmplitude() ?: 0
                delay(50)
            }
        }
    }

    private fun stopAmplitudePolling() {
        amplitudeJob?.cancel()
        currentAmplitude = 0
    }

    private fun vibrate(c: Context) {
        val v = if (Build.VERSION.SDK_INT >= 31) (c.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        else c.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun resetToRecording() {
        controlState = ActionState.RECORDING
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RecorderViewModel(AndroidAudioRecorder(context), PromptsRepository(context))
            }
        }
    }
}