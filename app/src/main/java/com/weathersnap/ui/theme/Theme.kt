package com.weathersnap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark color scheme matching the reference screenshots
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB5CC6A),          // olive/yellow-green accent
    onPrimary = Color(0xFF1A1A1A),
    primaryContainer = Color(0xFF2C2C2C),
    onPrimaryContainer = Color(0xFFE0E0E0),
    secondary = Color(0xFF888888),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFF1A1A1A),        // very dark background
    onBackground = Color(0xFFEEEEEE),
    surface = Color(0xFF242424),           // slightly lighter card surface
    onSurface = Color(0xFFEEEEEE),
    surfaceVariant = Color(0xFF2E2E2E),
    onSurfaceVariant = Color(0xFFAAAAAA),
    outline = Color(0xFF444444),
    error = Color(0xFFCF6679)
)

@Composable
fun WeatherSnapTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
