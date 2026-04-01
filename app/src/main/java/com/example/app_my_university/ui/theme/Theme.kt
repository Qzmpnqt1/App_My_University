package com.example.app_my_university.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = MuPalette.Accent,
    onPrimary = Color.White,
    primaryContainer = MuPalette.AccentLight,
    onPrimaryContainer = MuPalette.Ink,

    secondary = MuPalette.InkMuted,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3E8EE),
    onSecondaryContainer = MuPalette.Ink,

    tertiary = MuPalette.TealAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD5F0EA),
    onTertiaryContainer = Color(0xFF002019),

    background = MuPalette.Surface,
    onBackground = MuPalette.Ink,
    surface = MuPalette.SurfaceCard,
    onSurface = MuPalette.Ink,
    surfaceVariant = Color(0xFFEEF1F5),
    onSurfaceVariant = MuPalette.InkMuted,

    error = MuPalette.Error,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    outline = MuPalette.OutlineSoft,
    outlineVariant = Color(0xFFE2E5E9)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497E),
    onPrimaryContainer = Color(0xFFD1E4FF),

    secondary = Color(0xFFB4CAD6),
    onSecondary = Color(0xFF1F333D),
    secondaryContainer = Color(0xFF364955),
    onSecondaryContainer = Color(0xFFCFE4EF),

    tertiary = Color(0xFF8BD4C6),
    onTertiary = Color(0xFF00382F),
    tertiaryContainer = Color(0xFF005045),
    onTertiaryContainer = Color(0xFFA7F0E2),

    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF44474E),
    onSurfaceVariant = Color(0xFFC4C6CF),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474E)
)

@Composable
fun AppMyUniversityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && !darkTheme -> dynamicLightColorScheme(context)
        dynamicColor && darkTheme  -> dynamicDarkColorScheme(context)
        darkTheme                  -> DarkColorScheme
        else                       -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
