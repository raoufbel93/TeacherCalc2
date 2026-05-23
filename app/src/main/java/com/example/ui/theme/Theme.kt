package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color(0xFF121212),
    secondary = SuccessGreen,
    onSecondary = Color.White,
    tertiary = WarningOrange,
    onTertiary = Color.Black,
    background = DarkBg,
    onBackground = Color.White,
    surface = TabBg,
    onSurface = Color.White,
    surfaceVariant = SoftCard,
    onSurfaceVariant = Color.White,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
