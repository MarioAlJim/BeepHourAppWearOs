package com.wolf.beephour.presentation

import android.app.Service
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.wolf.beephour.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar


class HourlyBeepService : Service() {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        startHourlyCheck()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun startForegroundService() {
        val channelId = "hourly_beep_channel"
        val channel = NotificationChannel(
            channelId,
            "Hourly Beep Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Servicio activo")
            .setContentText("Sonará cada vez que cambie la hora")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
    }

    private fun startHourlyCheck() {
        scope.launch {
            while (isActive) {
                val calendar = Calendar.getInstance()
                val minute = calendar.get(Calendar.MINUTE)
                val second = calendar.get(Calendar.SECOND)

                if (minute == 40 && second == 0) {
                    playBeep()
                }
                delay(1000L) // revisa cada segundo
            }
        }
    }

    private fun playBeep() {
        // Reproduce el sonido
        val mediaPlayer = MediaPlayer.create(this, R.raw.beepbeep)
        mediaPlayer.setOnCompletionListener { it.release() }
        mediaPlayer.start()

        // Activa la vibración
        val vibrator = getSystemService(Vibrator::class.java)
        if (vibrator != null) {
            val effect = VibrationEffect.createOneShot(
                500,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
            vibrator.vibrate(effect)
        }
    }

}
