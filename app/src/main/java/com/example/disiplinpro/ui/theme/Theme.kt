package com.example.disiplinpro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun DisiplinproTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF7DAFCB), // Warna tombol OK/Cancel dan elemen terpilih
            onPrimary = Color.White,
            surface = Color(0xFF121212), // Latar belakang dialog
            onSurface = Color.White // Teks di atas surface
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF7DAFCB), // Warna tombol OK/Cancel dan elemen terpilih
            onPrimary = Color.White,
            surface = Color(0xFFF5F5F5), // Latar belakang dialog
            onSurface = Color.Black // Teks di atas surface
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}