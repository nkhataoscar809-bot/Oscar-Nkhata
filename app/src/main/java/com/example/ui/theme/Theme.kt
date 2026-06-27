package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = YovxPrimary,
    secondary = YovxSecondary,
    tertiary = YovxAccent,
    background = YovxObsidian,
    surface = YovxCharcoal,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = YovxTextPrimary,
    onSurface = YovxTextPrimary,
    outline = YovxGrey
)

private val LightColorScheme = lightColorScheme(
    primary = YovxPrimary,
    secondary = YovxSecondary,
    tertiary = YovxAccent,
    background = Color(0xFFFAFAFC),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1C24),
    onSurface = Color(0xFF1C1C24),
    outline = Color(0xFFE5E5EA)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors to keep Yovx branding consistent
    content: @Composable () -> Unit
) {
    // We enforce DarkColorScheme as primary, but fall back gracefully to Light if desired
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme // Enforce cinematic dark mode for social immersion!

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
