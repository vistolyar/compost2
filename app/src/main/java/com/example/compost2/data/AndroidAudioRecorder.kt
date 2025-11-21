package com.example.compost2.data

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.example.compost2.domain.AudioRecorder
import java.io.File
import java.io.FileOutputStream

class AndroidAudioRecorder(
    private val context: Context
) : AudioRecorder {

    private var recorder: MediaRecorder? = null

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }

    override fun start(outputFile: File) {
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)

            prepare()
            start()

            recorder = this
        }
    }

    override fun pause() {
        // Метод pause доступен с API 24 (Android 7.0), у нас MinSdk 26, так что безопасно
        recorder?.pause()
    }

    override fun resume() {
        recorder?.resume()
    }

    override fun stop() {
        try {
            recorder?.stop()
        } catch (e: Exception) {
            // Иногда stop() может упасть, если запись была слишком короткой, игнорируем
            e.printStackTrace()
        }
        recorder?.reset()
        recorder = null
    }

    override fun getAmplitude(): Int {
        return try {
            recorder?.maxAmplitude ?: 0
        } catch (e: Exception) {
            0
        }
    }
}