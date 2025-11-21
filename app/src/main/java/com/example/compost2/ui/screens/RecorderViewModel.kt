package com.example.compost2.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.compost2.data.AndroidAudioRecorder
import com.example.compost2.domain.AudioRecorder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

class RecorderViewModel(
    private val recorder: AudioRecorder
) : ViewModel() {

    var isRecording by mutableStateOf(false)
        private set

    var hasRecordingSession by mutableStateOf(false)
        private set

    var currentAmplitude by mutableIntStateOf(0)
        private set

    // --- НОВОЕ: Строка времени для отображения на экране ---
    var formattedTime by mutableStateOf("00:00")
        private set

    var recordedFile: File? = null
        private set

    private var amplitudeJob: Job? = null
    private var timerJob: Job? = null

    // Переменные для подсчета времени
    private var startTime = 0L
    private var accumulatedTime = 0L // Накопленное время до паузы

    fun startCapture(context: Context) {
        if (isRecording) return

        vibrate(context)

        var file = recordedFile
        if (file == null) {
            file = File(context.cacheDir, "voice_draft.mp4")
            recordedFile = file
            recorder.start(file)
            hasRecordingSession = true
        } else {
            recorder.resume()
        }

        isRecording = true

        // Запоминаем, во сколько начали текущий кусок записи
        startTime = System.currentTimeMillis()

        startAmplitudePolling()
        startTimer()
    }

    fun pauseCapture() {
        if (!isRecording) return

        recorder.pause()
        isRecording = false

        // Добавляем текущий кусок к общему времени
        accumulatedTime += System.currentTimeMillis() - startTime

        stopAmplitudePolling()
        stopTimer()
    }

    fun finalizeRecording() {
        stopAmplitudePolling()
        stopTimer()
        recorder.stop()
        isRecording = false
        // Сбрасываем таймер при завершении (опционально, зависит от логики перехода дальше)
        // accumulatedTime = 0L
        // formattedTime = "00:00"
    }

    private fun vibrate(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }
        }
    }

    private fun startAmplitudePolling() {
        amplitudeJob?.cancel()
        amplitudeJob = viewModelScope.launch {
            while (isActive) {
                currentAmplitude = recorder.getAmplitude()
                delay(100)
            }
        }
    }

    private fun stopAmplitudePolling() {
        amplitudeJob?.cancel()
        currentAmplitude = 0
    }

    // --- НОВОЕ: Логика таймера ---
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                // Текущая длительность = (что было раньше) + (сколько прошло с нажатия кнопки сейчас)
                val totalDurationMillis = accumulatedTime + (System.currentTimeMillis() - startTime)
                formattedTime = formatTime(totalDurationMillis)
                delay(100) // Обновляем каждые 0.1 сек
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / 1000) / 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val recorder = AndroidAudioRecorder(context)
                RecorderViewModel(recorder)
            }
        }
    }
}