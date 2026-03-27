package ru.custle.mobile.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Primary400,
    onPrimary = Primary950,
    primaryContainer = Primary800,
    onPrimaryContainer = Primary200,
    secondary = Gray400,
    onSecondary = Gray900,
    secondaryContainer = Color(0xFF232530),
    onSecondaryContainer = Gray200,
    tertiary = Accent400,
    onTertiary = Gray900,
    tertiaryContainer = Color(0xFF3D2E0A),
    onTertiaryContainer = Accent100,
    surface = Color(0xFF15171E),
    onSurface = Color(0xFFE2E4EA),
    surfaceVariant = Color(0xFF1C1E26),
    onSurfaceVariant = Color(0xFF9498A5),
    surfaceContainerHighest = Color(0xFF262830),
    surfaceContainerHigh = Color(0xFF20222A),
    surfaceContainer = Color(0xFF1A1C24),
    surfaceContainerLow = Color(0xFF16181F),
    surfaceContainerLowest = Color(0xFF111318),
    background = Color(0xFF0F1117),
    onBackground = Color(0xFFE2E4EA),
    outline = Color(0xFF3A3D48),
    outlineVariant = Color(0xFF2A2D36),
    error = Color(0xFFFF6B6B),
    errorContainer = Color(0xFF3D1515),
    onErrorContainer = Color(0xFFFFB3B3),
    inverseSurface = Color(0xFFE2E4EA),
    inverseOnSurface = Color(0xFF15171E),
    inversePrimary = Primary600,
)

// Light kept for reference but not used
private val LightColors = lightColorScheme(
    primary = Primary600,
    onPrimary = Color.White,
    primaryContainer = Primary100,
    onPrimaryContainer = Primary900,
    secondary = Gray600,
    onSecondary = Color.White,
    secondaryContainer = Gray100,
    onSecondaryContainer = Gray700,
    tertiary = Accent500,
    onTertiary = Color.White,
    tertiaryContainer = Accent100,
    onTertiaryContainer = Amber700,
    surface = Color.White,
    onSurface = Gray900,
    surfaceVariant = Gray050,
    onSurfaceVariant = Gray500,
    background = Gray050,
    onBackground = Gray900,
    outline = Gray300,
    outlineVariant = Gray200,
    error = Red600,
    errorContainer = Red050,
    onErrorContainer = Red700,
)

@Composable
fun CustleTheme(
    content: @Composable () -> Unit,
) {
    // Force dark theme
    MaterialTheme(
        colorScheme = DarkColors,
        typography = CustleTypography,
        content = content,
    )
}
