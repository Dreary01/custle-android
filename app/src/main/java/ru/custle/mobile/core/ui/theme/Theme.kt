package ru.custle.mobile.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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

private val DarkColors = darkColorScheme(
    primary = Primary400,
    onPrimary = Primary950,
    primaryContainer = Primary800,
    onPrimaryContainer = Primary100,
    secondary = Gray400,
    onSecondary = Gray900,
    secondaryContainer = Gray700,
    onSecondaryContainer = Gray200,
    tertiary = Accent400,
    onTertiary = Gray900,
    tertiaryContainer = Color(0xFF5C3D0A),
    onTertiaryContainer = Accent100,
    surface = Color(0xFF111318),
    onSurface = Gray100,
    surfaceVariant = Color(0xFF1E2028),
    onSurfaceVariant = Gray400,
    background = Color(0xFF0D0E12),
    onBackground = Gray100,
    outline = Gray600,
    outlineVariant = Gray700,
)

@Composable
fun CustleTheme(
    content: @Composable () -> Unit,
) {
    val colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CustleTypography,
        content = content,
    )
}
