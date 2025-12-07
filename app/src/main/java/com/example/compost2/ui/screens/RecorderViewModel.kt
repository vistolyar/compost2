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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecorderViewModel(
    private val recorder: AudioRecorder
) : ViewModel() {

    var isRecording by mutableStateOf(false)
        private set

    var hasRecordingSession by mutableStateOf(false)
        private set

    var isDictaphoneMode by mutableStateOf(true)
        private set

    var currentAmplitude by mutableIntStateOf(0)
        private set

    var formattedTime by mutableStateOf("00:00.0")
        private set

    var recordedFile: File? = null
        private set

    private var amplitudeJob: Job? = null
    private var timerJob: Job? = null

    private var sessionStartTimeMillis = 0L
    private var segmentStartTime = 0L
    private var accumulatedTime = 0L

    fun startCapture(context: Context, isDictaphone: Boolean) {
        if (isRecording) return

        isDictaphoneMode = isDictaphone
        vibrate(context)

        var file = recordedFile
        if (file == null) {
            file = File(context.cacheDir, "temp_recording.m4a")
            recordedFile = file
            recorder.start(file)
            hasRecordingSession = true
            sessionStartTimeMillis = System.currentTimeMillis()
        } else {
            recorder.resume()
        }

        isRecording = true
        segmentStartTime = System.currentTimeMillis()

        startAmplitudePolling()
        startTimer()
    }

    fun enableDictaphoneMode() {
        isDictaphoneMode = true
    }

    fun pauseCapture() {
        if (!isRecording) return

        recorder.pause()
        isRecording = false

        accumulatedTime += System.currentTimeMillis() - segmentStartTime

        stopAmplitudePolling()
        stopTimer()
    }

    fun finalizeRecording() {
        // 1. Сначала считаем точное время! До того, как остановим запись.
        val endTime = System.currentTimeMillis()
        val currentSegmentDuration = if (isRecording) (endTime - segmentStartTime) else 0L
        val totalDuration = accumulatedTime + currentSegmentDuration

        // 2. Теперь останавливаем процессы
        stopAmplitudePolling()
        stopTimer()
        recorder.stop()
        isRecording = false

        // 3. Переименовываем файл
        val file = recordedFile
        if (file != null && file.exists()) {

            // Формируем дату начала
            val dateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault())
            val datePart = dateFormat.format(Date(sessionStartTimeMillis))

            // Формируем длительность (теперь она не 0!)
            val durationPart = formatDurationForFileName(totalDuration)

            val newName = "${datePart}_${durationPart}.m4a"
            val newFile = File(file.parent, newName)

            file.renameTo(newFile)
            recordedFile = newFile
        }
    }

    // Формат ЧЧ-ММ-СС для имени файла
    private fun formatDurationForFileName(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / 1000 / 60) % 60
        val hours = (millis / 1000 / 3600)
        return String.format(Locale.getDefault(), "%02d-%02d-%02d", hours, minutes, seconds)
    }

    private fun formatTime(millis: Long): String {
        val tenths = (millis / 100) % 10
        val seconds = (millis / 1000) % 60
        val minutes = (millis / 1000) / 60
        return String.format(Locale.getDefault(), "%02d:%02d.%d", minutes, seconds, tenths)
    }

    private fun startAmplitudePolling() {
        amplitudeJob?.cancel()
        amplitudeJob = viewModelScope.launch {
            while (isActive) {
                currentAmplitude = try { recorder.getAmplitude() } catch (e: Exception) { 0 }
                delay(50)
            }
        }
    }

    private fun stopAmplitudePolling() {
        amplitudeJob?.cancel()
        currentAmplitude = 0
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                val currentSegmentDuration = if (isRecording) System.currentTimeMillis() - segmentStartTime else 0
                val totalDurationMillis = accumulatedTime + currentSegmentDuration
                formattedTime = formatTime(totalDurationMillis)
                delay(50)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    private fun vibrate(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(50)
        }
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