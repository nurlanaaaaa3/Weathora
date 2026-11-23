package com.nurlana.wathora.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BlueSecondary,
    secondary = GradientEnd,
    tertiary = SunnyYellow,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = WhiteText,
    onSecondary = WhiteText,
    onBackground = WhiteText,
    onSurface = WhiteText
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    secondary = BlueSecondary,
    tertiary = SunnyYellow,
    background = BlueLight,
    surface = WhiteText,
    onPrimary = WhiteText,
    onSecondary = WhiteText,
    onBackground = DarkText,
    onSurface = DarkText
)

@Composable
fun WathoraTheme(
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
