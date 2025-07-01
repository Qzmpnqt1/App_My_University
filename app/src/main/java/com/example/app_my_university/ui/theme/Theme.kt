package com.example.app_my_university.ui.theme

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
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
    primary = Color(0xFF0055A2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),

    secondary = Color(0xFF965F00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDB2),
    onSecondaryContainer = Color(0xFF301D00),

    tertiary = Color(0xFF4E5F7D),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD5E3FF),
    onTertiaryContainer = Color(0xFF091C37),

    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFAFAFA),
    onSurface = Color(0xFF1A1C1E),

    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF00315C),
    primaryContainer = Color(0xFF004784),
    onPrimaryContainer = Color(0xFFD1E4FF),

    secondary = Color(0xFFFFBA50),
    onSecondary = Color(0xFF502E00),
    secondaryContainer = Color(0xFF724600),
    onSecondaryContainer = Color(0xFFFFDDB2),

    tertiary = Color(0xFFB7C7FF),
    onTertiary = Color(0xFF1F3050),
    tertiaryContainer = Color(0xFF364767),
    onTertiaryContainer = Color(0xFFD5E3FF),

    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E6),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun AppMyUniversityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && !darkTheme -> dynamicLightColorScheme(context)
        dynamicColor && darkTheme     -> dynamicDarkColorScheme(context)
        darkTheme                     -> DarkColorScheme
        else                          -> LightColorScheme
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
