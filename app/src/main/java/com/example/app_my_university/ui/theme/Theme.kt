package com.example.app_my_university.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1F4D7A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD0E4FF),
    onPrimaryContainer = Color(0xFF001D36),

    secondary = Color(0xFF445968),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD2DBE5),
    onSecondaryContainer = Color(0xFF1B2832),

    tertiary = Color(0xFF2A6B5F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFB8EBE3),
    onTertiaryContainer = Color(0xFF002019),

    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = Color(0xFFF4F6FA),
    onBackground = Color(0xFF191C20),
    surface = Color(0xFFFAFAFC),
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFDEE3EB),
    onSurfaceVariant = Color(0xFF3F4A57),

    outline = Color(0xFF6F7888),
    outlineVariant = Color(0xFFBFC6D1),
    scrim = Color(0xFF000000),

    inverseSurface = Color(0xFF2E3136),
    inverseOnSurface = Color(0xFFF0F0F4),
    inversePrimary = Color(0xFF9ECAFF),

    surfaceDim = Color(0xFFD4D9E0),
    surfaceBright = Color(0xFFFAFAFC),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEFF1F6),
    surfaceContainer = Color(0xFFE8EBF2),
    surfaceContainerHigh = Color(0xFFE2E6ED),
    surfaceContainerHighest = Color(0xFFDCE1E9),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB0C8F0),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF2E5280),
    onPrimaryContainer = Color(0xFFD4E4FF),

    secondary = Color(0xFFB6C4D0),
    onSecondary = Color(0xFF1F2A32),
    secondaryContainer = Color(0xFF364855),
    onSecondaryContainer = Color(0xFFD2DBE5),

    tertiary = Color(0xFF8FD4C8),
    onTertiary = Color(0xFF00382F),
    tertiaryContainer = Color(0xFF1A5C52),
    onTertiaryContainer = Color(0xFFA8F0E5),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF12161C),
    onBackground = Color(0xFFE1E4EA),
    surface = Color(0xFF12161C),
    onSurface = Color(0xFFE1E4EA),
    surfaceVariant = Color(0xFF3D4450),
    onSurfaceVariant = Color(0xFFBFC6D4),

    outline = Color(0xFF89909C),
    outlineVariant = Color(0xFF3D4450),
    scrim = Color(0xFF000000),

    inverseSurface = Color(0xFFE1E4EA),
    inverseOnSurface = Color(0xFF2E3136),
    inversePrimary = Color(0xFF1F4D7A),

    surfaceDim = Color(0xFF12161C),
    surfaceBright = Color(0xFF383F4A),
    surfaceContainerLowest = Color(0xFF0D1015),
    surfaceContainerLow = Color(0xFF1A1F27),
    surfaceContainer = Color(0xFF1E242D),
    surfaceContainerHigh = Color(0xFF282F39),
    surfaceContainerHighest = Color(0xFF333A45),
)

@Composable
fun AppMyUniversityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extended = remember(darkTheme) { appExtendedColors(darkTheme) }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
    ) {
        CompositionLocalProvider(LocalAppExtendedColors provides extended) {
            content()
        }
    }
}
