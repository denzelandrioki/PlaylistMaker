package com.practicum.playlistmaker.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3772E7),
    onPrimary = Color.White,
    surface = Color.White,
    onSurface = Color(0xFF1A1B22),
    background = Color.White,
    onBackground = Color(0xFF1A1B22),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1A1B22),
    onPrimary = Color.White,
    surface = Color(0xFF1A1B22),
    onSurface = Color.White,
    background = Color(0xFF1A1B22),
    onBackground = Color.White,
)

/** Обёртка Material 3 для Compose: палитра согласована с темой приложения (светлая / тёмная). */
@Composable
fun PlaylistMakerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colors,
        typography = PlaylistTypography,
        content = content,
    )
}
