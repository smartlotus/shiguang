package com.shiguang.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    background = PaperLight,
    onBackground = Ink,
    surface = Color(0xFFFFFFFF),
    onSurface = Ink,
    surfaceVariant = Color(0xFFF1EBE1),
    onSurfaceVariant = InkSoft,
    outlineVariant = Color(0xFFE7E0D4),
    error = Color(0xFFB3564D),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE89273),
    onPrimary = Color(0xFF201410),
    background = PaperDark,
    onBackground = Color(0xFFF2EFEA),
    surface = Color(0xFF201F26),
    onSurface = Color(0xFFF2EFEA),
    surfaceVariant = Color(0xFF2A2932),
    onSurfaceVariant = Color(0xFFABA8B4),
    outlineVariant = Color(0xFF35333F),
    error = Color(0xFFE5989B),
)

@Composable
fun ShiGuangTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ShiGuangTypography,
        content = content,
    )
}
