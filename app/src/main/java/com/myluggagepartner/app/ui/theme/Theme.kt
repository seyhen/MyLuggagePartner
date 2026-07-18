package com.myluggagepartner.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Le design va au-delà du ColorScheme M3 standard (surfaceContainerLow,
 * secondaryCta, error background dédié…). On expose donc une palette étendue
 * via un CompositionLocal, en plus du MaterialTheme pour la compat des composants.
 */
@Immutable
data class AppColors(
    val surface: Color,
    val surfaceContainer: Color,
    val surfaceContainerLow: Color,
    val primary: Color,
    val primaryContainer: Color,
    val onPrimary: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val secondaryCta: Color,
    val error: Color,
    val errorText: Color,
    val outline: Color,
    val isDark: Boolean,
)

private val LightAppColors = AppColors(
    surface = LightSurface,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerLow = LightSurfaceContainerLow,
    primary = LightPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimary = LightOnPrimary,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    secondaryCta = LightSecondaryCta,
    error = LightError,
    errorText = LightErrorText,
    outline = LightOutline,
    isDark = false,
)

private val DarkAppColors = AppColors(
    surface = DarkSurface,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerLow = DarkSurfaceContainerHigh,
    primary = DarkPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimary = DarkOnPrimary,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    secondaryCta = DarkSecondaryCta,
    error = DarkError,
    errorText = DarkErrorText,
    outline = DarkOutline,
    isDark = true,
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

/** Accès pratique : AppTheme.colors depuis n'importe quel composable. */
object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
}

/** Modes de thème choisis dans les Paramètres. */
enum class ThemeMode { LIGHT, DARK, AUTO }

@Composable
fun MyLuggageTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.AUTO -> isSystemInDarkTheme()
    }
    val appColors = if (dark) DarkAppColors else LightAppColors

    // ColorScheme M3 minimal pour que les composants Material stock restent cohérents.
    val scheme = if (dark) darkColorScheme(
        primary = DarkPrimary,
        onPrimary = DarkOnPrimary,
        surface = DarkSurface,
        onSurface = DarkOnSurface,
        background = DarkSurface,
        surfaceVariant = DarkSurfaceContainer,
    ) else lightColorScheme(
        primary = LightPrimary,
        onPrimary = LightOnPrimary,
        surface = LightSurface,
        onSurface = LightOnSurface,
        background = LightSurface,
        surfaceVariant = LightSurfaceContainer,
    )

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AppTypography,
            content = content,
        )
    }
}
