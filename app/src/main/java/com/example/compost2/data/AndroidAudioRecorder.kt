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

            // --- НАСТРОЙКИ ДЛЯ VERCEL (СЖАТИЕ) ---
            // 64 kbps (64000) - оптимально для голоса, размер файла маленький
            setAudioEncodingBitRate(64000)
            // 16 kHz (16000) - родная частота для Whisper, экономит место
            setAudioSamplingRate(16000)
            // -------------------------------------

            setOutputFile(FileOutputStream(outputFile).fd)

            prepare()
            start()

            recorder = this
        }
    }

    override fun stop() {
        try {
            recorder?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        recorder?.reset()
        recorder = null
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

    override fun getAmplitude(): Int {
        return recorder?.maxAmplitude ?: 0
    }
}