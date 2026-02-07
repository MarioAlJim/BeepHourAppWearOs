package com.wolf.beephour.presentation

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.wolf.beephour.R
import java.util.Calendar

class HourlyBeepReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("hourly_alarm_enabled", true)

        Log.e("test", "check enabled")
        if (!enabled) return   // No hacer nada

        // Reproducir sonido
        val afd = context.resources.openRawResourceFd(R.raw.beepbeep)
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
        }

        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(
            VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
        )

        // Programar siguiente alarma
        scheduleNextHour(context)
    }

    companion object {
        @SuppressLint("ScheduleExactAlarm")
        fun scheduleNextHour(context: Context) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.HOUR_OF_DAY, 1)
            calendar.set(Calendar.MINUTE, 1)
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
}
