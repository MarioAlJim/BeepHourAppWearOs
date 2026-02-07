package com.wolf.beephour.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun BeepHourTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}

