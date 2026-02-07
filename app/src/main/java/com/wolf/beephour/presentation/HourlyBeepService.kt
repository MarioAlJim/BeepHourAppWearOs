package com.wolf.beephour.presentation

import android.app.Notification
import android.app.Service
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.wolf.beephour.R

class HourlyBeepService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startForeground(
            NOTIFICATION_ID,
            createNotification()
        )

        playBeep()

        return START_NOT_STICKY
    }

    private fun playBeep() {
        val afd = resources.openRawResourceFd(R.raw.beepbeep)

        val mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(
                afd.fileDescriptor,
                afd.startOffset,
                afd.length
            )
            prepare()
            start()
        }
        afd.close()
        mediaPlayer.setOnCompletionListener {
            it.release()
            stopSelf()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
    }

    private fun createNotification(): Notification {
        val channelId = "hourly_beep_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Hourly Beep",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Hourly Beep")
            .setContentText("Sonando alarma horaria")
            .setSmallIcon(R.drawable.splash_icon)
            .setOngoing(true)
            .build()
    }

}

