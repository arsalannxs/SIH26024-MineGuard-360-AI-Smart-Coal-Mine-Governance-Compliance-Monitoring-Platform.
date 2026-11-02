package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SafetyOrange,
    onPrimary = DeepNavy,
    primaryContainer = DeepNavy,
    onPrimaryContainer = SafetyOrange,
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = NavyCardSurface,
    onSecondaryContainer = LightTeal,
    tertiary = WarningAmber,
    background = NavyDarkSurface,
    onBackground = CloudBackground,
    surface = NavyCardSurface,
    onSurface = CloudBackground,
    surfaceVariant = DeepNavy,
    onSurfaceVariant = BorderColor,
    outline = Slate,
    error = CriticalRed
)

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    onPrimary = Color.White,
    primaryContainer = LightTeal,
    onPrimaryContainer = DeepNavy,
    secondary = SafetyOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEBD6),
    onSecondaryContainer = Color(0xFF6E3900),
    tertiary = Teal,
    onTertiary = Color.White,
    background = CloudBackground,
    onBackground = MainText,
    surface = SurfaceWhite,
    onSurface = MainText,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Slate,
    outline = BorderColor,
    error = CriticalRed
)

@Composable
fun MineGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
