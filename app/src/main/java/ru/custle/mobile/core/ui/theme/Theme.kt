package ru.custle.mobile.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Green900,
    onPrimary = Color.White,
    primaryContainer = Green100,
    onPrimaryContainer = Green900,
    secondary = Sand700,
    onSecondary = Color.White,
    secondaryContainer = Sand100,
    onSecondaryContainer = Sand700,
    tertiary = Brick600,
    onTertiary = Color.White,
    tertiaryContainer = Brick100,
    onTertiaryContainer = Brick600,
    surface = Sand050,
    onSurface = Neutral800,
    surfaceVariant = Sand100,
    onSurfaceVariant = Neutral600,
    background = Sand050,
    onBackground = Neutral800,
    outline = Sand300,
    outlineVariant = Sand200,
    error = Brick600,
    errorContainer = Brick050,
    onErrorContainer = Brick600,
)

private val DarkColors = darkColorScheme(
    primary = Green100,
    onPrimary = Green900,
    primaryContainer = Green800,
    onPrimaryContainer = Green100,
    secondary = Sand300,
    onSecondary = Sand700,
    secondaryContainer = Color(0xFF3D3527),
    onSecondaryContainer = Sand200,
    tertiary = Brick300,
    onTertiary = Brick600,
    tertiaryContainer = Color(0xFF5C2E22),
    onTertiaryContainer = Brick100,
    surface = Color(0xFF1A1A18),
    onSurface = Color(0xFFE8E4DD),
    surfaceVariant = Color(0xFF2C2A25),
    onSurfaceVariant = Color(0xFFB5B0A5),
    background = Color(0xFF141412),
    onBackground = Color(0xFFE8E4DD),
    outline = Color(0xFF555048),
    outlineVariant = Color(0xFF3A3732),
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
