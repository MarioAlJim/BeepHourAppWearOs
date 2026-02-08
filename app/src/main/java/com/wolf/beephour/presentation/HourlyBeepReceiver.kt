package com.wolf.beephour.presentation

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import com.wolf.beephour.R
import java.util.Calendar

class HourlyBeepReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("hourly_alarm_enabled", true)) return

        playHourlyFeedback(context)
        scheduleNextHour(context)
    }

    companion object {
        @SuppressLint("ScheduleExactAlarm")
        fun scheduleNextHour(context: Context) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.HOUR_OF_DAY, 1)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val alarmIntent = Intent(context, HourlyBeepReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun playHourlyFeedback(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        val ringerMode = audioManager.ringerMode
        val isSilent = ringerMode == AudioManager.RINGER_MODE_SILENT
        val isVibrate = ringerMode == AudioManager.RINGER_MODE_VIBRATE
        val isDndEnabled = notificationManager.currentInterruptionFilter !=
                NotificationManager.INTERRUPTION_FILTER_ALL
        if (isSilent || isDndEnabled) {
            Log.e("test", "1")
            return
        } else if (isVibrate) {
            Log.e("test", "2")
            vibrate(context)
        } else {
            Log.e("test", "3")
            playBeep(context)
            vibrate(context)
        }
    }

    private fun playBeep(context: Context) {
        val afd = context.resources.openRawResourceFd(R.raw.beepbeep)
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(attributes)
            setDataSource(
                afd.fileDescriptor,
                afd.startOffset,
                afd.length
            )
            prepare()
            start()
        }
        mediaPlayer.setOnCompletionListener {
            it.release()
            afd.close()
        }
    }

    private fun vibrate(context: Context) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator
        vibrator.vibrate(
            VibrationEffect.createOneShot(
                300, VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    }
}
