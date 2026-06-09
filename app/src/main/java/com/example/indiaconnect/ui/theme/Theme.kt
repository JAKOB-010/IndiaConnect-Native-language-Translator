package com.example.indiaconnect.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MintGreen,
    secondary = CardSurface,
    tertiary = MintGreen,
    background = DarkBackground,
    surface = CardSurface,
    onPrimary = Color.Black,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

private val LightColorScheme = DarkColorScheme // Application is primarily dark as per requirements

@Composable
fun IndiaConnectTheme(
    darkTheme: Boolean = true, // Force dark theme as per requirements
    dynamicColor: Boolean = false, // Disable dynamic color to strictly follow the palette
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
