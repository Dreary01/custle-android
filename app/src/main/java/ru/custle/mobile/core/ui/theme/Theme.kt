package ru.custle.mobile.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Green900,
    secondary = Sand700,
    tertiary = Brick600,
    surface = Sand050,
    background = Sand100,
)

private val DarkColors = darkColorScheme(
    primary = Sand200,
    secondary = Sand500,
    tertiary = Brick300,
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
