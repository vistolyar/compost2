package com.example.compost2.data

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.compost2.ui.MainActivity
import java.io.File

class AudioRecordingService : Service() {
    private val CHANNEL_ID = "recording_channel"

    companion object {
        // Это позволит ViewModel брать амплитуду прямо из работающего сервиса
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
                startForeground(1, createNotification("Запись идет..."))
            }
            "PAUSE" -> {
                activeRecorder?.pause()
                updateNotification("На паузе")
            }
            "RESUME" -> {
                activeRecorder?.resume()
                updateNotification("Запись идет...")
            }
            "STOP" -> {
                activeRecorder?.stop()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ComPost")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setContentIntent(pi)
            .build()
    }

    private fun updateNotification(t: String) = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(1, createNotification(t))

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(CHANNEL_ID, "Record", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(chan)
        }
    }

    override fun onDestroy() {
        activeRecorder = null
        super.onDestroy()
    }

    override fun onBind(i: Intent?): IBinder? = null
}