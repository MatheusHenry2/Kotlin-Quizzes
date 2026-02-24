package com.example.kotlinquizzes.core.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme =
    lightColorScheme(
        primary = Purple600,
        onPrimary = White,
        primaryContainer = Purple100,
        onPrimaryContainer = Purple700,
        background = White,
        onBackground = Black,
        surface = White,
        onSurface = Black,
        surfaceVariant = Gray50,
        onSurfaceVariant = Gray600,
        error = Error,
        onError = White,
        outline = Gray200,
    )

@Composable
fun KotlinQuizzesTheme(content: @Composable () -> Unit) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
