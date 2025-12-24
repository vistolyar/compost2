package com.example.compost2.ui.screens

import android.content.Context
import android.content.Intent
import android.os.*
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.compost2.data.AndroidAudioRecorder
import com.example.compost2.data.AudioRecordingService
import com.example.compost2.data.PromptsRepository
import com.example.compost2.domain.AudioRecorder
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RecorderViewModel(private val recorder: AudioRecorder) : ViewModel() {
    var isRecording by mutableStateOf(false)
    var hasRecordingSession by mutableStateOf(false)
    var isDictaphoneMode by mutableStateOf(false)
    var currentAmplitude by mutableIntStateOf(0)
    var formattedTime by mutableStateOf("00:00.0")

    private var recordedFile: File? = null
    private var timerJob: Job? = null
    private var amplitudeJob: Job? = null
    private var startTime = 0L
    private var sessionStartTimeMillis = 0L // Время начала самого первого сегмента
    private var accumulated = 0L

    fun startCapture(context: Context, dictaphone: Boolean) {
        if (isRecording) return
        isDictaphoneMode = dictaphone
        vibrate(context)
        val intent = Intent(context, AudioRecordingService::class.java)

        if (recordedFile == null) {
            recordedFile = File(context.cacheDir, "temp_recording.m4a")
            intent.action = "START"
            intent.putExtra("PATH", recordedFile?.absolutePath)
            context.startForegroundService(intent)

            sessionStartTimeMillis = System.currentTimeMillis() // Фиксируем время создания файла
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

    fun finalizeRecording(context: Context) {
        val finalDuration = if (isRecording) {
            accumulated + (System.currentTimeMillis() - startTime)
        } else {
            accumulated
        }

        val intent = Intent(context, AudioRecordingService::class.java).apply { action = "STOP" }
        context.startService(intent)

        recordedFile?.let { file ->
            if (file.exists()) {
                // Формируем дату начала записи
                val dateDf = SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault())
                val datePart = dateDf.format(Date(sessionStartTimeMillis))

                // Формируем длительность в формате HH-mm-ss, как ждет главный экран
                val durationPart = formatDurationForFileName(finalDuration)

                val newName = "${datePart}_${durationPart}.m4a"
                file.renameTo(File(file.parent, newName))
            }
        }

        recordedFile = null
        accumulated = 0
        isRecording = false
        hasRecordingSession = false
        formattedTime = "00:00.0"

        stopTimer()
        stopAmplitudePolling()
    }

    // Хелпер для форматирования длительности в имя файла (00-05-12)
    private fun formatDurationForFileName(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / 1000 / 60) % 60
        val hours = (millis / 1000 / 3600)
        return String.format(Locale.getDefault(), "%02d-%02d-%02d", hours, minutes, seconds)
    }

    fun enableDictaphoneMode() {
        isDictaphoneMode = true
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && isRecording) {
                val total = accumulated + (System.currentTimeMillis() - startTime)
                val minutes = (total / 1000) / 60
                val seconds = (total / 1000) % 60
                val tenths = (total / 100) % 10
                formattedTime = String.format(Locale.getDefault(), "%02d:%02d.%d", minutes, seconds, tenths)
                delay(100)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

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
        amplitudeJob = null
        currentAmplitude = 0
    }

    private fun vibrate(c: Context) {
        try {
            val v = if (Build.VERSION.SDK_INT >= 31) {
                (c.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                c.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer { RecorderViewModel(AndroidAudioRecorder(context)) }
        }
    }
}