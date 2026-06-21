package com.challenge.hard75.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Green = Color(0xFF4CAF50)
val Amber = Color(0xFFFFC107)
val DarkBg = Color(0xFF0D0D0D)
val SurfaceColor = Color(0xFF1A1A1A)
val CardColor = Color(0xFF222222)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF9E9E9E)
val BorderColor = Color(0xFF333333)

private val DarkColors = darkColorScheme(
    primary = Green,
    secondary = Amber,
    background = DarkBg,
    surface = SurfaceColor,
    onPrimary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun Hard75Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}
