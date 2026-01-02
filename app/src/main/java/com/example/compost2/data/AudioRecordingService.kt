package com.example.compost2.data

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.compost2.ui.MainActivity
import java.io.File

class AudioRecordingService : Service() {
    private val CHANNEL_ID = "recording_channel"

    companion object {
        var activeRecorder: AndroidAudioRecorder? = null
    }

    override fun onCreate() {
        super.onCreate()
        activeRecorder = AndroidAudioRecorder(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> {
                val path = intent.getStringExtra("PATH") ?: return START_NOT_STICKY
                activeRecorder?.start(File(path))
                startForeground(1, createNotification("Recording..."))
            }
            "PAUSE" -> {
                activeRecorder?.pause()
                updateNotification("Paused")
            }
            "RESUME" -> {
                activeRecorder?.resume()
                updateNotification("Recording...")
            }
            "STOP" -> {
                forceStopRecording()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    // Этот метод вызывается, когда пользователь смахивает приложение из "Недавних"
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        forceStopRecording()
        stopSelf()
    }

    // Гарантированная очистка при уничтожении сервиса
    override fun onDestroy() {
        forceStopRecording()
        activeRecorder = null
        super.onDestroy()
    }

    private fun forceStopRecording() {
        try {
            activeRecorder?.stop() // Внутри AndroidAudioRecorder это вызовет release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun createNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ComPost")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true) // Уведомление нельзя смахнуть пока идет запись
            .setContentIntent(pi)
            .build()
    }

    private fun updateNotification(t: String) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, createNotification(t))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(CHANNEL_ID, "Voice Recorder", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
        }
    }

    override fun onBind(i: Intent?): IBinder? = null
}