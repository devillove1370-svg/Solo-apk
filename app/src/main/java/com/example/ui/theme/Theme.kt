package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val SoloLevelingColorScheme = darkColorScheme(
    primary = SoloNeonBlue,
    secondary = SoloMonarchPurple,
    tertiary = SoloWarningRed,
    background = SoloSlateBase,
    surface = SoloOnyxCard,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Force Solo Leveling Custom Dark Color Scheme for maximum RPG immersion
    val colorScheme = SoloLevelingColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

