package com.wolf.beephour.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val alarmEnabled = prefs.getBoolean("hourly_alarm_enabled", false)
            if (alarmEnabled) {
                HourlyBeepReceiver.scheduleNextHour(context)
            }
        }
    }
}


