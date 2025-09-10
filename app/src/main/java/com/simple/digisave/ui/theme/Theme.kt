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
    primary = PastelGreen,
    secondary = ActiveBlue,
    tertiary = PastelPurple,
    background = BackgroundSoft,
    surface = SurfaceWhite,
    onPrimary = TextDark,
    onSecondary = TextDark,
    onTertiary = TextDark,
    onBackground = TextDark,
    onSurface = TextDark,
    error = PastelRed,
)

private val DigiSaveDarkColors = darkColorScheme(
    primary = AccentTeal,
    secondary = ActiveBlue,
    tertiary = PastelPurple,
    background = TextDark,
    surface = Color(0xFF121212),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = SurfaceWhite,
    onSurface = SurfaceWhite,
    error = PastelRed,
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
            color = colors.primary,       // 👈 Status bar = same as TopBar
            darkIcons = !darkTheme
        )
        systemUiController.setNavigationBarColor(
            color = colors.surface,       // 👈 Matches BottomNav bg
            darkIcons = !darkTheme
        )
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
