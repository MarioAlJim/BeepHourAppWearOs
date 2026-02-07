package com.wolf.beephour.presentation

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wolf.beephour.presentation.theme.BeepHourTheme
import androidx.compose.ui.tooling.preview.Preview


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val prefs = getSharedPreferences("prefs", MODE_PRIVATE)

            var darkThemeEnabled by remember {
                mutableStateOf(prefs.getBoolean("dark_theme_enabled", true))
            }

            BeepHourTheme(darkTheme = darkThemeEnabled) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WelcomeScreen(
                        darkThemeEnabled = darkThemeEnabled,
                        onDarkThemeChange = {
                            darkThemeEnabled = it
                            prefs.edit()
                                .putBoolean("dark_theme_enabled", it)
                                .apply()
                        }
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun WelcomeScreen(darkThemeEnabled: Boolean, onDarkThemeChange: (Boolean) -> Unit) {
    val context = LocalContext.current

    val prefs = remember {
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    }
    var alarmEnabled by remember {
        mutableStateOf(prefs.getBoolean("hourly_alarm_enabled", true))
    }
    WearContent(
        darkThemeEnabled = darkThemeEnabled,
        alarmEnabled = alarmEnabled,
        onAlarmChange = {
            alarmEnabled = it
            prefs.edit().putBoolean("hourly_alarm_enabled", it).apply()
            Log.e("test", ("alarm $alarmEnabled"))
            if (alarmEnabled) {
                HourlyBeepReceiver.scheduleNextHour(context)
            } else {
                cancelHourlyAlarm(context)
            }
        },
        onDarkThemeChange = {
            onDarkThemeChange(it)
        }
    )
}

@Composable
fun WearContent(
    darkThemeEnabled: Boolean,
    alarmEnabled: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    onAlarmChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Text(
            text = "Hourly Beep",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        SettingCard(
            title = "Alarma horaria",
            subtitle = "Sonar cada hora",
            checked = alarmEnabled,
            onCheckedChange = onAlarmChange
        )

        SettingCard(
            title = "Modo oscuro",
            subtitle = "Recomendado para el reloj",
            checked = darkThemeEnabled,
            onCheckedChange = onDarkThemeChange,
        )
    }
}


@Composable
fun SettingCard(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.error,
                        checkedTrackColor = MaterialTheme.colorScheme.errorContainer
                )
            )
        }
    }
}



@SuppressLint("ServiceCast")
fun cancelHourlyAlarm(context: Context) {
    val intent = Intent(context, HourlyBeepReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
}
