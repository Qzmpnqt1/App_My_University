package com.example.app_my_university.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Семантические цвета поверх [ColorScheme]: success / warning / info для бейджей и акцентов.
 * Top bar и графики опираются на [ColorScheme]; контейнеры статусов — на этот набор.
 */
@Immutable
data class AppExtendedColors(
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
)

val LocalAppExtendedColors = staticCompositionLocalOf<AppExtendedColors> {
    error("LocalAppExtendedColors не задан — оберните контент в AppMyUniversityTheme")
}

object AppThemeExtras {
    val extendedColors: AppExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppExtendedColors.current
}

fun appExtendedColors(darkTheme: Boolean): AppExtendedColors =
    if (darkTheme) {
        AppExtendedColors(
            successContainer = Color(0xFF0F3D30),
            onSuccessContainer = Color(0xFF9EE8C8),
            warningContainer = Color(0xFF4A3800),
            onWarningContainer = Color(0xFFFFDEA8),
            infoContainer = Color(0xFF1A3F52),
            onInfoContainer = Color(0xFFB8DDF5),
        )
    } else {
        AppExtendedColors(
            successContainer = Color(0xFFD4F0E3),
            onSuccessContainer = Color(0xFF0A3D28),
            warningContainer = Color(0xFFFFE8CC),
            onWarningContainer = Color(0xFF5C3800),
            infoContainer = Color(0xFFD4EAF5),
            onInfoContainer = Color(0xFF063A52),
        )
    }
