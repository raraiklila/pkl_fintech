package com.example.pkl_finance.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ─────────────────────────────────────────────────────────────────────────────
// PKL Finance – Material 3 Color Scheme
// Uses design-system tokens from Color.kt
// ─────────────────────────────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    // Brand
    primary            = Blue500,          // Main Brand
    onPrimary          = ColorWhite,
    primaryContainer   = Blue50,           // Background section
    onPrimaryContainer = Blue900,

    // Secondary (Gold)
    secondary            = Yellow500,      // Gold Accent
    onSecondary          = Blue900,        // Text on Gold
    secondaryContainer   = Yellow50,       // Gold card background
    onSecondaryContainer = Yellow800,

    // Tertiary – uses a deeper navy as accent
    tertiary            = Blue700,         // Heading
    onTertiary          = ColorWhite,
    tertiaryContainer   = Blue100,
    onTertiaryContainer = Blue900,

    // Error
    error            = ErrorRed,
    onError          = ColorWhite,
    errorContainer   = ErrorRedLight,
    onErrorContainer = ErrorRed,

    // Neutrals
    background    = ColorBackground,
    onBackground  = ColorBlack,
    surface       = ColorSurface,
    onSurface     = ColorBlack,
    surfaceVariant   = Blue50,
    onSurfaceVariant = Blue800,
    outline       = ColorBorder,
    outlineVariant = ColorDivider,

    // Inverse
    inversePrimary = Blue200,
    inverseSurface = Blue800,
    inverseOnSurface = ColorWhite,
)

private val DarkColorScheme = darkColorScheme(
    primary            = Blue400,
    onPrimary          = Blue900,
    primaryContainer   = Blue700,
    onPrimaryContainer = Blue50,

    secondary            = Yellow500,
    onSecondary          = Blue900,
    secondaryContainer   = Yellow800,
    onSecondaryContainer = Yellow100,

    tertiary            = Blue300,
    onTertiary          = Blue900,
    tertiaryContainer   = Blue800,
    onTertiaryContainer = Blue100,

    error            = ErrorRedBadge,
    onError          = ColorBlack,
    errorContainer   = ErrorRed,
    onErrorContainer = ErrorRedLight,

    background    = Color(0xFF0F1A2E),
    onBackground  = ColorWhite,
    surface       = Color(0xFF1A2744),
    onSurface     = ColorWhite,
    surfaceVariant   = Blue800,
    onSurfaceVariant = Blue100,
    outline       = Blue600,
    outlineVariant = Blue700,

    inversePrimary   = Blue500,
    inverseSurface   = Blue50,
    inverseOnSurface = Blue900,
)

@Composable
fun Pkl_financeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep dynamic colors disabled so our design system is used
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        shapes      = ExpressiveShapes,
        content     = content
    )
}