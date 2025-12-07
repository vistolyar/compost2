package com.example.compost2.ui.screens

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

class PlayerViewModel(private val context: Context) : ViewModel() {

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    // Переменные состояния UI
    var isPlaying by mutableStateOf(false)
    var totalDurationMillis by mutableLongStateOf(0L)
    var currentPositionMillis by mutableLongStateOf(0L)
    var sliderPosition by mutableFloatStateOf(0f)
    var currentSpeed by mutableFloatStateOf(1f)
    var fileName by mutableStateOf("")

    fun loadFile(name: String) {
        fileName = name
        val file = File(context.cacheDir, name)

        if (!file.exists()) return

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, Uri.fromFile(file))
            prepare()

            // ИСПРАВЛЕНИЕ: Явно указываем this@PlayerViewModel, чтобы не путать с duration плеера
            this@PlayerViewModel.totalDurationMillis = duration.toLong()

            setOnCompletionListener {
                // ИСПРАВЛЕНИЕ: Явно указываем this@PlayerViewModel, чтобы не путать с isPlaying плеера
                this@PlayerViewModel.isPlaying = false
                this@PlayerViewModel.currentPositionMillis = 0
                this@PlayerViewModel.sliderPosition = 0f
            }
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                // ИСПРАВЛЕНИЕ: Конфликт имен устранен
                this@PlayerViewModel.isPlaying = false
                stopProgressLoop()
            } else {
                player.start()
                // ИСПРАВЛЕНИЕ: Конфликт имен устранен
                this@PlayerViewModel.isPlaying = true
                startProgressLoop()
                applySpeed(currentSpeed)
            }
        }
    }

    fun seekTo(position: Float) {
        mediaPlayer?.let { player ->
            val newMillis = (totalDurationMillis * position).toInt()
            player.seekTo(newMillis)
            currentPositionMillis = newMillis.toLong()
            sliderPosition = position
        }
    }

    fun changeSpeed() {
        val newSpeed = when (currentSpeed) {
            1f -> 1.5f
            1.5f -> 2f
            else -> 1f
        }
        currentSpeed = newSpeed
        applySpeed(newSpeed)
    }

    private fun applySpeed(speed: Float) {
        mediaPlayer?.let { player ->
            try {
                val params = player.playbackParams
                params.speed = speed
                player.playbackParams = params
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startProgressLoop() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive && isPlaying) {
                mediaPlayer?.let { player ->
                    currentPositionMillis = player.currentPosition.toLong()
                    if (totalDurationMillis > 0) {
                        sliderPosition = currentPositionMillis.toFloat() / totalDurationMillis
                    }
                }
                delay(50)
            }
        }
    }

    private fun stopProgressLoop() {
        progressJob?.cancel()
    }

    fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PlayerViewModel(context)
            }
        }
    }
}