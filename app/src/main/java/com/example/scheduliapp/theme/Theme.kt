package com.example.scheduliapp.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = IndigoAccent,
    secondary = TealAccent,
    tertiary = RoseAccent,
    background = DarkBackground,
    surface = SurfaceCard,
    surfaceVariant = SurfaceContainer,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor
)

@Composable
fun ScheduliAppTheme(
    darkTheme: Boolean = true, // Force dark theme for premium dark developer aesthetics
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
