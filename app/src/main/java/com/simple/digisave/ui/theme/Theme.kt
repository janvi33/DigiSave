package com.simple.digisave.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DigiSaveLightColors = lightColorScheme(
    primary          = PastelGreen,
    onPrimary        = TextDark,
    secondary        = ActiveBlue,
    onSecondary      = TextDark,
    tertiary         = PastelPurple,
    onTertiary       = TextDark,
    background       = BackgroundSoft,
    onBackground     = TextDark,
    surface          = SurfaceWhite,
    onSurface        = TextDark,
    onSurfaceVariant = TextGray,
    error            = PastelRed,
    onError          = TextDark,
)

private val DigiSaveDarkColors = darkColorScheme(
    primary          = AccentTeal,
    onPrimary        = Color.White,
    secondary        = ActiveBlue,
    onSecondary      = Color.White,
    tertiary         = PastelPurple,
    onTertiary       = Color.White,
    background       = Color(0xFF1A1A1A),
    onBackground     = SurfaceWhite,
    surface          = Color(0xFF242424),
    onSurface        = SurfaceWhite,
    onSurfaceVariant = TextGray,
    error            = PastelRed,
    onError          = TextDark,
)

@Composable
fun DigiSaveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DigiSaveDarkColors else DigiSaveLightColors

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = colors.primary,
            darkIcons = !darkTheme
        )
        systemUiController.setNavigationBarColor(
            color = colors.surface,
            darkIcons = !darkTheme
        )
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
