package com.example.disiplinpro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.disiplinpro.data.preferences.ThemePreferences

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF7DAFCB),
    onPrimary = Color.White,
    surface = Color(0xFFF5F5F5),
    onSurface = Color.Black,
    background = LightBeige,
    onBackground = TextDark,
    secondary = SecondaryBlue,
    onSecondary = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryBlue,
    onPrimary = Color.Black,
    surface = DarkSurface,
    onSurface = DarkTextLight,
    background = DarkBackground,
    onBackground = DarkTextLight,
    secondary = DarkSecondaryBlue,
    onSecondary = Color.Black
)

@Composable
fun DisiplinproTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themePreferences = ThemePreferences(context)
    val isDarkModeFromPrefs by themePreferences.isDarkMode.collectAsState(initial = false)

    val isDarkMode = darkTheme ?: isDarkModeFromPrefs

    val colorScheme = if (isDarkMode) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}