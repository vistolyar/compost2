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
import com.example.compost2.data.PromptsRepository
import com.example.compost2.domain.AudioRecorder
import com.example.compost2.domain.PromptItem
import com.example.compost2.ui.components.ControlState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecorderViewModel(
    private val recorder: AudioRecorder,
    private val promptsRepository: PromptsRepository
) : ViewModel() {

    // Состояния записи
    var isRecording by mutableStateOf(false)
    var hasRecordingSession by mutableStateOf(false)
    var isDictaphoneMode by mutableStateOf(true)
    var currentAmplitude by mutableIntStateOf(0)
    var formattedTime by mutableStateOf("00:00.0")

    // Состояние интерфейса (Пятиугольник или Додекаэдр)
    var controlState by mutableStateOf(ControlState.RECORDING)

    // Список промптов для граней
    var prompts by mutableStateOf<List<PromptItem>>(emptyList())

    private var recordedFile: File? = null
    private var amplitudeJob: Job? = null
    private var timerJob: Job? = null

    private var sessionStartTimeMillis = 0L
    private var segmentStartTime = 0L
    private var accumulatedTime = 0L

    init {
        // Загружаем промпты из репозитория для отображения на фигуре
        loadPrompts()
    }

    private fun loadPrompts() {
        prompts = promptsRepository.getPrompts().take(12)
    }

    fun startCapture(context: Context, isDictaphone: Boolean) {
        if (isRecording) return

        isDictaphoneMode = isDictaphone
        vibrate(context)

        if (recordedFile == null) {
            recordedFile = File(context.cacheDir, "temp_recording.m4a")
            recorder.start(recordedFile!!)
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

    fun pauseCapture() {
        if (!isRecording) return

        recorder.pause()
        isRecording = false

        accumulatedTime += System.currentTimeMillis() - segmentStartTime

        stopAmplitudePolling()
        stopTimer()
    }

    fun finalizeRecording() {
        // Считаем финальное время перед остановкой
        if (isRecording) {
            accumulatedTime += System.currentTimeMillis() - segmentStartTime
        }

        recorder.stop()
        isRecording = false
        hasRecordingSession = false

        // ПЕРЕКЛЮЧАЕМ интерфейс на Додекаэдр
        controlState = ControlState.SELECTION

        stopAmplitudePolling()
        stopTimer()

        // Сохраняем файл
        val file = recordedFile
        if (file != null && file.exists()) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault())
            val datePart = dateFormat.format(Date(sessionStartTimeMillis))
            val newName = "${datePart}_final.m4a"
            val newFile = File(file.parent, newName)
            file.renameTo(newFile)
            recordedFile = newFile
        }
    }

    // Возврат из режима выбора к режиму записи (кнопка Back)
    fun resetToRecording() {
        controlState = ControlState.RECORDING
        // Оставляем сессию активной, чтобы можно было продолжить писать в тот же файл
        hasRecordingSession = true
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
                val current = if (isRecording) System.currentTimeMillis() - segmentStartTime else 0
                val total = accumulatedTime + current

                val minutes = (total / 1000) / 60
                val seconds = (total / 1000) % 60
                val tenths = (total / 100) % 10

                formattedTime = String.format(Locale.getDefault(), "%02d:%02d.%d", minutes, seconds, tenths)
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
                val repo = PromptsRepository(context)
                RecorderViewModel(recorder, repo)
            }
        }
    }
}