package com.example.compost2.data

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.example.compost2.domain.AudioRecorder
import java.io.File
import java.io.FileOutputStream

class AndroidAudioRecorder(private val context: Context): AudioRecorder {

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
        // На всякий случай освобождаем старый, если он был
        stop()

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder?.pause()
        }
    }

    override fun resume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder?.resume()
        }
    }

    override fun stop() {
        try {
            recorder?.stop()
        } catch (e: Exception) {
            // Иногда stop() падает, если запись была слишком короткой или не началась
            e.printStackTrace()
        } finally {
            // САМОЕ ВАЖНОЕ: Освобождаем микрофон
            recorder?.release()
            recorder = null
        }
    }

    override fun getAmplitude(): Int {
        return try {
            recorder?.maxAmplitude ?: 0
        } catch (e: Exception) {
            0
        }
    }
}